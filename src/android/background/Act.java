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

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class Act extends BaseActivity implements Runnable {

    private BackgroundThread backgroundThread;
    private static final Paint paint = new Paint();
    private TextView tv;
    private int count;

    static {
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = new TextView(this);
        tv.setText("Please wait couple of dozen seconds for the first alert to appear");
        tv.setTextColor(Color.YELLOW);
        Button button = new Button(this);
        button.setText("Dialog & Keyboard");
        button.setOnClickListener(showDialog);
        LinearLayout vg = new LinearLayout(this);
        vg.setOrientation(LinearLayout.VERTICAL);
        vg.setWillNotDraw(false);
        vg.setBackgroundColor(Color.BLUE);
        vg.addView(tv);
        vg.addView(button);
        setContentView(vg);
        backgroundThread = new BackgroundThread(10, this);
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            backgroundThread.join();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private View.OnClickListener showDialog = new View.OnClickListener() { public void onClick(View v) { showDialog(); } };

    private void showDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Dialog");
        alert.setMessage("Enter Text Please");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                tv.setText(input.getText().toString());
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                tv.setText("Cancelled");
            }
        });
        alert.show();
    }

    public void run() {
        String name = BackgroundThread.class.getSimpleName();
        String details = "You have to choose what needs to be done...";
        final int[] result = new int[]{0};
        final Runnable response = new Runnable() {
            public void run() {
                String s;
                switch (result[0]) {
                    case DialogInterface.BUTTON_POSITIVE: s = "positive"; break;
                    case DialogInterface.BUTTON_NEUTRAL:  s = "neutral"; break;
                    case DialogInterface.BUTTON_NEGATIVE: s = "negative"; break;
                    case AlertActivity.BUTTON_CANCEL: s = "cancel"; break;
                    default: s = "unexpected " + result[0]; break;
                }
                tv.setText(s);
            }
        };
        alerts.ask("2B|!2B", "From " + name + ":" + count, details, false, "Retry", "Ignore", "Abort", result, response);
        count++;
    }

}
