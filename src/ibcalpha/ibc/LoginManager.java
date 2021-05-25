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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public abstract class LoginManager {

    private static LoginManager _LoginManager;

    static {
        _LoginManager = new DefaultLoginManager();
    }

    public static void initialise(LoginManager loginManager){
        if (loginManager == null) throw new IllegalArgumentException("loginManager");
        _LoginManager = loginManager;
    }

    public static void setDefault() {
        _LoginManager = new DefaultLoginManager();
    }

    public static LoginManager loginManager() {
        return _LoginManager;
    }

    public enum LoginState{
        LOGGED_OUT,
        LOGGED_IN,
        LOGGING_IN,
        TWO_FA_IN_PROGRESS,
        LOGIN_FAILED,
        AWAITING_CREDENTIALS
    }

    void startSession() {
        int loginDialogDisplayTimeout = Settings.settings().getInt("LoginDialogDisplayTimeout", 60);
        Utils.logToConsole("Starting session: will exit if login dialog is not displayed within " + loginDialogDisplayTimeout + " seconds");
        MyScheduledExecutorService.getInstance().schedule(()->{
            GuiExecutor.instance().execute(()->{
                if (getLoginState() != LoginManager.LoginState.LOGGED_OUT) {
                    // Login diaog has been shown - no need for IBC to exit
                    return;
                }
                Utils.exitWithError(ErrorCodes.ERROR_CODE_LOGIN_DIALOG_DISPLAY_TIMEOUT, "IBC closing after TWS/Gateway failed to display login dialog");
            });
        }, loginDialogDisplayTimeout, TimeUnit.SECONDS);
    }

    private volatile LoginState loginState = LoginState.LOGGED_OUT;
    public LoginState getLoginState() {
        return loginState;
    }

    public void setLoginState(LoginState state) {
        if (state == loginState) return;
        loginState = state;
        if (loginState == LoginState.TWO_FA_IN_PROGRESS) {
            Utils.logToConsole("Second Factor Authentication initiated");
            if (LoginStartTime == null) LoginStartTime = Instant.now();
        } else if (loginState == LoginState.LOGGED_IN) {
            Utils.logToConsole("Login has completed");
            if (shutdownAfterTimeTask != null) {
                shutdownAfterTimeTask.cancel(false);
                shutdownAfterTimeTask = null;
            }
        }
    }

    private Instant LoginStartTime;
    private ScheduledFuture<?> shutdownAfterTimeTask;

    public void secondFactorAuthenticationDialogClosed() {

        if (!Settings.settings().getBoolean("ExitAfterSecondFactorAuthenticationTimeout", false)) {
            Utils.logToConsole("ExitAfterSecondFactorAuthenticationTimeout is set to 'no'");
        } else {
            try {
                // minimum time (seconds) between attempts to login to ensure no 'Too many
                // failed login attempts' messages
                final int LOGIN_RETRY_INTERVAL = 300;

                // time (seconds) to allow for login to complete before exiting
                final int SECOND_FACTOR_AUTHENTICATION_EXIT_INTERVAL = 40;
                final int exitInterval = Settings.settings().getInt("SecondFactorAuthenticationExitInterval", 
                                                                    SECOND_FACTOR_AUTHENTICATION_EXIT_INTERVAL);
                final Duration d = Duration.between(LoginStartTime, Instant.now());
                LoginStartTime = null;

                final int secondsTillTimeout = Math.max(LOGIN_RETRY_INTERVAL - (int)d.getSeconds(),
                                                        exitInterval);

                Utils.logToConsole("If login has not completed, IBC will exit in " + secondsTillTimeout + " seconds");
                
                restartAfterTime(secondsTillTimeout, "IBC closing after Second Factor Authentication dialog timeout");

            } catch (Throwable e) {
                Utils.exitWithException(99999, e);
            }
        }

    }
    
    public void restartAfterTime(final int secondsTillShutdown, final String message) {
        try {
            shutdownAfterTimeTask = MyScheduledExecutorService.getInstance().schedule(()->{
                GuiExecutor.instance().execute(()->{
                    if (getLoginState() == LoginManager.LoginState.LOGGED_IN) {
                        Utils.logToConsole("Login has already completed - no need for IBC to exit");
                        return;
                    }
                    Utils.exitWithError(ErrorCodes.ERROR_CODE_2FA_DIALOG_TIMED_OUT, message);
                });
            }, secondsTillShutdown, TimeUnit.SECONDS);
        } catch (Throwable e) {
            Utils.exitWithException(99999, e);
        }
    }

    public abstract void logDiagnosticMessage();

    public abstract String FIXPassword();

    public abstract String FIXUserName();

    public abstract String IBAPIPassword();

    public abstract String IBAPIUserName();

    public abstract JFrame getLoginFrame();

    public abstract void setLoginFrame(JFrame window);

}
