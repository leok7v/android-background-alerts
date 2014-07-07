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

import static android.background.util.*;

public final class KeepAliveThread {

    private int sleep;
    private boolean quit;
    private Thread thread;

    public KeepAliveThread(int sleepSeconds) {
        sleep = sleepSeconds;
        thread = new Thread() { public void run() { background(); } };
        thread.start();
    }

    private void background() {
        assertion(Thread.currentThread() != G.mainThread);
        util.startService(OutOfProcessNotSticky.class, false, true);
        util.startService(OutOfProcessSticky.class, true, false);
        util.startService(InProcessService.class, false, false);
        while (!quit) {
            try {
                if (G.app != null && !util.isServiceRunning(OutOfProcessNotSticky.class)) {
                    util.startService(OutOfProcessNotSticky.class, false, true);
                }
                Thread.sleep(sleep * 1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void join() throws InterruptedException {
        quit = true;
        thread.interrupt();
        thread.join();
        thread = null; // not reusable
    }

}