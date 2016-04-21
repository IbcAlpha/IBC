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

    private final List<WindowHandler> windowHandlers;

    private final String logComponents;

    TwsListener (List<WindowHandler> windowHandlers) {
        this.windowHandlers = windowHandlers;

        String logComponentsSetting =  Settings.settings().getString("LogComponents", "never").toLowerCase();
        switch (logComponentsSetting) {
            case "activate":
            case "open":
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
        int eventID = event.getID();

        Window window =((WindowEvent) event).getWindow();

        if (eventID == WindowEvent.WINDOW_OPENED ||
                eventID == WindowEvent.WINDOW_ACTIVATED ||
                eventID == WindowEvent.WINDOW_CLOSING ||
                eventID == WindowEvent.WINDOW_CLOSED) {
            logWindow(window, eventID);
        }

        for (WindowHandler wh : windowHandlers) {
            if (wh.filterEvent(window, eventID) && wh.recogniseWindow(window))  {
                wh.handleWindow(window, eventID);
                break;
            }
        }

    }

    private void logWindow(Window window, int eventID) {
        String event = SwingUtils.windowEventToString(eventID);

        if (window instanceof JFrame) {
            Utils.logToConsole("detected frame entitled: " + ((JFrame) window).getTitle() + "; event=" + event);
        } else if (window instanceof JDialog) {
            Utils.logToConsole("detected dialog entitled: " + ((JDialog) window).getTitle() + "; event=" + event);
        } else {
            Utils.logToConsole("detected window: type=" + window.getClass().getName() + "; event=" + event);
        }
        
        if ((eventID == WindowEvent.WINDOW_OPENED && (logComponents.equals("open") || logComponents.equals("activate")))
            ||
            (eventID == WindowEvent.WINDOW_ACTIVATED && logComponents.equals("activate")))
        {
            Utils.logRawToConsole(SwingUtils.getWindowStructure(window));
        }
    }
    
}



