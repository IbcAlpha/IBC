// This file is part of TwsAutomater.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2018 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// TwsAutomater is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// TwsAutomater is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with TwsAutomater.  If not, see <http://www.gnu.org/licenses/>.

package TwsAutomater;

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public abstract class AbstractLoginHandler implements WindowHandler {
    
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
    public final void handleWindow(Window window, int eventID) {
        LoginManager.loginManager().setLoginFrame((JFrame) window);

        try {
            if (!initialise(window, eventID)) return;
            if (!setFields(window, eventID)) return;
            if (!preLogin(window, eventID)) return;
            doLogin(window);
        } catch (TwsAutomaterException e) {
            Utils.exitWithError(ErrorCodes.ERROR_CODE_CANT_FIND_CONTROL, "could not login: could not find control: " + e.getMessage());
        }
    }
    
    @Override
    public abstract boolean recogniseWindow(Window window);
    
    private void doLogin(final Window window) throws TwsAutomaterException {
        if (SwingUtils.findButton(window, "Login") == null) throw new TwsAutomaterException("Login button");

        GuiDeferredExecutor.instance().execute(new Runnable() {
            @Override
            public void run() {
                SwingUtils.clickButton(window, "Login");
            }
        });
    }
    
    protected abstract boolean initialise(final Window window, int eventID) throws TwsAutomaterException;
    
    protected abstract boolean preLogin(final Window window, int eventID) throws TwsAutomaterException;
    
    protected abstract boolean setFields(Window window, int eventID) throws TwsAutomaterException;

    protected final void setMissingCredential(final Window window, final int credentialIndex) {
        SwingUtils.findTextField(window, credentialIndex).requestFocus();
    }

    protected final void setCredential(final Window window, 
                                            final String credentialName,
                                            final int credentialIndex, 
                                            final String value) throws TwsAutomaterException {
        if (! SwingUtils.setTextField(window, credentialIndex, value)) throw new TwsAutomaterException(credentialName);
    }
    
    protected final void setTradingModeCombo(final Window window) {
        if (SwingUtils.findLabel(window, "Trading Mode") != null)  {
            JComboBox<?> tradingModeCombo;
            if (Settings.settings().getBoolean("FIX", false)) {
                tradingModeCombo = SwingUtils.findComboBox(window, 1);
            } else {
                tradingModeCombo = SwingUtils.findComboBox(window, 0);
            }
            
            if (tradingModeCombo != null ) {
                String tradingMode = TradingModeManager.tradingModeManager().getTradingMode();
                Utils.logToConsole("Setting Trading mode = " + tradingMode);
                if (tradingMode.equalsIgnoreCase(TradingModeManager.TRADING_MODE_LIVE)) {
                    tradingModeCombo.setSelectedItem("Live Trading");
                } else {
                    tradingModeCombo.setSelectedItem("Paper Trading");
                }
            }
        }
    }
    
}
