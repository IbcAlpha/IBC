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
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class DefaultConfigDialogManager extends ConfigDialogManager {
    
    private volatile JDialog configDialog = null;
    private volatile GetConfigDialogTask configDialogTask;
    private volatile Future<JDialog> configDialogFuture;
    
    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default config dialog manager");
    }

    /**
     * Records the fact that the config dialog has closed.
     */
    @Override
    public void clearConfigDialog() {
        configDialog = null;
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
    @Override
    public JDialog getConfigDialog(long timeout, TimeUnit unit) throws IllegalStateException {
        /* Note that caching a config dialog doesn't work, since they seem to
         * be one-time-only. So we have to go via the menu each time this 
         * method is called (if it isn't currently open or being opened).
        */
        
        if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException();
        
        Utils.logToConsole("Getting config dialog");
        
        if (configDialog != null) return configDialog;
        
        if (configDialogFuture == null) {
            Utils.logToConsole("Creating config dialog future");
            configDialogTask = new GetConfigDialogTask(MainWindowManager.mainWindowManager().isGateway());
            ExecutorService exec = Executors.newSingleThreadExecutor();
            configDialogFuture = exec.submit((Callable<JDialog>)configDialogTask);
            exec.shutdown();
        }
        
        try {
            if (timeout < 0) {
                configDialog = configDialogFuture.get();
            } else {
                configDialog = configDialogFuture.get(timeout, unit);
            }
            return configDialog;
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
    @Override
    public JDialog getConfigDialog() throws IllegalStateException {
        return getConfigDialog(-1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setConfigDialog(JDialog window) {
        configDialog = window;
        if (configDialogTask != null) {
            configDialogTask.setConfigDialog(window);
            configDialogTask = null;
            configDialogFuture = null;
        }
    }

    @Override
    public void setSplashScreenClosed() {
        if (configDialogTask != null) configDialogTask.setSplashScreenClosed();
    }
    

    private boolean apiConfigChangeConfirmationExpected;
    
    @Override
    public boolean getApiConfigChangeConfirmationExpected() {
        return apiConfigChangeConfirmationExpected;
    }

    @Override
    public void setApiConfigChangeConfirmationExpected(boolean yesOrNo) {
        apiConfigChangeConfirmationExpected = yesOrNo;
    }

}
