// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2015 Richard L King (rlking@aultan.com)
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
import javax.swing.JDialog;

public class ReloginDialogHandler implements WindowHandler {

    @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handleWindow(Window window, int eventID) {
        String setting = Settings.settings().getString("ExistingSessionDetectedAction", "manual");
        if (setting.equalsIgnoreCase("primary")) {
            Utils.logToConsole("Re-login because this is the primary session");
            if (!SwingUtils.clickButton(window, "Re-login"))  {
                Utils.logError("could not handle 'Re-login is required' dialog because the 'Re-login' button wasn't found.");
            }
        } else if (setting.equalsIgnoreCase("secondary")) {
            Utils.logToConsole("Don't re-login because this is a secondary session");
            if (!SwingUtils.clickButton(window, "Exit Application"))  {
                Utils.logError("could not handle 'Re-login is required' dialog because the 'Exit Application' button wasn't found.");
            }
        } else if (setting.equalsIgnoreCase("manual")) {
            // NB: arguably we should handle re-login automatically here, but in 
            // practice TWS seems to get itself into a funny state and doesn't display 
            // the 'Existing session detected' dialog (maybe because IBController responds
            // too quickly? Who knows...)
            Utils.logToConsole("Let user choose whether to re-login");
        } else {
            Utils.logError("could not handle 'Re-login is required' dialog because the ExistingSessionDetectedAction setting is invalid.");
        }
}

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JDialog)) return false;
        return (SwingUtils.titleContains(window, "Re-login is required"));
    }
    
}
