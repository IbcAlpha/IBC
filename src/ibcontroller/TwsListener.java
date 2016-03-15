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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

class TwsListener
        implements AWTEventListener {

    static final String TRADING_MODE_LIVE = "live";
    static final String TRADING_MODE_PAPER = "paper";
    
    private static final TwsListener _OnlyInstance = new TwsListener();
    
    private static volatile String _TradingMode;

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

    static void initialise(String tradingMode, List<WindowHandler> windowHandlers) {
        _TradingMode = tradingMode;
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

    static String getTradingMode() {
        return _TradingMode;
    }
    
    static boolean _ApiConfigChangeConfirmationExpected;
    
    static boolean getApiConfigChangeConfirmationExpected() {
        return _ApiConfigChangeConfirmationExpected;
    }

    static void setApiConfigChangeConfirmationExpected(boolean yesOrNo) {
        _ApiConfigChangeConfirmationExpected = yesOrNo;
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
    
    /**
     * Selects the specified section in the Global Configuration dialog.
     * @param configDialog
     * the Global Configuration dialog
     * @param path
     * the path to the required configuration section in the Global Configuration dialog
     * @return
     * true if the specified section can be found; otherwise false
     * @throws IBControllerException
     * a UI component could not be found
     * @throws IllegalStateException
     * the method has not been called on the SWing event dispatch thread
     */
    static boolean selectConfigSection(final JDialog configDialog, final String[] path) throws IBControllerException, IllegalStateException {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("selectConfigSection must be run on the event dispatch thread");
        
        JTree configTree = Utils.findTree(configDialog);
        if (configTree == null) throw new IBControllerException("could not find the config tree in the Global Configuration dialog");

        Object node = configTree.getModel().getRoot();
        TreePath tp = new TreePath(node);

        for (String pathElement: path) {
            node = Utils.findChildNode(configTree.getModel(), node, pathElement);
            if (node == null) return false;
            tp = tp.pathByAddingChild(node);
        }

        configTree.setExpandsSelectedPaths(true);
        configTree.setSelectionPath(tp);
        return true;
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



