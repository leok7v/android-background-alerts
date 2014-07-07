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
import android.content.res.*;
import android.util.*;

import java.util.*;

import static android.view.View.MeasureSpec.*;

@SuppressWarnings("UnusedDeclaration")
public class util {

    public static void assertion(boolean b) {
        if (!b) {
            throw new AssertionError();
        }
    }

    public static float unitToPixels(int unit, float size) {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        if (dm.xdpi == 75 && dm.widthPixels >= 1920 && dm.heightPixels >= 1000) {
            dm.xdpi = 100; // most probably unrecognized HDMI monitor with dpi > 90
            dm.ydpi = 100;
        }
        return TypedValue.applyDimension(unit, size, dm);
    }

    public static int measure(int mode, int size, int preferred) {
        return mode == EXACTLY ? size :
              (mode == AT_MOST ? Math.min(preferred, size) : preferred);
    }

    public static boolean isServiceRunning(Class c) {
        if (G.app != null) {
            ActivityManager am = (ActivityManager)G.app.getSystemService(ContextWrapper.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo si : rs) {
                if (c.getCanonicalName().equals(si.service.getClassName()) && si.started) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void startService(Class c, boolean sticky, boolean foreground) {
        if (!isServiceRunning(c)) {
            Intent i = new Intent(G.app, c);
            i.putExtra("sticky", sticky);
            i.putExtra("foreground", foreground);
            G.app.startService(i);
        }
    }

    private util() { }

}
