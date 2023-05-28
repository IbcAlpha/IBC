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

    private final Object futureCreationLock = new Object();
    private Future<JDialog> configDialogFuture;

    /* records the number of 'things' (including possibly the user) that
     * are currently accessing the config dialog
    */
    private int usageCount;

    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default config dialog manager");
    }

    /**
     * Records the fact that the config dialog has closed.
     */
    @Override
    public void clearConfigDialog() {
        openedByUser = false;
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

        incrementUsage();

        if (configDialog != null) {
            Utils.logToConsole("Config dialog already found");
            return configDialog;
        }

        synchronized(futureCreationLock) {
            if (configDialogFuture != null) {
                    Utils.logToConsole("Waiting for config dialog future to complete");
            } else {
                Utils.logToConsole("Creating config dialog future");
                configDialogTask = new GetConfigDialogTask();
                ExecutorService exec = Executors.newSingleThreadExecutor();
                configDialogFuture = exec.submit((Callable<JDialog>)configDialogTask);
                exec.shutdown();
            }
        }

        try {
            if (timeout < 0) {
                configDialog = configDialogFuture.get();
            } else {
                configDialog = configDialogFuture.get(timeout, unit);
            }
            Utils.logToConsole("Got config dialog from future");
            return configDialog;
        } catch (TimeoutException | InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof IbcException) {
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

    private boolean openedByUser;
    @Override
    public void setConfigDialog(JDialog window) {
        configDialog = window;
        if (configDialogTask == null) {
            // config dialog opened by user
            openedByUser = true;
        } else {
            configDialogTask.setConfigDialog(window);
            configDialogTask = null;
            configDialogFuture = null;
        }
    }

    private boolean apiConfigChangeConfirmationExpected;
    @Override
    public boolean getApiConfigChangeConfirmationExpected() {
        return apiConfigChangeConfirmationExpected;
    }

    @Override
    public void releaseConfigDialog() {
        decrementUsage();
    }

    @Override
    public void setApiConfigChangeConfirmationExpected() {
        apiConfigChangeConfirmationExpected = true;
    }

    @Override
    public void setApiConfigChangeConfirmationHandled() {
        apiConfigChangeConfirmationExpected = false;
    }

    private synchronized void incrementUsage() {
        usageCount++;
    }

    private synchronized void decrementUsage() {
        usageCount--;
        if (openedByUser) return;
        if (usageCount == 0){
            GuiDeferredExecutor.instance().execute(() -> {
                Utils.logToConsole("Configuration tasks completed");
                SwingUtils.clickButton(configDialog, "OK");
                GuiDeferredExecutor.instance().execute(() -> MainWindowManager.mainWindowManager().iconizeIfRequired());
            });
        }
    }



}
