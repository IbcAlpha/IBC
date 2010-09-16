// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 Richard L King (rlking@aultan.com)
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
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

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
    private static volatile String _UserName;
    private static volatile String _Password;

    private static List<WindowHandler> _WindowHandlers;

    private static volatile JFrame _LoginFrame = null;
    private static volatile JFrame _MainWindow = null;

    private TwsListener() {}

    static TwsListener getInstance() {return _OnlyInstance; }

    static void initialise(String userName, String password, List<WindowHandler> windowHandlers) {
        _UserName = userName;
        _Password = password;
        _WindowHandlers = windowHandlers;
    }

    public void eventDispatched(AWTEvent event) {
        int eventID = event.getID();
        if (eventID != WindowEvent.WINDOW_OPENED &&
                eventID != WindowEvent.WINDOW_ACTIVATED) return;

        Window window =((WindowEvent) event).getWindow();
        logWindow(window, eventID);

        for (WindowHandler wh : _WindowHandlers) {
            if (wh.recogniseWindow(window)) {
                wh.handleWindow(window, eventID);
                break;
            }
        }

    }

    static JFrame getLoginFrame() {
        return _LoginFrame;
    }

    static JFrame getMainWindow() {
        return _MainWindow;
    }

    static String getPassword() {
        return _Password;
    }

    static String getUserName() {
        return _UserName;
    }

    private static void logWindow(Window window,int eventID) {
        if (window instanceof JFrame) {
            Utils.logToConsole("detected frame entitled: " + ((JFrame) window).getTitle() + "; eventID=" + eventID);
        } else if (window instanceof JDialog) {
            Utils.logToConsole("detected dialog entitled: " + ((JDialog) window).getTitle() + "; eventID=" + eventID);
        }
    }

    static void setLoginFrame(JFrame window) {
        _LoginFrame = window;
    }

    static void setMainWindow(JFrame window) {
        Utils.logToConsole("Found TWS main window");
        _MainWindow = window;
    }

}
