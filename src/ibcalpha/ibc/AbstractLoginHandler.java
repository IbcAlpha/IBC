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
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
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
                        return true;
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
                if (SessionManager.isRestart()) {
                    // IBC thinks we're auto-restarting because of the existence
                    // of the autorestart file. If the autorestart file contains
                    // valid credentials, TWS does everything automatically and
                    // there is nothing for IBC to do: in this case, the userid,
                    // and password fields are disabled (and sometimes aren't
                    // present at all).
                    //
                    // However, if the autorestart file contains invalid or
                    // expired credentials, TWS just displays the normal login
                    // dialog and waits for userid and password to be supplied
                    // (ie a normal full login). In this case, the user id and
                    // password fields are enabled and empty.
                    
                    if (isUserIdDisabledOrAbsent(window) && isPasswordDisabledOrAbsent(window)) {
                        // nothing to do so get out
                        break;
                    }

                    // the autorestart file is invalid. TWS doesn't remove
                    // it, so we need to delete it before proceeding with a
                    // normal login
                    
                    Utils.logToConsole("Autorestart file contains invalid credentials: performing full login");
                    Utils.logToConsole("Deleting Autorestart file");
                    File autorestartFile = new File(System.getProperty("jtsConfigDir") +
                                                    File.separator +
                                                    System.getProperty("restart") +
                                                    File.separator +
                                                    "autorestart");
                    autorestartFile.delete();
                };

                initiateLogin(window);
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
            Utils.exitWithError(ErrorCodes.CANT_FIND_CONTROL, "could not login: could not find control: " + e.getMessage());
        }
    }

    private void doLogin(final Window window) throws IbcException {
        
        // this JLabel is only present for the 1016+ versions
        final JLabel initialTitleLabel = SwingUtils.findLabel(window, "LOGIN");
        
        GuiDeferredExecutor.instance().execute(() -> {
            final JButton loginButton = findLoginButton(window);
            LoginManager.loginManager().setLoginState(LoginManager.LoginState.LOGGING_IN);
            SwingUtils.clickButton(loginButton);
        });
        
        String tradingMode = TradingModeManager.tradingModeManager().getTradingMode();
        if (tradingMode.equalsIgnoreCase(TradingModeManager.TRADING_MODE_PAPER)) {
            // paper trading mode doesn't use Second Factor Authentication, so nothing
            // to do here
        } else if (initialTitleLabel != null) {
            // Starting with TWS 1016, there is no longer a separate Second Factor
            // Authentication dialog. Instead, TWS replaces the Login frame's controls
            // with the controls that used to be in the 2FA dialog (so the Login frame
            // effectively becomes the 2FA frame). This doesn't generate any events
            // that IBC normally handles, so it goes undetected, and thus IBC doesn't
            // know when to process the Second Factor Authentication dialog. 
            //
            // To avoid this problem, we make a periodic check that the JLabel that
            // initially contained "LOGIN" has changed to "SECOND FACTOR AUTHENTICATION":
            // when this happens, we can pass the window to the SecondFactorAuthenticationDialogHandler
            // to be actioned.

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
    
    protected abstract boolean isUserIdDisabledOrAbsent(Window window);

    protected abstract boolean isPasswordDisabledOrAbsent(Window window);

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

    protected final boolean setTradingMode(final Window window) {
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
            return true;
        } else {
            // the dialog appears to have been deconstructed, stop tidily
            // and do a cold restart
            
            Utils.logToConsole("Login dialog has been invslidated - initiate cold restart");
            MyCachedThreadPool.getInstance().execute(new StopTask(null, true, "Login Error dialog encountered"));
            return false;
        }
    }
    
}
