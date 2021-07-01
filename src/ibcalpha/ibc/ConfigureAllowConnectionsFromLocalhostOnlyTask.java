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

import javax.swing.*;

public class ConfigureAllowConnectionsFromLocalhostOnlyTask implements ConfigurationAction{

    private final boolean allowConnectionsFromLocalhostOnly;
    private JDialog configDialog;

    ConfigureAllowConnectionsFromLocalhostOnlyTask(boolean allowConnectionsFromLocalhostOnly) {
        this.allowConnectionsFromLocalhostOnly = allowConnectionsFromLocalhostOnly;
    }

    @Override
    public void run() {
        try {
            Utils.selectApiSettings(configDialog);

            JCheckBox checkBox = SwingUtils.findCheckBox(configDialog, "Allow connections from localhost only");
            if (checkBox == null) {
                // NB: we don't throw here because older TWS versions did not have this setting
                Utils.logError("could not find 'Allow connections from localhost only' checkbox");
                return;
            }
            
            if (checkBox.isSelected() == allowConnectionsFromLocalhostOnly) {
                Utils.logToConsole("'Allow connections from localhost only' checkbox is already set to: " + allowConnectionsFromLocalhostOnly);
            } else {
                checkBox.setSelected(allowConnectionsFromLocalhostOnly);
                Utils.logToConsole("'Allow connections from localhost only' checkbox is now set to: " + allowConnectionsFromLocalhostOnly);
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