// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2023 Richard L King (rlking@aultan.com)
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

import javax.swing.JDialog;

public class ConfigureResetOrderIdsTask implements ConfigurationAction {

    private final boolean resetOrderIds;
    private JDialog configDialog;

    ConfigureResetOrderIdsTask(boolean resetOrderIds) {
        this.resetOrderIds = resetOrderIds;
    }

    @Override
    public void initialise(JDialog configDialog) {
        this.configDialog = configDialog;
    }

    @Override
    public void run() {
        try {
            if (!resetOrderIds) return;
            
            Utils.logToConsole("Resetting API order ids");

            if (!SessionManager.isGateway()) {
                // NB: Gateway never displays the confirmation dialog
                ResetOrderIdConfirmationDialogHandler.orderIdResetRequestedAtStart = true;
            }

            Utils.selectApiSettings(configDialog);

            if (!SwingUtils.clickButton(configDialog, "Reset API order ID sequence")) throw new IbcException("could not find 'Reset API order ID sequence' button"); 
        } catch (IbcException e) {
            Utils.logException(e);
        }
    }
}
