// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JFrame;

class LoginFrameHandler implements WindowHandler {
    public void handleWindow(Window window, int eventID) {
        if (eventID != WindowEvent.WINDOW_OPENED) return;
        TwsListener.setLoginFrame((JFrame) window);

        if (! setFieldsAndClick(window)) {
            System.err.println("IBController: could not login because we could not find one of the controls.");
        }
    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;

        return (Utils.titleEquals(window, "New Login") ||
                Utils.titleEquals(window, "Login"));
    }

    private boolean setFieldsAndClick(final Window window) {
        if (! Utils.setTextField(window, 0, TwsListener.getUserName())) return false;
        if (! Utils.setTextField(window, 1, TwsListener.getPassword())) return false;
        if (! Utils.setCheckBoxSelected(window,
                                            "Use/store settings on server",
                                            Settings.getBoolean("StoreSettingsOnServer", false))) return false;

        if (TwsListener.getUserName().length() == 0) {
            Utils.findTextField(window, 0).requestFocus();
            return true;
        }
        if (TwsListener.getPassword().length() == 0) {
            Utils.findTextField(window, 1).requestFocus();
            return true;
        }

        if (Utils.findButton(window, "Login") == null) return false;

        /* Starting with TWS 903, when the username and password fields are filled in
         * programmatically, the Login button is not enabled (though it is if these
         * fields are filled in manually).
         * 
         * Moreover, once we've enabled the login button by calling setEnabled,
         * clicking it immediately does not always work: for some reason a short delay
         * is needed (but calling Thread.sleep() on the current thread doesn't work).
         * 
         * So we create a timer task to enable the button and click it after a short delay.
         */

        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                final AtomicBoolean done = new AtomicBoolean(false);

                /* we keep clicking the login button periodically until it
                 * becomes disabled, as this seems to be a good indicator
                 * that the login has actually taken effect
                 */
                do {
                    GuiSynchronousExecutor.instance().execute(new Runnable() {
                        public void run() {
                            Utils.clickButton(window, "Login");
                            done.set(! Utils.isButtonEnabled(window, "Login"));
                        }
                    });
                    Utils.pause(500);
                }
                while (! done.get());
            }
        }, 10);

        return true;
    }
}

