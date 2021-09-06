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

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

public class BidAskLastSizeDisplayUpdateDialogHandler implements WindowHandler {

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
        String accept = Settings.settings().getString("AcceptBidAskLastSizeDisplayUpdateNotification", "ignore");
        
        switch(accept) {
            case "ignore":
                return;
            case "accept":
                JCheckBox cb = (SwingUtils.findCheckBox(window, "Don't display this message again."));
                if (cb == null) {
                    Utils.logError("could not set 'Don't display this message again': checkbox not found");
                }
                cb.setSelected(true);
                break;
            case "defer":
                break;
            default:
                Utils.logError("AcceptBidAskLastSizeDisplayUpdateNotification setting is invalid: " + accept);
                return;
        }

        if (! SwingUtils.clickButton(window, "I understand - display market data")) {
            Utils.logError("could not dismiss AcceptBidAskLastSizeDisplayUpdateNotification - button not found");
        }

        String sendMarketDataInLots = Settings.settings().getString("SendMarketDataInLotsForUSstocks", "");
        if (!sendMarketDataInLots.equals("")) {
            (new ConfigurationTask(new ConfigureSendMarketDataInLotsForUSstocksTask(Settings.settings().getBoolean("SendMarketDataInLotsForUSstocks", true)))).executeAsync();
        }
    }

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JDialog)) return false;
        return (SwingUtils.findTextPane(window, "Bid, Ask and Last Size Display Update") != null );
    }
    
}
