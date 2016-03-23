// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2016 Richard L King (rlking@aultan.com)
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
    
    public DefaultMainWindowManager() {
        this.isGateway = false;
        message = "parameterless constructor (isGateway = false assumed)";
    }
    
    public DefaultMainWindowManager(boolean isGateway) {
        this.isGateway = isGateway;
        message = "constructor parameter isGateway=" + isGateway;
    }
    
    private volatile JFrame mainWindow = null;
    
    private volatile GetMainWindowTask mainWindowTask;
    private volatile Future<JFrame> mainWindowFuture;
    
    private final boolean isGateway;
    private final String message;
    
    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default config dialog manager: " + message);
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
        
        if (mainWindow != null) return mainWindow;
        
        if (mainWindowFuture == null) {
            mainWindowTask = new GetMainWindowTask();
            ExecutorService exec = Executors.newSingleThreadExecutor();
            mainWindowFuture = exec.submit((Callable<JFrame>) mainWindowTask);
            exec.shutdown();
        }
        
        try {
            if (timeout < 0) {
                mainWindow = mainWindowFuture.get();
            } else {
                mainWindow = mainWindowFuture.get(timeout, unit);
            }
            if (mainWindow != null) mainWindowFuture = null;
        } catch (TimeoutException | InterruptedException e) {
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException)t;
            if (t instanceof Error) throw (Error)t;
            throw new IllegalStateException(t);
        }
        return mainWindow;
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
    public boolean isGateway() {
        return this.isGateway;
    };
    
    @Override
    public void setMainWindow(JFrame window) {
        Utils.logToConsole("Found " + (isGateway ? "Gateway" : "TWS") + " main window");
        mainWindow = window;
        if (mainWindowTask != null) mainWindowTask.setMainWindow(window);
        if (Settings.settings().getBoolean("MinimizeMainWindow", false)) mainWindow.setExtendedState(java.awt.Frame.ICONIFIED);
    }
    
}
