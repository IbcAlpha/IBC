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

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TradesFrameHandler implements WindowHandler {
    
    boolean firstTradesWindowOpened;
    
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

    public void handleWindow(final Window window, int eventID) {
        if (!Settings.getBoolean("ShowAllTrades", false)) return;
        if (eventID == WindowEvent.WINDOW_OPENED) {
            Utils.logToConsole("Setting trades log to show all trades");
            Utils.setCheckBoxSelected(window, "Sun", true);
            Utils.setCheckBoxSelected(window, "Mon", true);
            Utils.setCheckBoxSelected(window, "Tue", true);
            Utils.setCheckBoxSelected(window, "Wed", true);
            Utils.setCheckBoxSelected(window, "Thu", true);
            Utils.setCheckBoxSelected(window, "Fri", true);
            Utils.setCheckBoxSelected(window, "Sat", true);
            Utils.setCheckBoxSelected(window, "All", true);
            
            monitorAllTradesCheckbox(window, "All");
            
            if (! firstTradesWindowOpened) {
                firstTradesWindowOpened = true;
                if (Settings.getBoolean("MinimizeMainWindow", false)) {
                    ((JFrame) window).setExtendedState(java.awt.Frame.ICONIFIED);
                }
            }
        } else if (eventID == WindowEvent.WINDOW_CLOSING) {
            Utils.logToConsole("User closing trades log");
        } else if (eventID == WindowEvent.WINDOW_CLOSED) {
            Utils.logToConsole("Trades log closed by user - recreating");
            (new ThreadPerTaskExecutor()).execute(new Runnable () {
                @Override public void run() {TwsListener.showTradesLogWindow();}
            });
        }

    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame))  return false;

        return (Utils.titleContains(window, "Trades"));
    }

    private void monitorAllTradesCheckbox(Window window, String text) {
        final JCheckBox check = Utils.findCheckBox(window, text);
        if (check != null) check.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                Utils.logToConsole("Checkbox: " + check.getText() + "; selected=" + check.isSelected());
                if (!check.isSelected()) {
                    GuiDeferredExecutor.instance().execute(new Runnable() {
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
