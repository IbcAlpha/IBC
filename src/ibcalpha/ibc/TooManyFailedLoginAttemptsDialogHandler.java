// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2021 Richard L King (rlking@aultan.com)
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

public class TooManyFailedLoginAttemptsDialogHandler implements WindowHandler {
        @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handleWindow(Window window, int eventID) {
        // restart IBC in 5 minutes, which will ensure enough
        // time has passed before the next login attempt
        Utils.logToConsole("Too many failed login attempts");
        
        if (Settings.settings().getBoolean("ExitAfterSecondFactorAuthenticationTimeout", false)) {
            Utils.logToConsole("Restarting in 5 minutes");
            LoginManager.loginManager().restartAfterTime(300,"IBC closing after too many failed login attempts");
            if (!SwingUtils.clickButton(window, "OK")) {
                Utils.logError("could not dismiss \"Too many failed login attempts\" dialog because we could not find one of the controls.");
            }
        }
    }

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JDialog)) return false;

        return (SwingUtils.findTextArea(window, "Too many failed login attempts") != null);
    }

}
