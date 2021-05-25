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

    private String logStructureScope;
    private String logStructureWhen;

    TwsListener (List<WindowHandler> windowHandlers) {
        this.windowHandlers = windowHandlers;
        getLogStructureParameters();
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        try {
            final int eventID = event.getID();

            final Window window;
            window = ((WindowEvent) event).getWindow();

            if (SwingUtils.titleContains(window, "Second Factor Authentication") &&
                    ! Settings.settings().getBoolean("ReadOnlyLogin", false)) {
                logWindow(window, eventID);
                logWindowStructure(window, eventID, true);
                handleSecondFactorAuthenticationDialogue(eventID);
                return;
            }

            logWindow(window, eventID);

            for (WindowHandler wh : windowHandlers) {
                if (wh.recogniseWindow(window))  {
                    logWindowStructure(window, eventID, true);
                    if (wh.filterEvent(window, eventID)) wh.handleWindow(window, eventID);
                    return;
                }
            }

            logWindowStructure(window, eventID, false);
        } catch (Throwable e) {
            Utils.exitWithException(ErrorCodes.ERROR_CODE_UNHANDLED_EXCEPTION, e);
        }
    }
    
    private void getLogStructureParameters() {
        // legacy deprecated setting overrides explicit values of LogStructureScope 
        // and LogStructureWhen
        final String logComponentsSetting = Settings.settings().getString("LogComponents", "ignore").toLowerCase();
        
        logStructureScope = getLogStructureScope(logComponentsSetting);
        logStructureWhen = getLogStructureWhen(logComponentsSetting);
    }
    
    private String getLogStructureScope(String logComponentsSetting) {
        String logStructureScope;
        if (logComponentsSetting.equals("ignore")) {
            logStructureScope = Settings.settings().getString("LogStructureScope", "known").toLowerCase();
        } else {
            logStructureScope = "all";
        }

        switch (logStructureScope) {
            case "known":
            case "unknown":
            case "untitled":
            case "all":
                break;
            default:
                Utils.logError("the LogStructureScope setting is invalid.");
                logStructureScope = "known";
        }
        
        return logStructureScope;
    }
    
    private String getLogStructureWhen(String logComponentsSetting) {
        String logStructureWhen;
        if (logComponentsSetting.equals("ignore")) {
            logStructureWhen = Settings.settings().getString("LogStructureWhen", "never").toLowerCase();
        } else {
            logStructureWhen = logComponentsSetting;
        }

        switch (logStructureWhen) {
            case "activate":
            case "open":
            case "openclose":
            case "never":
                break;
            case "yes":
            case "true":
                logStructureWhen = "open";
                break;
            case "no":
            case "false":
                logStructureWhen = "never";
                break;
            default:
                logStructureWhen = "never";
                Utils.logError("the LogStructureWhen setting is invalid.");
        }
        
        return logStructureWhen;
    }

    private void handleSecondFactorAuthenticationDialogue(final int eventID) {
        // Only handle SFA while ReadOnlyLogin mode is off.

        // Ideally we would handle the Second Factor Authentication dialog event using
        // a WindowHandler-derived class, as for all the other dialogs. But it turns out that
        // this does not work for TWS (though it does for Gateway), because it's impossible to
        // recognise the dialog any time after this point. This is completely bizarre, but I
        // suspect TWS does something unusual in an attempt to prevent anything interfering
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
    }

    private void logWindow(Window window, int eventID) {
        final String event = SwingUtils.windowEventToString(eventID);
        final String windowTitle;
        if (window instanceof JFrame) {
            windowTitle = SwingUtils.getWindowTitle(window);
            Utils.logToConsole("detected frame entitled: " + windowTitle + "; event=" + event);
        } else if (window instanceof JDialog) {
            windowTitle = SwingUtils.getWindowTitle(window);
            Utils.logToConsole("detected dialog entitled: " + windowTitle + "; event=" + event);
        } else {
            windowTitle = window.getClass().getName();
            Utils.logToConsole("detected window: type=" + windowTitle + "; event=" + event);
        }
    }

    private void logWindowStructure(Window window, int eventID, boolean windowKnown) {
        if (logStructureScope.equals("known") && !windowKnown) {
            return;
        } else if (logStructureScope.equals("unknown") && windowKnown) {
            return;
        } else if (logStructureScope.equals("untitled") && 
                    !SwingUtils.getWindowTitle(window).equals(SwingUtils.NO_TITLE)) {
            return;
        }

        if ((eventID == WindowEvent.WINDOW_OPENED && (logStructureWhen.equals("open") || logStructureWhen.equals("activate")))
            ||
            (eventID == WindowEvent.WINDOW_ACTIVATED && logStructureWhen.equals("activate"))
            ||
            ((eventID == WindowEvent.WINDOW_OPENED || eventID == WindowEvent.WINDOW_CLOSED) && logStructureWhen.equals("openclose")))
        {
            Utils.logRawToConsole(SwingUtils.getWindowStructure(window));
        }
    }
}



