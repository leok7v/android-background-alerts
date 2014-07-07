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

/* all the methods in this class are thread safe and can be called from any application thread */

@SuppressWarnings("UnusedDeclaration")
public class alerts {

    public static void ok(int title, int message) {
        alert(G.app.getResources().getString(title), G.app.getResources().getString(message), null, true, "OK");
    }

    public static void ok(int title, int message, int details) {
        alert(G.app.getResources().getString(title), G.app.getResources().getString(message),
              G.app.getResources().getString(details), true, "OK");
    }

    public static void ok(String title, String message) {
        alert(title, message, null, true, "OK");
    }

    public static void ok(String title, String message, String details) {
        alert(title, message, details, true, "OK");
    }

    public static void alert(String title, String message, String details, boolean cancelable, String positive) {
        alert(title, message, details, cancelable, positive, null);
    }

    public static void alert(String title, String message, String details, boolean cancelable,
            String positive, String negative) {
        alert(title, message, details, cancelable, positive, null, negative);
    }

    public static void alert(String title, String message, String details, boolean cancelable,
            String positive, String neutral, String negative) {
        AlertActivity.broadcast(title, message, details, cancelable, positive, neutral, negative);
    }

    /* "ask" can only be called from the foreground process that has user facing activities. */

    public static void ask(String title, String message, String details, boolean cancelable, String positive,
            String neutral, String negative, int result[], Runnable response) {
        assertion(G.hadActivities); // cannot be called from background UI-less service
        assertion(result == null || result.length == 1); // exactly one integer returned
        AlertActivity.enqueue(title, message, details, cancelable, positive, neutral, negative, result, response);
    }

    private alerts() {}

}
