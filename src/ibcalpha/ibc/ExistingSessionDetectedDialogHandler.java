// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2018 Richard L King (rlking@aultan.com)
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class ExistingSessionDetectedDialogHandler implements WindowHandler {

    private boolean hasTakenOverAnotherSession;

    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(Window window, int eventID) {
        String setting = Settings.settings().getString("ExistingSessionDetectedAction", "manual");
        if (setting.equalsIgnoreCase("primary")) {
            if (hasTakenOverAnotherSession) {
                Utils.logToConsole("Other session must be a primary: let the other session proceed");
                if (!SwingUtils.clickButton(window, "Cancel") && !SwingUtils.clickButton(window, "Exit Application")) {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'Cancel' or 'Exit Application' button wasn't found.");
                }
            } else {
                Utils.logToConsole("End the other session and continue this one");
                hasTakenOverAnotherSession = true;
                if (!SwingUtils.clickButton(window, "OK") &&
                        !SwingUtils.clickButton(window, "Continue Login") &&
                        !SwingUtils.clickButton(window, "Reconnect This Session")) {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'OK' or 'Continue Login' or 'Reconnect This Session' button wasn't found.");
                }
            }
        } else if (setting.equalsIgnoreCase("primaryoverride")) {
            if (LoginManager.loginManager().getLoginState() != LoginManager.LoginState.LOGGED_IN) {
                /* The login has not yet been completed, so this is a new IBC instance
                   and we must continue this one and let the other one finish
                */
                Utils.logToConsole("End the other session and continue this one");
                if (!SwingUtils.clickButton(window, "OK") &&
                        !SwingUtils.clickButton(window, "Continue Login") &&
                        !SwingUtils.clickButton(window, "Reconnect This Session")) {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'OK' or 'Continue Login' or 'Reconnect This Session' button wasn't found.");
                }
            } else {
                /* The login has already been completed, and the ExistingSessionDetected
                   dialog is in response to handing the ReLogin dialog after a new
                   primary or primaryoverride IBC was started. We must terminate this
                   instance and let the other one proceed
                */
                Utils.logToConsole("End this session and let the other session proceed");
                if (!SwingUtils.clickButton(window, "Cancel") && !SwingUtils.clickButton(window, "Exit Application")) {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'Cancel' or 'Exit Application' button wasn't found.");
                }
            }
        } else if (setting.equalsIgnoreCase("primaryalways")) {
            Utils.logToConsole("End the other session and continue this one");
            if (!SwingUtils.clickButton(window, "OK") &&
                    SwingUtils.clickButton(window, "Continue Login") &&
                    SwingUtils.clickButton(window, "Reconnect This Session")) {
                Utils.logError("could not handle 'Existing session detected' dialog because the 'OK' or 'Continue Login' or 'Reconnect This Session' button wasn't found.");
            }
        } else if (setting.equalsIgnoreCase("secondary")) {
            Utils.logToConsole("End this session and let the other session proceed");
            if (!SwingUtils.clickButton(window, "Cancel") && !SwingUtils.clickButton(window, "Exit Application")) {
                Utils.logError("could not handle 'Existing session detected' dialog because the 'Cancel' or 'Exit Application' button wasn't found.");
            }
        } else if (setting.equalsIgnoreCase("manual")) {
            Utils.logToConsole("User must choose whether to continue with this session");
            // nothing to do
        } else {
            Utils.logError("could not handle 'Existing session detected' dialog because the ExistingSessionDetectedAction setting is invalid.");
        }
    }

    public boolean recogniseWindow(Window window) {
        if (!(window instanceof JDialog)) return false;

        return (SwingUtils.titleContains(window, "Existing session detected"));
    }
}
