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
import android.os.*;

public abstract class BaseService extends Service implements Runnable {

    private static BackgroundThread thread;

    public int onStartCommand(Intent i, int flags, int id) {
        boolean sticky = i != null && i.getExtras() != null && i.getExtras().getBoolean("sticky", false);
        boolean foreground = i != null && i.getExtras() != null && i.getExtras().getBoolean("foreground", false);
        if (thread == null) {
            thread = new BackgroundThread(10, this);
        }
        if (foreground) {
            // service MUST call "startForeground" to become semi-true daemon
            Notification note = new Notification.Builder(getApplicationContext()).
                    setContentTitle(getClass().getSimpleName()).setSmallIcon(R.drawable.alert).build();
            note.flags |= Notification.FLAG_NO_CLEAR; // does not allow user to clear the notification
            startForeground(id, note);
        }
        return (sticky ? START_STICKY : START_NOT_STICKY) | START_REDELIVER_INTENT;
    }

    public void onDestroy() { // usually not called
        super.onDestroy();
        stopForeground(true);
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
        thread = null;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public abstract void run();

}