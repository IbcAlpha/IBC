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

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public abstract class AbstractLoginHandler implements WindowHandler {

    @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                switch (LoginManager.loginManager().getLoginState()) {
                    case LOGGED_IN:
                        return false;
                    case LOGIN_FAILED:
                        return false;
                    case LOGGING_IN:
                        return false;
                    case TWO_FA_IN_PROGRESS:
                        return false;
                    default:
                        return true;
                }
            default:
                return false;
        }
    }

    @Override
    public final void handleWindow(Window window, int eventID) {
        LoginManager.loginManager().setLoginFrame((JFrame) window);
        switch (LoginManager.loginManager().getLoginState()){
            case LOGGED_OUT:
                LoginManager.loginManager().setLoginState(LoginManager.LoginState.AWAITING_CREDENTIALS);
                try {
                    if (!initialise(window, eventID)) return;
                    if (!setFields(window, eventID)) return;
                    if (!preLogin(window, eventID)) return;
                    doLogin(window);
                } catch (IbcException e) {
                    Utils.exitWithError(ErrorCodes.ERROR_CODE_CANT_FIND_CONTROL, "could not login: could not find control: " + e.getMessage());
                }
        }
    }

    @Override
    public abstract boolean recogniseWindow(Window window);

    private void doLogin(final Window window) throws IbcException {
        JButton b = findLoginButton(window);

        GuiDeferredExecutor.instance().execute(() -> {
            LoginManager.loginManager().setLoginState(LoginManager.LoginState.LOGGING_IN);
            SwingUtils.clickButton(window, b.getText());
        });
    }

    protected abstract boolean initialise(final Window window, int eventID) throws IbcException;

    protected abstract boolean preLogin(final Window window, int eventID) throws IbcException;

    protected abstract boolean setFields(Window window, int eventID) throws IbcException;

    private JButton findLoginButton(Window window) {
        JButton b = SwingUtils.findButton(window, "Login");
        if (b == null) b = SwingUtils.findButton(window, "Log In");
        if (b == null) b = SwingUtils.findButton(window, "Paper Log In");
        return b;
    }

    protected final void setMissingCredential(final Window window, final int credentialIndex) {
        SwingUtils.findTextField(window, credentialIndex).requestFocus();
    }

    protected final void setCredential(final Window window, 
                                            final String credentialName,
                                            final int credentialIndex, 
                                            final String value) throws IbcException {
        if (! SwingUtils.setTextField(window, credentialIndex, value)) throw new IbcException(credentialName);
    }

    protected final void setTradingMode(final Window window) {
        String tradingMode = TradingModeManager.tradingModeManager().getTradingMode();

        if (SwingUtils.findToggleButton(window, "Live Trading") != null && 
                SwingUtils.findToggleButton(window, "Paper Trading") != null) {
            // TWS 974 onwards uses toggle buttons rather than a combo box
            Utils.logToConsole("Setting Trading mode = " + tradingMode);
            if (tradingMode.equalsIgnoreCase(TradingModeManager.TRADING_MODE_LIVE)) {
                SwingUtils.findToggleButton(window, "Live Trading").doClick();
            } else {
                SwingUtils.findToggleButton(window, "Paper Trading").doClick();
            }
        } else {
            JComboBox<?> tradingModeCombo;
            if (Settings.settings().getBoolean("FIX", false)) {
                tradingModeCombo = SwingUtils.findComboBox(window, 1);
            } else {
                tradingModeCombo = SwingUtils.findComboBox(window, 0);
            }

            if (tradingModeCombo != null ) {
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
