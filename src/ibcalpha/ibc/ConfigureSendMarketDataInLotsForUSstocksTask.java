/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2019 Richard L King (rlking@aultan.com)
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

import javax.swing.JCheckBox;
import javax.swing.JDialog;

public class ConfigureSendMarketDataInLotsForUSstocksTask implements ConfigurationAction{

    private final boolean sendMarketDataInLots;
    private JDialog configDialog;

    ConfigureSendMarketDataInLotsForUSstocksTask(boolean sendMarketDataInLots) {
        this.sendMarketDataInLots = sendMarketDataInLots;
    }

    @Override
    public void run() {
        try {
            Utils.logToConsole("Setting Send Market Data In Lots");

            Utils.selectApiSettings(configDialog);

            JCheckBox sendMarketDataInLotsCheckbox = SwingUtils.findCheckBox(configDialog, "Send market data in lots for US stocks for dual-mode API clients");
            if (sendMarketDataInLotsCheckbox == null) {
                // NB: we don't throw here because older TWS versions did not have this setting
                Utils.logError("could not find Send Market Data In Lots checkbox");
                return;
            }

            if (sendMarketDataInLotsCheckbox.isSelected() == sendMarketDataInLots) {
                Utils.logToConsole("Send Market Data In Lots checkbox is already set to: " + sendMarketDataInLots);
            } else {
                sendMarketDataInLotsCheckbox.setSelected(sendMarketDataInLots);
                Utils.logToConsole("Send Market Data In Lots checkbox is now set to: " + sendMarketDataInLots);
            }
        } catch (IbcException e) {
            Utils.logException(e);
        }
    }

    @Override
    public void initialise(JDialog configDialog) {
        this.configDialog = configDialog;
    }
}
