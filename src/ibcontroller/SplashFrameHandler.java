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

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class SplashFrameHandler implements WindowHandler {

    @Override
    public boolean filterEvent(Window window, int eventId) {
        /*
         * Note that we are only interested in the closing of the gateway splash
         * frame, because that indicates that the gateway is now in a position to
         * start handling menu commands.
         * 
         * Note also that the splash frame's window title is repeatedly changed during 
         * gateway initialisation, and it's only the last title value that we use for 
         * recognising it
         */
        switch (eventId) {
            case WindowEvent.WINDOW_CLOSED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handleWindow(Window window, int eventID) {
        ConfigDialogManager.configDialogManager().setSplashScreenClosed();
    }

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame))  return false;

        return (SwingUtils.titleContains(window, "Starting application..."));
    }
    
}
