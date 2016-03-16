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

class TwsListener
        implements AWTEventListener {

    private static final TwsListener _OnlyInstance = new TwsListener();
    
    private static List<WindowHandler> _WindowHandlers;

    private static String logComponents;

    private TwsListener() {
        logComponents =  Settings.getString("LogComponents", "never").toLowerCase();
        if (logComponents.equals("activate") || logComponents.equals("open") || logComponents.equals("never")) {
        } else if (logComponents.equals("yes") || logComponents.equals("true")) {
            logComponents="open";
        } else if (logComponents.equals("no") || logComponents.equals("false")) {
            logComponents="never";
        } else {
            Utils.logError("the LogComponents setting is invalid.");
        }
    }

    static TwsListener getInstance() {return _OnlyInstance; }

    static void initialise(List<WindowHandler> windowHandlers) {
        _WindowHandlers = windowHandlers;
    }

    @Override
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
            if (wh.filterEvent(window, eventID) && wh.recogniseWindow(window))  {
                wh.handleWindow(window, eventID);
                break;
            }
        }

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
        
        if ((eventID == WindowEvent.WINDOW_OPENED && logComponents.equals("open"))
            ||
            (eventID == WindowEvent.WINDOW_ACTIVATED && logComponents.equals("activate")))
        {
            Utils.logWindowComponents(window);
        }
    }
    
    static void showTradesLogWindow() {
        Utils.invokeMenuItem(MainWindowManager.getMainWindow(), new String[] {"Account", "Trade Log"});
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



