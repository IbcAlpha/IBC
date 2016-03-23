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

import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public abstract class MainWindowManager {

    private static MainWindowManager _mainWindowManager;

    static {
        _mainWindowManager = new DefaultMainWindowManager();
    }
    
    public static void initialise(MainWindowManager mainWindowManager){
        if (mainWindowManager == null) throw new IllegalArgumentException("mainWindowManager");
        _mainWindowManager = mainWindowManager;
    }
    
    public static void setDefault() {
        _mainWindowManager = new DefaultMainWindowManager();
    }
    
    public static MainWindowManager mainWindowManager() {
        return _mainWindowManager;
    }
    
    public abstract void logDiagnosticMessage();
    

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
    public abstract JFrame getMainWindow(long timeout, TimeUnit unit);

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
    public abstract JFrame getMainWindow() throws IllegalStateException;
    
    public abstract boolean isGateway();

    public abstract void setMainWindow(JFrame window);
    
}
