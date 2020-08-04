// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2020 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBC is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBC is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBC.  If not, see <http://www.gnu.org/licenses/>.

package ibcalpha.ibc;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

public class LoginFailedDialogHandler implements WindowHandler  {
    final String DIALOG_TITLE = "Login failed";

    @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            case WindowEvent.WINDOW_CLOSED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handleWindow(Window window, int eventID) {
        if (eventID == WindowEvent.WINDOW_OPENED) {
            Utils.logToConsole("Login failed");
            LoginManager.loginManager().setLoginState(LoginManager.LoginState.LOGIN_FAILED);
        } else {
            if ((LoginManager.loginManager().getLoginFrame().getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED)
                Utils.logToConsole("Ensure login frame visible");
                LoginManager.loginManager().getLoginFrame().setExtendedState(Frame.NORMAL);
        }
    }

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JDialog)) return false;

        return (SwingUtils.titleContains(window, DIALOG_TITLE));
    }

}
