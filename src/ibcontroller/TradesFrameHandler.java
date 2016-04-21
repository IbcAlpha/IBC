// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import static ibcontroller.SwingUtils.findCheckBox;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TradesFrameHandler implements WindowHandler {
    
    boolean firstTradesWindowOpened;
    
    boolean showAllTrades;
    
    @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
            case WindowEvent.WINDOW_CLOSING:
            case WindowEvent.WINDOW_CLOSED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handleWindow(final Window window, int eventID) {
        if (!firstTradesWindowOpened) {
            showAllTrades = Settings.settings().getBoolean("ShowAllTrades", false);
        }
        if (!showAllTrades) {
            firstTradesWindowOpened = true;
            return;
        }
        if (eventID == WindowEvent.WINDOW_OPENED) {
            if (findCheckBox(window, "Sun") != null) {
                Utils.logToConsole("Setting trades log to show all trades");
                // TWS versions before 955
                SwingUtils.setCheckBoxSelected(window, "Sun", true);
                SwingUtils.setCheckBoxSelected(window, "Mon", true);
                SwingUtils.setCheckBoxSelected(window, "Tue", true);
                SwingUtils.setCheckBoxSelected(window, "Wed", true);
                SwingUtils.setCheckBoxSelected(window, "Thu", true);
                SwingUtils.setCheckBoxSelected(window, "Fri", true);
                SwingUtils.setCheckBoxSelected(window, "Sat", true);
                SwingUtils.setCheckBoxSelected(window, "All", true);

                monitorAllTradesCheckbox(window, "All");

                if (! firstTradesWindowOpened) {
                    if (Settings.settings().getBoolean("MinimizeMainWindow", false)) {
                        ((JFrame) window).setExtendedState(java.awt.Frame.ICONIFIED);
                    }
                }
            } else {
                Utils.logToConsole("Can't set trades log to show all trades with this TWS version: user must do this");
                /*
                 * For TWS 955 onwards, IB have replaced the row of daily 
                 * checkboxes with what appears visually to be a combo box:
                 * it is indeed derived from a JComboBox, but setting the
                 * selected item to 'Last 7 Days' doesn't have the desired
                 * effect.
                 * 
                 * At present I don't see a way of getting round this, but 
                 * the setting chosen by the user can now be persisted
                 * between sessions, so there is really no longer a need for
                 * 'ShowAllTrades'.
                 * 
                 */
                
                showAllTrades = false;
                ((JFrame) window).dispose();
            }

            firstTradesWindowOpened = true;

        } else if (eventID == WindowEvent.WINDOW_CLOSING) {
            Utils.logToConsole("User closing trades log");
        } else if (eventID == WindowEvent.WINDOW_CLOSED) {
            if (showAllTrades) {
                Utils.logToConsole("Trades log closed by user - recreating");
                Utils.showTradesLogWindow();
            }
        }

    }

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame))  return false;

        return (SwingUtils.titleContains(window, "Trades"));
    }

    private void monitorAllTradesCheckbox(Window window, String text) {
        final JCheckBox check = SwingUtils.findCheckBox(window, text);
        if (check != null) check.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                Utils.logToConsole("Checkbox: " + check.getText() + "; selected=" + check.isSelected());
                if (!check.isSelected()) {
                    GuiDeferredExecutor.instance().execute(new Runnable() {
                        @Override
                        public void run(){
                            Utils.logToConsole("Checkbox: " + check.getText() + "; setting selected");
                            if (!check.isSelected()) check.doClick();
                        }
                    });
                }
            }
        });
    }
}
