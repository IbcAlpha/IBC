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

class ConfigureAutoLogoffOrRestartTimeTask implements ConfigurationAction {
    private final String autoActionTime;
    private final String autoActionName;
    private JDialog configDialog;

    ConfigureAutoLogoffOrRestartTimeTask(String autoActionName, String autoActionTime) {
        this.autoActionName=autoActionName;
        this.autoActionTime = autoActionTime;
    }

    ConfigureAutoLogoffOrRestartTimeTask(String autoActionName, LocalTime autoActionTime) {
        this.autoActionName=autoActionName;
        this.autoActionTime = autoActionTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    @Override
    public void initialise(JDialog configDialog) {
        this.configDialog = configDialog;
    }

    @Override
    public void run() {
        try {
            Utils.logToConsole("Setting " + autoActionName  + " time");
            
            DateTimeFormatter timeFormatter12HourAmPm = DateTimeFormatter.ofPattern("hh:mm a");

            LocalTime newAutoActionTime;
            try {
                newAutoActionTime = LocalTime.parse(autoActionTime, timeFormatter12HourAmPm);
            } catch (DateTimeParseException e) {
                throw new IbcException(autoActionName + " time setting must be hh:mm AM or hh:mm PM, for example \"09:30 AM\" or \"04:00 PM\"");
            }

            Utils.selectConfigSection(configDialog, new String[] {"Lock and Exit"});
            JLabel l = SwingUtils.findLabel(configDialog, "Set Auto Log Off Time (HH:MM)");
            if (l == null) l = SwingUtils.findLabel(configDialog, "Set Auto Restart Time (HH:MM)");
            if (l == null) throw new IbcException("could not find auto logoff/restart time settings");

            Container c = javax.swing.SwingUtilities.getAncestorOfClass(Container.class, l);
            if (c == null) throw new IbcException("could not find Auto Log Off or Auto Restart section in config dialog");
            c = javax.swing.SwingUtilities.getAncestorOfClass(Container.class, c);
            if (c == null) throw new IbcException("could not find Auto Log Off or Auto Restart section in config dialog");

            JTextField tf = SwingUtils.findTextField(c, 0);
            JRadioButton autoAction = SwingUtils.findRadioButton(c, autoActionName);
            JRadioButton am = SwingUtils.findRadioButton(c, "AM");
            JRadioButton pm = SwingUtils.findRadioButton(c, "PM");
            if (tf == null || autoAction == null || am == null || pm == null) throw new IbcException("could not find auto logoff/restart time controls");

            DateTimeFormatter timeFormatter12Hour = DateTimeFormatter.ofPattern("hh:mm");
            String time = newAutoActionTime.format(timeFormatter12Hour);

            DateTimeFormatter ampmFormatter = DateTimeFormatter.ofPattern("a");
            String ampm = newAutoActionTime.format(ampmFormatter);
            
            LocalTime currentAutoActionTime = LocalTime.parse(tf.getText() + (am.isSelected() ? " AM" : " PM"), timeFormatter12HourAmPm);

            if (newAutoActionTime.equals(currentAutoActionTime) && autoAction.isSelected()) {
                Utils.logToConsole(autoActionName + " time already set to " + currentAutoActionTime.format(timeFormatter12HourAmPm));
            } else {
                tf.setText(time);
                if ("AM".equals(ampm)) {
                    am.setSelected(true);
                } else {
                    pm.setSelected(true);
                }

                if (autoAction.isSelected()) {
                    Utils.logToConsole(autoActionName + " time changed from " + 
                                        currentAutoActionTime.format(timeFormatter12HourAmPm) + 
                                        " to " + 
                                        newAutoActionTime.format(timeFormatter12HourAmPm));
                } else {
                    autoAction.setSelected(true);
                    Utils.logToConsole(autoActionName + " time set to " + 
                                        newAutoActionTime.format(timeFormatter12HourAmPm));
                }
            }
            
        } catch (IbcException e) {
            Utils.logError(e.getMessage());
        }
    }
    
}
