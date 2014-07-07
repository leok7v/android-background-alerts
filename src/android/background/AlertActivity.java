/*  Copyright (c) 2013, Leo Kuznetsov
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 * Neither the name of the {organization} nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package android.background;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.method.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import static android.background.util.*;
import static android.view.View.MeasureSpec.*;

/**
 * Do not instantiate directly or indirectly. Use "alerts" static member functions instead.
 */

public final class AlertActivity extends BaseActivity implements DialogInterface.OnDismissListener,
        DialogInterface.OnCancelListener {

    public static final int BUTTON_CANCEL = 0;
    private static final String ACTION = AlertActivity.class.getPackage().getName() + ".intent.action.ALERT";

    private static HashMap<Long, int[]> result = new HashMap<Long, int[]>();
    private static HashMap<Long, Runnable> response = new HashMap<Long, Runnable>();
    private static LinkedList<Intent> queue = new LinkedList<Intent>();

    private final DisplayMetrics dm = new DisplayMetrics();
    private AlertDialog dialog;
    private long id;
    private static long xid;

    public static void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        G.app.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                assertion(Thread.currentThread() == G.mainThread);
                // if more than one process is presenting UI G.hadActivities may need to be replaced with
                // something like getProcessName() logic to route AlertReceiver to specific process
                if (G.hadActivities) {
                    enqueue(intent);
                }
                // else we are running in service background process with no UI
                // intent is ignored here but will be received in the UI process too
            }
        }, filter);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View center = new View(this);
        center.setWillNotDraw(false);
        Drawable d = new ColorDrawable(Color.TRANSPARENT);
        d.setAlpha(0);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) { // noinspection deprecation
            center.setBackgroundDrawable(d);
        } else {
            center.setBackground(d);
        }
        setContentView(center);
    }

    public void onStart() {
        super.onStart();
        buildAndShow(getIntent());
    }

    public void onStop() {
        dismiss();
        super.onStop();
    }

    public void onDestroy() {
        dialog = null;
        super.onDestroy();
    }

    private void dismiss() {
        if (dialog != null) {
            dialog.setOnDismissListener(null);
            dialog.dismiss();
        }
    }

    private void result(int whichButton) {
        int[] res = result.get(id);
        if (res != null) {
            res[0] = whichButton;
        }
        result.remove(id);
        Runnable r = response.get(id);
        if (r != null) {
            G.mainHandler.post(r);
        }
        response.remove(id);
    }

    public static void enqueue(String title, String message, String details,
                boolean cancelable,
                String positive, String neutral, String negative,
                final int result[], final Runnable response) {
        final Intent i = new Intent(G.app, AlertActivity.class);
        i.putExtra("title", title).putExtra("message", message).putExtra("details", details).
        putExtra("cancelable", cancelable).
        putExtra("positive", positive).putExtra("neutral", neutral).putExtra("negative", negative).
        putExtra("id", xid).setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Thread.currentThread() == G.mainThread) {
            AlertActivity.response.put(xid, response);
            AlertActivity.result.put(xid, result);
            xid++;
            enqueue(i);
        } else { // called from background thread
            G.mainHandler.post(new Runnable() {
                public void run() {
                    AlertActivity.response.put(xid, response);
                    AlertActivity.result.put(xid, result);
                    xid++;
                    enqueue(i);
                }
            });
        }
    }

    public static void broadcast(String title, String message, String details, boolean cancelable,
            String positive, String neutral, String negative) {
        Intent i = new Intent(ACTION);
        i.putExtra("title", title).putExtra("message", message).putExtra("details", details).
        putExtra("cancelable", cancelable).
        putExtra("positive", positive).putExtra("neutral", neutral).putExtra("negative", negative);
        G.app.sendBroadcast(i);
    }

    public void onDismiss(DialogInterface dialog) {
        if (queue.isEmpty()) {
            finish();
        } else {
            buildAndShow(queue.remove());
        }
    }

    public void onCancel(DialogInterface dialog) {
        result(0);
    }

    private static void enqueue(Intent intent) {
        if (G.act instanceof AlertActivity) {
            queue.add((Intent)intent.clone());
        } else {
            Intent i = new Intent(G.app, AlertActivity.class);
            i.putExtras(intent.getExtras());
            i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
            G.app.startActivity(i);
        }
    }

    private DialogInterface.OnClickListener finishOnClick = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) { result(whichButton); }
    };

    private void buildAndShow(Intent i) {
        Bundle b = i.getExtras();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(b.getString("title")).setMessage(b.getString("message"));
        String details = b.getString("details");
        if (details != null) {
            TextView view = new TextView(this) {
                protected void onMeasure(int wms, int hms) {
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    setMeasuredDimension(util.measure(getMode(wms), getSize(wms), dm.widthPixels / 4),
                                         util.measure(getMode(hms), getSize(hms), dm.heightPixels / 4));
                }
            };
            view.setMaxLines(10);
            view.setMovementMethod(new ScrollingMovementMethod());
            view.setFocusableInTouchMode(true);
            builder.setView(view);
            int padding = Math.round(unitToPixels(TypedValue.COMPLEX_UNIT_PT, 10f));
            view.setPadding(padding, 0, padding, 0);
            view.setText(details);
        }
        String positive = b.getString("positive");
        if (positive != null) {
            builder.setPositiveButton(positive, finishOnClick);
        }
        String neutral = b.getString("neutral");
        if (neutral != null) {
            builder.setNeutralButton(neutral, finishOnClick);
        }
        String negative = b.getString("negative");
        if (negative != null) {
            builder.setNegativeButton(negative, finishOnClick);
        }
        builder.setCancelable(b.getBoolean("cancelable", true));
        id = b.getLong("id", -1);
        dialog = builder.create();
        dialog.setOnDismissListener(this);
        dialog.show();
    }

}
