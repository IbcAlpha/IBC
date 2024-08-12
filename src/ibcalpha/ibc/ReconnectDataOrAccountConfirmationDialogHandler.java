// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2024 Richard L King (rlking@aultan.com)
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

import static ibcalpha.ibc.SwingUtils.getWindowTitle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

public class ReconnectDataOrAccountConfirmationDialogHandler implements WindowHandler {

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
        if (! SwingUtils.clickButton(window, "OK")) {
            Utils.logError("could not dismiss " + getWindowTitle(window) + " because we could not find the OK button");
        }
    }

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JDialog)) return false;

        return ((SwingUtils.titleContains(window, "IBKR Trader Workstation") 
                ||
                SwingUtils.titleContains(window, "IBKR Gateway"))
                && 
                (SwingUtils.findLabel(window, "Are you sure you want to execute \"simulate") != null)
                );
    }
    
}
