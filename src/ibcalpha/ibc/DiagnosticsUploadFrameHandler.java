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
        String accept = Settings.settings().getString("DiagnosticsUploadAction", "");
        String msgIncludeScreenshot = ", screenshot off";
        switch(accept) {
            case "sendwithscreenshot":
                JCheckBox cb = SwingUtils.findCheckBox(window, "Include screenshot of entire desktop");
                if (cb == null) {
                    //don't fail (return) here. a report without a screenshot is better than no report at all
                    Utils.logError("could not find \"Include screenshot of entire desktop\" checkbox");
                } else {
                    cb.setSelected(true);
                    msgIncludeScreenshot = ", screenshot on";
                }
                //do not return here as "sendwithscreenshot" means send as well
            case "send":
                Utils.logToConsole("uploading TWS diagnostics bundle" + msgIncludeScreenshot);
                if (!SwingUtils.clickButton(window, "Send Diagnostics Bundle")) {
                    Utils.logError("could not upload diagnostics");
                }
                return; case "reject": if (!SwingUtils.clickButton(window, "Don't Send")) {
                    Utils.logError("could not upload diagnostics");
                }
                return;
            default:
                Utils.logToConsole("WARN: UploadIBDiagnosticsBundleIfAsked unknown command : " + accept);
                return;
        }
    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;
        return "Trader Workstation - diagnostics upload".equalsIgnoreCase(SwingUtils.getWindowTitle(window));
    }
}
