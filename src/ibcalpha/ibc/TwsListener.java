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

import java.awt.AWTEvent;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;

class TwsListener
        implements AWTEventListener {

    private final List<WindowHandler> windowHandlers;

    private final String logComponents;

    TwsListener (List<WindowHandler> windowHandlers) {
        this.windowHandlers = windowHandlers;

        final String logComponentsSetting =  Settings.settings().getString("LogComponents", "never").toLowerCase();
        switch (logComponentsSetting) {
            case "activate":
            case "open":
            case "openclose":
            case "never":
                logComponents = logComponentsSetting;
                break;
            case "yes":
            case "true":
                logComponents="open";
                break;
            case "no":
            case "false":
                logComponents="never";
                break;
            default:
                logComponents="never";
                Utils.logError("the LogComponents setting is invalid.");
                break;
        }
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        try {
            final int eventID = event.getID();

            final Window window;
            window = ((WindowEvent) event).getWindow();

            if (eventID == WindowEvent.WINDOW_OPENED ||
                    eventID == WindowEvent.WINDOW_ACTIVATED ||
                    eventID == WindowEvent.WINDOW_CLOSING ||
                    eventID == WindowEvent.WINDOW_CLOSED || 
                    eventID == WindowEvent.WINDOW_ICONIFIED ||
                    eventID == WindowEvent.WINDOW_DEICONIFIED ||
                    eventID == WindowEvent.WINDOW_GAINED_FOCUS ||
                    eventID == WindowEvent.WINDOW_LOST_FOCUS) {
                if (SwingUtils.titleContains(window, SwingUtils.NO_TITLE)) {
                    if (eventID == WindowEvent.WINDOW_OPENED) {
                        Utils.logRawToConsole(SwingUtils.getWindowStructure(window));
                    }
                } else if (SwingUtils.titleContains(window, "Second Factor Authentication") &&
                        ! Settings.settings().getBoolean("ReadOnlyLogin", false)) {
                    // Only handle SFA while ReadOnlyLogin mode is off.
                    
                    // Ideally we would handle the Second Factor Authentication dialog event using
                    // a WindowHandler-derived class, as for all the other dialogs. But it turns out that
                    // this does not work for TWS (though it does for Gateway), because it's impossible to
                    // recognise the dialog any time after this point. This is completely bizarre, but I
                    // suspect TWS does something unusual in an attempt to prevent anythinng interfering
                    // with the dialog. Anyone interested in the background to this discovery should look
                    // at this rather long thread in the IBC User Group:
                    //    https://groups.io/g/ibcalpha/topic/73312303#1165
                    
                    Utils.logToConsole("Second Factor Authentication dialog event: " + SwingUtils.windowEventToString(eventID));
                    if (eventID == WindowEvent.WINDOW_OPENED) {
                        Utils.logToConsole("Second Factor Authentication dialog opened");
                        LoginManager.loginManager().setLoginState(LoginManager.LoginState.TWO_FA_IN_PROGRESS);
                    } else if (eventID == WindowEvent.WINDOW_CLOSED) {
                        Utils.logToConsole("Second Factor Authentication dialog closed");
                        LoginManager.loginManager().secondFactorAuthenticationDialogClosed();
                    }
                    return;
                }
                logWindow(window, eventID);
            }

            for (WindowHandler wh : windowHandlers) {
                if (wh.recogniseWindow(window))  {
                    if (wh.filterEvent(window, eventID)) wh.handleWindow(window, eventID);
                    break;
                }
            }
        } catch (Throwable e) {
            Utils.exitWithException(ErrorCodes.ERROR_CODE_UNHANDLED_EXCEPTION, e);
        }
    }

    private void logWindow(Window window, int eventID) {
        final String event = SwingUtils.windowEventToString(eventID);

        if (window instanceof JFrame) {
            Utils.logToConsole("detected frame entitled: " + SwingUtils.getWindowTitle(window) + "; event=" + event);
        } else if (window instanceof JDialog) {
            Utils.logToConsole("detected dialog entitled: " + SwingUtils.getWindowTitle(window) + "; event=" + event);
        } else {
            Utils.logToConsole("detected window: type=" + window.getClass().getName() + "; event=" + event);
        }

        if ((eventID == WindowEvent.WINDOW_OPENED && (logComponents.equals("open") || logComponents.equals("activate")))
            ||
            (eventID == WindowEvent.WINDOW_ACTIVATED && logComponents.equals("activate"))
            ||
            ((eventID == WindowEvent.WINDOW_OPENED || eventID == WindowEvent.WINDOW_CLOSED) && logComponents.equals("openclose")))
        {
            Utils.logRawToConsole(SwingUtils.getWindowStructure(window));
        }
    }

}



