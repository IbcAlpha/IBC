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

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class DefaultMainWindowManager extends MainWindowManager {

    private volatile JFrame mainWindow = null;

    private volatile GetMainWindowTask mainWindowTask;

    private final Object futureCreationLock = new Object();
    private Future<JFrame> mainWindowFuture;

    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default main window manager");
    }

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
    @Override
    public JFrame getMainWindow(long timeout, TimeUnit unit) {
        if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException();

        Utils.logToConsole("Getting main window");

        if (mainWindow != null) {
            Utils.logToConsole("Main window already found");
            return mainWindow;
        }

        synchronized(futureCreationLock) {
            if (mainWindowFuture != null) {
                    Utils.logToConsole("Waiting for main window future to complete");
            } else {
                Utils.logToConsole("Creating main window future");
                mainWindowTask = new GetMainWindowTask();
                ExecutorService exec = Executors.newSingleThreadExecutor();
                mainWindowFuture = exec.submit((Callable<JFrame>) mainWindowTask);
                exec.shutdown();
            }
        }

        try {
            if (timeout < 0) {
                mainWindow = mainWindowFuture.get();
            } else {
                mainWindow = mainWindowFuture.get(timeout, unit);
            }
            Utils.logToConsole("Got main window from future");
            return mainWindow;
        } catch (TimeoutException | InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException)t;
            if (t instanceof Error) throw (Error)t;
            throw new IllegalStateException(t);
        }
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
    @Override
    public JFrame getMainWindow() throws IllegalStateException{
        return getMainWindow(-1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setMainWindow(JFrame window) {
        Utils.logToConsole("Found " + (SessionManager.isGateway() ? "Gateway" : "TWS") + " main window");
        mainWindow = window;

        // For TWS, the main window being opened indicates that login is complete. This is not the case
        // for the Gateway, because the main window is created right at the start, but the splash frame
        // being closed indicates that login is complete (see the SplashFrameHandler).
        if (! SessionManager.isGateway()) LoginManager.loginManager().setLoginState(LoginManager.LoginState.LOGGED_IN);

        if (mainWindowTask != null) mainWindowTask.setMainWindow(window);
        mainWindowTask = null;
        mainWindowFuture = null;

        iconizeIfRequired();

        mainWindow.addWindowStateListener(listener);
    }

    private final WindowStateListener listener = new WindowStateListener() {
        @Override
        public void windowStateChanged(WindowEvent e) {
            int state = e.getNewState();
            if (((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)) {
                if ((Calendar.getInstance().getTimeInMillis() - lastMinimizeTime) < 2000) {
                    iconizeIfRequired();
                } else {
                    mainWindow.removeWindowStateListener(listener);
                }
            }
        }
    };

    private Long lastMinimizeTime;
    @Override
    public void iconizeIfRequired() {
        if (Settings.settings().getBoolean("MinimizeMainWindow", false)) {
            Utils.logToConsole("Minimizing main window");
            mainWindow.setExtendedState(java.awt.Frame.ICONIFIED);
        }
        lastMinimizeTime = Calendar.getInstance().getTimeInMillis();
    }

}
