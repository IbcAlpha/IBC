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
import javax.swing.JFrame;

class LoginFrameHandler implements WindowHandler {
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(Window window, int eventID) {
        if (eventID != WindowEvent.WINDOW_OPENED) return;
        TwsListener.setLoginFrame((JFrame) window);

        if (! setFieldsAndClick(window)) {
            Utils.logError("could not login because we could not find one of the controls.");
        }
    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;

        // we check for the presence of the Login button because 
        // TWS displays a different (information-only) dialog, also 
        // entitled Login, when it's trying to reconnect
        return ((Utils.titleEquals(window, "New Login") ||
                Utils.titleEquals(window, "Login")) &&
                Utils.findButton(window, "Login") != null);
    }

    private boolean setFieldsAndClick(final Window window) {
        if (! Utils.setTextField(window, 0, TwsListener.getIBAPIUserName())) return false;
        if (! Utils.setTextField(window, 1, TwsListener.getIBAPIPassword())) return false;
        if (! Utils.setCheckBoxSelected(window,
                                            "Use/store settings on server",
                                            Settings.getBoolean("StoreSettingsOnServer", false))) return false;

        if (TwsListener.getIBAPIUserName().length() == 0) {
            Utils.findTextField(window, 0).requestFocus();
            return true;
        }
        if (TwsListener.getIBAPIPassword().length() == 0) {
            Utils.findTextField(window, 1).requestFocus();
            return true;
        }

        if (Utils.findButton(window, "Login") == null) return false;
        
        GuiDeferredExecutor.instance().execute(new Runnable() {
            @Override
            public void run() {
                Utils.clickButton(window, "Login");
            }
        });

        return true;
    }
}

