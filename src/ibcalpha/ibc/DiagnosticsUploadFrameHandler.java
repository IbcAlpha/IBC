// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2021 Richard L King (rlking@aultan.com)
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

class DiagnosticsUploadFrameHandler implements WindowHandler {
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(Window window, int eventID) {
        if (uploaded) {
            Utils.logError("TODO. Second upload request. Will upload diagnostics only once");
        }
        else
        {
            String accept = Settings.settings().getString("UploadIBDiagnosticsBundleIfAsked", "");
            switch(accept) {
                case "yes":
                    uploaded = true;
                    if (!SwingUtils.clickButton(window, "Send Diagnostics Bundle")) {
                        Utils.logError("could not upload diagnostics");
                    }
                    return;
                default:
                    Utils.logError("UploadIBDiagnosticsBundleIfAsked : " + accept);
                    return;
            }
        }
    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;
        return "Trader Workstation - diagnostics upload".equalsIgnoreCase(SwingUtils.getWindowTitle(window));
    }

    private boolean uploaded = false;
}
