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

import java.awt.AWTEvent;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

class TwsListener
        implements AWTEventListener {

    private static final TwsListener _OnlyInstance = new TwsListener();
    private static volatile String _IBAPIUserName;
    private static volatile String _IBAPIPassword;
    private static volatile String _FIXUserName;
    private static volatile String _FIXPassword;

    private static List<WindowHandler> _WindowHandlers;

    private static volatile JFrame _LoginFrame = null;
    private static volatile JFrame _MainWindow = null;
    private static volatile JDialog _ConfigDialog = null;

    private TwsListener() {}

    static TwsListener getInstance() {return _OnlyInstance; }

    static void initialise(String IBAPIUserName, String IBAPIPassword, String FIXUserName, String FIXPassword, List<WindowHandler> windowHandlers) {
        _IBAPIUserName = IBAPIUserName;
        _IBAPIPassword = IBAPIPassword;
        _FIXUserName = FIXUserName;
        _FIXPassword = FIXPassword;
        _WindowHandlers = windowHandlers;
    }

    public void eventDispatched(AWTEvent event) {
        int eventID = event.getID();

        Window window =((WindowEvent) event).getWindow();

        if (eventID == WindowEvent.WINDOW_OPENED ||
                eventID == WindowEvent.WINDOW_ACTIVATED ||
                eventID == WindowEvent.WINDOW_CLOSING ||
                eventID == WindowEvent.WINDOW_CLOSED) {
            logWindow(window, eventID);
        }

        for (WindowHandler wh : _WindowHandlers) {
            if (wh.recogniseWindow(window) && wh.filterEvent(window, eventID))  {
                wh.handleWindow(window, eventID);
                break;
            }
        }

    }

    static JDialog getConfigDialog() {
        return _ConfigDialog;
    }

    static String getFIXPassword() {
        return _FIXPassword;
    }

    static String getFIXUserName() {
        return _FIXUserName;
    }

    static JFrame getLoginFrame() {
        return _LoginFrame;
    }

    static JFrame getMainWindow() {
        return _MainWindow;
    }

    static String getIBAPIPassword() {
        return _IBAPIPassword;
    }

    static String getIBAPIUserName() {
        return _IBAPIUserName;
    }

    private static void logWindow(Window window,int eventID) {
        String event = windowEventToString(eventID);

        if (window instanceof JFrame) {
            Utils.logToConsole("detected frame entitled: " + ((JFrame) window).getTitle() + "; event=" + event);
        } else if (window instanceof JDialog) {
            Utils.logToConsole("detected dialog entitled: " + ((JDialog) window).getTitle() + "; event=" + event);
        } else {
            Utils.logToConsole("detected window: type=" + window.getClass().getName() + "; event=" + event);
        }
        if (eventID == WindowEvent.WINDOW_OPENED && Settings.getBoolean("LogComponents", false)) Utils.logWindowComponents(window);
    }

    static void setConfigDialog(JDialog window) {
        _ConfigDialog = window;
    }
    
    static void setLoginFrame(JFrame window) {
        _LoginFrame = window;
    }

    static void setMainWindow(JFrame window) {
        Utils.logToConsole("Found TWS main window");
        _MainWindow = window;
    }
    
    static void showTradesLogWindow() {
        final JMenuItem jmi = Utils.findMenuItem(_MainWindow, new String[] {"Account", "Trade Log"});
        if (jmi != null) {
                Utils.logToConsole("Showing trades log window");
                jmi.doClick();
        } else {
            Utils.err.println("IBControllerServer: could not find Account > Trade Log menu");
        }
    }

    static String windowEventToString(int eventID) {
        switch (eventID) { 
            case WindowEvent.WINDOW_ACTIVATED:
                return "Activated";
            case WindowEvent.WINDOW_CLOSED:
                return "Closed";
            case WindowEvent.WINDOW_CLOSING:
                return "Closing";
            case WindowEvent.WINDOW_DEACTIVATED:
                return "Deactivated";
            case WindowEvent.WINDOW_DEICONIFIED:
                return "Deiconfied";
            case WindowEvent.WINDOW_GAINED_FOCUS:
                return "Focused";
            case WindowEvent.WINDOW_ICONIFIED:
                return "Iconified";
            case WindowEvent.WINDOW_LOST_FOCUS:
                return "Lost focus";
            case WindowEvent.WINDOW_OPENED:
                return "Opened";
            case WindowEvent.WINDOW_STATE_CHANGED:
                return "State changed";
            default:
                return "???";
        }
    }

}
