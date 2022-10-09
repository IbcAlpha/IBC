// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2022 Richard L King (rlking@aultan.com)
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

import java.awt.Container;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

class ConfigureAutoLogoffTimeTask implements ConfigurationAction {
    private final String autoLogoffTime;
    private JDialog configDialog;

    ConfigureAutoLogoffTimeTask(String autoLogoffTime) {
        this.autoLogoffTime = autoLogoffTime;
    }

    @Override
    public void initialise(JDialog configDialog) {
        this.configDialog = configDialog;
    }

    @Override
    public void run() {
        try {
            Utils.logToConsole("Setting AutoLogoffTime");
            
            DateTimeFormatter timeFormatter12HourAmPm = DateTimeFormatter.ofPattern("KK:mm a");

            LocalTime newAutoLogoffTime;
            try {
                newAutoLogoffTime = LocalTime.parse(autoLogoffTime, timeFormatter12HourAmPm);
            } catch (DateTimeParseException e) {
                throw new IbcException("AutoLogoffTime setting must be hh:mm AM or hh:mm PM, for example \"09:30 AM\" or \"04:00 PM\"");
            }

            Utils.selectConfigSection(configDialog, new String[] {"Lock and Exit"});
            JLabel l = SwingUtils.findLabel(configDialog, "Set Auto Log Off Time (HH:MM)");
            if (l == null) throw new IbcException("could not find auto logoff time settings");

            Container c = javax.swing.SwingUtilities.getAncestorOfClass(Container.class, l);
            if (c == null) throw new IbcException("could not find 'Set Auto Log Off Time (HH:MM)' section in config dialog");

            JTextField tf = SwingUtils.findTextField(c, 0);
            JRadioButton am = SwingUtils.findRadioButton(c, "AM");
            JRadioButton pm = SwingUtils.findRadioButton(c, "PM");
            if (tf == null || am == null || pm == null) throw new IbcException("could not find auto logoff time controls");

            DateTimeFormatter timeFormatter12Hour = DateTimeFormatter.ofPattern("KK:mm");
            String time = newAutoLogoffTime.format(timeFormatter12Hour);

            DateTimeFormatter ampmFormatter = DateTimeFormatter.ofPattern("a");
            String ampm = newAutoLogoffTime.format(ampmFormatter);
            
            LocalTime currentAutoLogoffTime = LocalTime.parse(tf.getText() + (am.isSelected() ? " AM" : " PM"), timeFormatter12HourAmPm);

            if (newAutoLogoffTime.equals(currentAutoLogoffTime)) {
                Utils.logToConsole("Auto logoff time already set to " + currentAutoLogoffTime.format(timeFormatter12HourAmPm));
            } else {
                tf.setText(time);
                if ("AM".equals(ampm)) {
                    am.setSelected(true);
                } else {
                    pm.setSelected(true);
                }

                Utils.logToConsole("Auto logoff time changed from " + 
                                    currentAutoLogoffTime.format(timeFormatter12HourAmPm) + 
                                    " to " + 
                                    newAutoLogoffTime.format(timeFormatter12HourAmPm));
            }
            
        } catch (IbcException e) {
            Utils.logError(e.getMessage());
        }
    }
    
}
