// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2018 Richard L King (rlking@aultan.com)
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

import java.awt.Component;
import java.awt.Container;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JTextField;

class ConfigureTwsMasterClientIDTask implements ConfigurationAction{

    private final String masterClientID;
    private JDialog configDialog;

    ConfigureTwsMasterClientIDTask(String masterClientID) {
        this.masterClientID = masterClientID;
    }

    @Override
    public void run() {
        try {
            Utils.logToConsole("Performing master client ID configuration");

            Utils.selectApiSettings(configDialog);

            Component comp = SwingUtils.findComponent(configDialog, "Master API client ID");
            if (comp == null) throw new IbcException("could not find Master Client ID component");

            JTextField tf = SwingUtils.findTextField((Container)comp, 0);
            if (tf == null) throw new IbcException("could not find Master Client ID field");

            String currentMasterClientID = tf.getText();
            if (currentMasterClientID.equals(masterClientID)) {
                Utils.logToConsole("TWS Master Client ID is already set to " + tf.getText());
            } else {
                if (!SessionManager.isGateway()) {
                    JCheckBox cb = SwingUtils.findCheckBox(configDialog, "Enable ActiveX and Socket Clients");
                    if (cb == null) throw new IbcException("could not find Enable ActiveX checkbox");
                    if (cb.isSelected()) ConfigDialogManager.configDialogManager().setApiConfigChangeConfirmationExpected();
                }
                Utils.logToConsole("TWS Master Client ID was set to " + tf.getText());
                tf.setText(masterClientID);
                Utils.logToConsole("TWS Master Client ID now set to " + tf.getText());
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
