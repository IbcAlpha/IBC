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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JFrame;

class ExitSessionFrameHandler implements WindowHandler {
    
    private JFrame exitSessionFrame = null;

    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
            case WindowEvent.WINDOW_ACTIVATED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(Window window, int eventID) {
        if (eventID == WindowEvent.WINDOW_OPENED) {
            exitSessionFrame = (JFrame) window;
        }

        if (Settings.settings().getBoolean("IbAutoClosedown", false)) return;

        if (! adjustExitSessionTime(window)) {
            Utils.logError("could not change AutoLogoff time because we could not find one of the controls.");
        }
    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame))  return false;

        return (SwingUtils.titleContains(window, "Exit Session Setting")  ||
                    SwingUtils.titleContains(window, "Session-Exit-Einstellung") || 
                    exitSessionFrame == (JFrame)window);
    }

    private boolean adjustExitSessionTime(Window window) {
        Date newLogoffTime =new Date(System.currentTimeMillis() - 5 * 60 * 1000);
        Calendar cal = Calendar.getInstance();
        cal.setTime(newLogoffTime);
        String newLogoffTimeText = new SimpleDateFormat("hh:mm").format(newLogoffTime);

        SwingUtils.setTextField(window, 0, newLogoffTimeText);

        if (cal.get(Calendar.AM_PM) == Calendar.AM) {
            if (! SwingUtils.setRadioButtonSelected(window, "AM" /*, true*/)) return false;
        } else {
            if (! SwingUtils.setRadioButtonSelected(window, "PM" /*, true*/)) return false;
        }

        if (SwingUtils.clickButton(window, "Update")) {
        } else if (SwingUtils.clickButton(window, "Aktualisieren")) {
        } else {
            return false;
        }

        if (SwingUtils.clickButton(window, "Close")) {
        } else if (SwingUtils.clickButton(window, "Schliessen")) {
        } else {
            return false;
        }

        Utils.logToConsole("AutoLogoff time changed to " +
                            newLogoffTimeText +
                            (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"));
        return true;
    }

}

