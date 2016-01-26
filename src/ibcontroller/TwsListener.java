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

    private static final TwsListener _OnlyInstance = new TwsListener();
    private static volatile String _IBAPIUserName;
    private static volatile String _IBAPIPassword;
    private static volatile String _FIXUserName;
    private static volatile String _FIXPassword;

    private static List<WindowHandler> _WindowHandlers;

    private static volatile JFrame _LoginFrame = null;
    private static volatile JFrame _MainWindow = null;
    private static volatile JDialog _ConfigDialog = null;
    
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

    static void initialise(String IBAPIUserName, String IBAPIPassword, String FIXUserName, String FIXPassword, List<WindowHandler> windowHandlers) {
        _IBAPIUserName = IBAPIUserName;
        _IBAPIPassword = IBAPIPassword;
        _FIXUserName = FIXUserName;
        _FIXPassword = FIXPassword;
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

    private static volatile GetConfigDialogTask _ConfigDialogTask;
    private static volatile Future<JDialog> _ConfigDialogFuture;
    
    /**
     * Records the fact that the config dialog has closed.
     */
    static void clearConfigDialog() {
        _ConfigDialog = null;
    }
    
    /**
     * Returns the Global Configuration dialog, if necessary blocking the calling thread until
     * either it is available or a specified timeout has elapsed.
     * 
     * If the Global Configuration dialog is currently open, it is returned immediately without blocking
     * the calling thread.
     * 
     * Calling this method from the Swing event dispatch thread results in an IllegalStateException
     * being thrown.
     * 
     * @param timeout
     * the length of time to wait for the Global Configuration dialog to become available. If this is
     * negative, the calling thread blocks until the dialog becomes available.
     * @param unit
     * the time units for the timeout parameter
     * @return
     * null if the timeout expires before the Global Configuration dialog becomes available; otherwise
     * the Global Configuration dialog
     * @throws IllegalStateException 
     * the method has been called from the Swing event dispatch thread
     */
    static JDialog getConfigDialog(long timeout, TimeUnit unit) throws IllegalStateException {
        /* Note that caching a config dialog doesn't work, since they seem to
         * be one-time-only. So we have to go via the menu each time this 
         * method is called (if it isn't currently open or being opened).
        */
        
        if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException();
        
        Utils.logToConsole("Getting config dialog");
        
        if (_ConfigDialog != null) return _ConfigDialog;
        
        if (_ConfigDialogFuture == null) {
            Utils.logToConsole("Creating config dialog future");
            _ConfigDialogTask = new GetConfigDialogTask();
            ExecutorService exec = Executors.newSingleThreadExecutor();
            _ConfigDialogFuture = exec.submit((Callable<JDialog>)_ConfigDialogTask);
            exec.shutdown();
        }
        
        try {
            if (timeout < 0) {
                _ConfigDialog = _ConfigDialogFuture.get();
            } else {
                _ConfigDialog = _ConfigDialogFuture.get(timeout, unit);
            }
            return _ConfigDialog;
        } catch (TimeoutException | InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof IBControllerException) {
                Utils.logError("getConfigDialog could not find " + t.getMessage());
                return null;
            }
            if (t instanceof RuntimeException) throw (RuntimeException)t;
            if (t instanceof Error) throw (Error)t;
            throw new IllegalStateException(t);
        }
    }

    /**
     * Returns the Global Configuration dialog, if necessary blocking the calling thread until
     * it is available.
     * 
     * If the Global Configuration dialog is currently open, it is returned immediately without blocking
     * the calling thread.
     * 
     * Calling this method from the Swing event dispatch thread results in an IllegalStateException
     * being thrown.
     * 
     * @return
     * the Global Configuration dialog, or null if the relevant menu entries cannot be found
     * @throws IllegalStateException 
     * the method has been called from the Swing event dispatch thread
     */
    static JDialog getConfigDialog() throws IllegalStateException {
        return getConfigDialog(-1, TimeUnit.MILLISECONDS);
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

    private static volatile GetMainWindowTask _MainWindowTask;
    private static volatile Future<JFrame> _MainWindowFuture;
    
    /**
     * Returns the main window, if necessary blocking the calling thread until
     * either it is available or a specified timeout has elapsed.
     * 
     * If the main window is currently open, it is returned immediately without blocking
     * the calling thread.
     * 
     * Calling this method from the Swing event dispatch thread results in an IllegalStateException
     * being thrown.
     * 
     * @param timeout
     * the length of time to wait for the main window to become available. If this is
     * negative, the calling thread blocks until the window becomes available.
     * @param unit
     * the time units for the timeout parameter
     * @return
     * null if the timeout expires before the main window becomes available; otherwise
     * the main window
     * @throws IllegalStateException
     * the method has been called from the Swing event dispatch thread
     */
    static JFrame getMainWindow(long timeout, TimeUnit unit) {
        if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException();
        
        if (_MainWindow != null) return _MainWindow;
        
        if (_MainWindowFuture == null) {
            _MainWindowTask = new GetMainWindowTask();
            ExecutorService exec = Executors.newSingleThreadExecutor();
            _MainWindowFuture = exec.submit((Callable<JFrame>) _MainWindowTask);
            exec.shutdown();
        }
        
        try {
            if (timeout < 0) {
                _MainWindow = _MainWindowFuture.get();
            } else {
                _MainWindow = _MainWindowFuture.get(timeout, unit);
            }
            if (_MainWindow != null) _MainWindowFuture = null;
        } catch (TimeoutException | InterruptedException e) {
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException)t;
            if (t instanceof Error) throw (Error)t;
            throw new IllegalStateException(t);
        }
        return _MainWindow;
    }

    /**
     * Returns the main window, if necessary blocking the calling thread until
     * it is available.
     * 
     * If the main window is currently open, it is returned immediately without blocking
     * the calling thread.
     * 
     * Calling this method from the Swing event dispatch thread results in an IllegalStateException
     * being thrown.
     * 
     * @return
     * the main window
     * @throws IllegalStateException 
     * the method has been called from the Swing event dispatch thread
     */
    static JFrame getMainWindow() throws IllegalStateException{
        return getMainWindow(-1, TimeUnit.MILLISECONDS);
    }
    
    static String getIBAPIPassword() {
        return _IBAPIPassword;
    }

    static String getIBAPIUserName() {
        return _IBAPIUserName;
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

    static void setConfigDialog(JDialog window) {
        _ConfigDialog = window;
        if (_ConfigDialogTask != null) {
            _ConfigDialogTask.setConfigDialog(window);
            _ConfigDialogTask = null;
            _ConfigDialogFuture = null;
        }
    }

    static void setLoginFrame(JFrame window) {
        _LoginFrame = window;
    }

    static void setMainWindow(JFrame window) {
        Utils.logToConsole("Found " + (IBController.isGateway() ? "Gateway" : "TWS") + " main window");
        _MainWindow = window;
        if (_MainWindowTask != null) _MainWindowTask.setMainWindow(window);
        if (Settings.getBoolean("MinimizeMainWindow", false)) _MainWindow.setExtendedState(java.awt.Frame.ICONIFIED);
    }
    
    static void setSplashScreenClosed() {
        if (_ConfigDialogTask != null) _ConfigDialogTask.setSplashScreenClosed();
    }
    
    static void showTradesLogWindow() {
        Utils.invokeMenuItem(getMainWindow(), new String[] {"Account", "Trade Log"});
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



