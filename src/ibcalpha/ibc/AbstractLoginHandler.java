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
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public abstract class AbstractLoginHandler implements WindowHandler {

    @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                Utils.logToConsole("Login dialog WINDOW_OPENED: LoginState is " + LoginManager.loginManager().getLoginState().toString());
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
        if (LoginManager.loginManager().getLoginHandler() == null) LoginManager.loginManager().setLoginHandler(this);
        LoginManager.loginManager().setLoginFrame((JFrame) window);
        switch (LoginManager.loginManager().getLoginState()){
            case LOGGED_OUT:
                // don't initiate login if we're auto-restarting
                if (! LoginManager.loginManager().getIsRestart()) initiateLogin(window);
        }
    }

    @Override
    public abstract boolean recogniseWindow(Window window);
    
    private static int loginAttemptNumber = 0;
    int currentLoginAttemptNumber() {
        return loginAttemptNumber;
    }
    
    void initiateLogin(Window window) {
        LoginManager.loginManager().setLoginState(LoginManager.LoginState.AWAITING_CREDENTIALS);
        try {
            if (!initialise(window, WindowEvent.WINDOW_OPENED)) return;
            if (!setFields(window, WindowEvent.WINDOW_OPENED)) return;
            if (!preLogin(window, WindowEvent.WINDOW_OPENED)) return;

            Utils.logToConsole("Login attempt: " + ++loginAttemptNumber);
            doLogin(window);
        } catch (IbcException e) {
            Utils.exitWithError(ErrorCodes.ERROR_CODE_CANT_FIND_CONTROL, "could not login: could not find control: " + e.getMessage());
        }
    }

    private void doLogin(final Window window) throws IbcException {
        final boolean readOnlyLoginRequired = LoginManager.loginManager().readonlyLoginRequired();
        
        // this JLabel is only present for the 1016+ versions
        final JLabel initialTitleLabel = SwingUtils.findLabel(window, "LOGIN");
        
        GuiDeferredExecutor.instance().execute(() -> {
            final JButton loginButton = findLoginButton(window);
            LoginManager.loginManager().setLoginState(LoginManager.LoginState.LOGGING_IN);
            SwingUtils.clickButton(loginButton);
        });

        if (readOnlyLoginRequired && initialTitleLabel != null) {
            // Starting with TWS 1016, there is no longer a separate Second Factor
            // Authentication dialog. Instead, TWS replaces the Login frame's controls
            // with the controls that used to be in the 2FA dialog (so the Login frame
            // effectively becomes the 2FA frame). This doesn't generate any events
            // that IBC normally handles, so it goes undetected, and thus IBC doesn't
            // know when to click the 'Enter Read Only' button. 
            //
            // To avoid this problem, we make a periodic check that the JLabel that
            // initially contained "LOGIN" has changed to "SECOND FACTOR AUTHENTICATION":
            // when this happens, we can pass the window to the SecondFactorAuthenticationDialogHandler
            // to be actioned.
            //
            // (Note that if we don't want readonly login and the 2FA prompt is handled at
            // the IBKR Mobile app, the event generated when the Login frame closes is
            // actually detected by the SecndFactorAuthenticationDialogHandler, so we
            // don't need to do anything special for that.)

            Utils.logToConsole("Waiting for Login frame to become SecondFactorAuthenticationDialog");
            MyScheduledExecutorService.getInstance().schedule(
                    () -> {
                        checkChangeToSecondFactorAuthenticationDialog(window);
                    }, 
                    200, TimeUnit.MILLISECONDS);
        }
    }

    private void checkChangeToSecondFactorAuthenticationDialog(Window window) {
        JLabel currentTitleLabel = SwingUtils.findLabel(window, "SECOND FACTOR AUTHENTICATION");
        if (currentTitleLabel != null) {
            // the login frame has now become the 2FA dialog, so invoke the 
            // handler for that as if it had just been opened
            Utils.logToConsole("Login frame has now become SecondFactorAuthenticationDialog");
            TwsListener.logWindow(window, WindowEvent.WINDOW_OPENED);
            TwsListener.logWindowStructure(window, WindowEvent.WINDOW_OPENED, true);
            SecondFactorAuthenticationDialogHandler.getInstance().handleWindow(window, WindowEvent.WINDOW_OPENED);
        } else {
            MyScheduledExecutorService.getInstance().schedule(
                    () -> {
                        checkChangeToSecondFactorAuthenticationDialog(window);
                    }, 
                    200, TimeUnit.MILLISECONDS);
        }
    }
    
    protected abstract boolean initialise(final Window window, int eventID) throws IbcException;

    protected abstract boolean preLogin(final Window window, int eventID) throws IbcException;

    protected abstract boolean setFields(Window window, int eventID) throws IbcException;

    private JButton findLoginButton(final Window window) {
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
