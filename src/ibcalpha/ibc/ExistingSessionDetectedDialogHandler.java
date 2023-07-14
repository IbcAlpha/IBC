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

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

public class ExistingSessionDetectedDialogHandler implements WindowHandler {

    private boolean attempted;

    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(Window window, int eventID) {
        final String MANUAL = "manual";
        final String PRIMARY = "primary";
        final String PRIMARY_OVERRIDE = "primaryoverride";
        final String SECONDARY = "secondary";
        
        String setting = Settings.settings().getString("ExistingSessionDetectedAction", "manual").toLowerCase();
        
        switch (setting) {
            case MANUAL:
            case PRIMARY:
            case PRIMARY_OVERRIDE:
            case SECONDARY:
                break;
            default:
                Utils.logToConsole("ExistingSessionDetectedAction has an invalid value: " + setting + ": assuming 'secondary'");
                setting = SECONDARY;
        }

        if (setting.equals(MANUAL)) {
            Utils.logToConsole("User must choose whether to continue with this session (scenario 1)");
            // nothing to do
            return;
        }

        if (setting.equals(SECONDARY)) {
            Utils.logToConsole("End this session and let the other session proceed (scenario 2)");
            if (!SwingUtils.clickButton(window, "Cancel") && !SwingUtils.clickButton(window, "Exit Application")) {
                Utils.logError("could not handle 'Existing session detected' dialog because the 'Cancel' or 'Exit Application' button wasn't found.");
            }
            return;
        }

        /* The handling of this dialog is based on the following observed
           sequence of events.

           If session A is logged in, and session B tries tologin, session B
           displays the'Existing Session Detected' dialog.

           If session B chooses to continue login, session A displays the
           'Re-login is required' dialog. If session A clicks the 'Re-login'
           button, session B then also displays the 'Re-login is required'
           dialog.

           If session B clicks the 'Re-login' button then session A now displays
           the 'Existing Session Detected' dialog.

           If session A chooses to terminate, then session B continues
           automatically; but if session A chooses to continue then, session B
           displays the 'Existing Session Detected' dialog again and must now
           terminate.
        */

        if (LoginManager.loginManager().getLoginState() != LoginManager.LoginState.LOGGED_IN){
            /* The login has not yet been completed, so this is a new IBC instance, ie we are
               session B.

               We don't know the type of session A, so we continue this one.

               If session A is primary it won't shut down, and this dialog will
               be invoked again, at which point we terminate this session.

               If session A is primaryoverride it will shut down, and that'll be
               that.
            */
            if (!attempted) {
                attempted = true;
                Utils.logToConsole("Don't know the type of the other session, so continue this one (scenario 3)");
                if (!SwingUtils.clickButton(window, "OK") &&
                        !SwingUtils.clickButton(window, "Continue Login") &&
                        !SwingUtils.clickButton(window, "Reconnect This Session"))  {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'OK' or 'Continue Login' or 'Reconnect This Session' button wasn't found.");
                }
            } else {
                Utils.logToConsole("Other session must be primary or primary override, so end this session and let the other one proceed (scenario 4)");
                if (!SwingUtils.clickButton(window, "Cancel") && !SwingUtils.clickButton(window, "Exit Application")) {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'Cancel' or 'Exit Application' button wasn't found.");
                }
            }
        } else {
            /* The login has already been completed so we are session A. The
               case where we are Secondary has already been handled, so we must
               be either Primary or PrimaryOverride. If
               we are primary we must continue this session. If we are
               PrimaryOverride, we must allow the other session to proceed in case it
               is primary.
            */
            if (setting.equals(PRIMARY)) {
                Utils.logToConsole("Continue this session and let the other session exit (scenario 5)");
                if (!SwingUtils.clickButton(window, "OK") &&
                        !SwingUtils.clickButton(window, "Continue Login") &&
                        !SwingUtils.clickButton(window, "Reconnect This Session"))  {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'OK' or 'Continue Login' or 'Reconnect This Session' button wasn't found.");
                }
            } else if (setting.equals(PRIMARY_OVERRIDE)) {
                Utils.logToConsole("Other session may be primary, so end this session and let the other one proceed (scenario 6)");

                // ideally we'd just click the "Exit Application" button, but TWS doesn't react to
                // it properly in these circumstances, so we have to click the button and then exit the program
                if (!SwingUtils.clickButton(window, "Cancel") && !SwingUtils.clickButton(window, "Exit Application")) {
                    Utils.logError("could not handle 'Existing session detected' dialog because the 'Cancel' or 'Exit Application' button wasn't found.");
                }
            } else {
                Utils.exitWithError(ErrorCodes.INVALID_STATE, "Unexpected setting value: " + setting);
            }
        }
    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JDialog)) return false;

        return (SwingUtils.titleContains(window, "Existing session detected"));
    }
}
