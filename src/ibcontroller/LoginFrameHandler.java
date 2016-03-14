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
import javax.swing.JComboBox;
import javax.swing.JFrame;

class LoginFrameHandler implements WindowHandler {
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(Window window, int eventID) {
        TwsListener.setLoginFrame((JFrame) window);

        try {
            if (setFields(window)) doLogin(window);
        } catch (IBControllerException e) {
            Utils.logError("could not login: could not find control: " + e.getMessage());
        }
    }

    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;

        // we check for the presence of the Login button because 
        // TWS displays a different (information-only) dialog, also 
        // entitled Login, when it's trying to reconnect
        return ((Utils.titleEquals(window, "New Login") ||
                Utils.titleEquals(window, "Login")) &&
                Utils.findButton(window, "Login") != null);
    }

    private boolean setFields(final Window window) throws IBControllerException {
        setTradingModeCombo(window);
        
        if (! Utils.setCheckBoxSelected(window,
                                            "Use/store settings on server",
                                            Settings.getBoolean("StoreSettingsOnServer", false))) return false;

        setAPICredentials(window);
        return checkNoMissingApiCredentials(window);
    }

    private boolean checkNoMissingApiCredentials(final Window window) {
            if (TwsListener.getIBAPIUserName().length() == 0) {
                Utils.findTextField(window, 0).requestFocus();
                return false;
            }
            if (TwsListener.getIBAPIPassword().length() == 0) {
                Utils.findTextField(window, 1).requestFocus();
                return false;
            }
            return true;
    }

    private void doLogin(final Window window) throws IBControllerException {
        if (Utils.findButton(window, "Login") == null) throw new IBControllerException("Login button");

        GuiDeferredExecutor.instance().execute(new Runnable() {
            @Override
            public void run() {
                Utils.clickButton(window, "Login");
            }
        });
    }
    
    private void setAPICredentials(final Window window) throws IBControllerException {
            if (! Utils.setTextField(window, 0, TwsListener.getIBAPIUserName())) throw new IBControllerException("IBAPI user name");
            if (! Utils.setTextField(window, 1, TwsListener.getIBAPIPassword())) throw new IBControllerException("IBAPI password");
    }
    
    private void setTradingModeCombo(final Window window) {
        if (Utils.findLabel(window, "Trading Mode") != null)  {
            JComboBox<?> tradingModeCombo = Utils.findComboBox(window, 0);
            
            if (tradingModeCombo != null ) {
                String tradingMode = TwsListener.getTradingMode();
                Utils.logToConsole("Setting Trading mode = " + tradingMode);
                if (tradingMode.equalsIgnoreCase(TwsListener.TRADING_MODE_LIVE)) {
                    tradingModeCombo.setSelectedItem("Live Trading");
                } else {
                    tradingModeCombo.setSelectedItem("Paper Trading");
                }
            }
        }
    }
    
}

