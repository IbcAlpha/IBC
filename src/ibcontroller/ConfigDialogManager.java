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

public class ConfigDialogManager {
    
    private static volatile JDialog _ConfigDialog = null;
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

    static void setConfigDialog(JDialog window) {
        _ConfigDialog = window;
        if (_ConfigDialogTask != null) {
            _ConfigDialogTask.setConfigDialog(window);
            _ConfigDialogTask = null;
            _ConfigDialogFuture = null;
        }
    }

    static void setSplashScreenClosed() {
        if (_ConfigDialogTask != null) _ConfigDialogTask.setSplashScreenClosed();
    }
    
}
