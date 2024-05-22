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

import javax.swing.JFrame;

public class DefaultLoginManager extends LoginManager {

    public DefaultLoginManager() {
        /* don't actually get the credentials yet because the settings 
         * provider might be changed
         */
        ibapiCredentialsFromArgs = false;
        fixCredentialsFromArgs = false;
        message = "will get username and password from settings";
    }

    public DefaultLoginManager(String[] args) {
        ibapiCredentialsFromArgs = getTWSUserNameAndPasswordFromArguments(args);
        fixCredentialsFromArgs = getFIXUserNameAndPasswordFromArguments(args);
        message = "will get username and password from " + 
                (ibapiCredentialsFromArgs ? "args" : "settings") + 
                "; FIX username and password (if required) from " + 
                (fixCredentialsFromArgs ? "args" : "settings");
    }

    public DefaultLoginManager(String username, String password) {
        IBAPIUserName = username;
        IBAPIPassword = password;
        ibapiCredentialsFromArgs = true;
        fixCredentialsFromArgs = false;
        message = "getting username and password from constructor";
    }

    public DefaultLoginManager(String FIXUsername, String FIXPassword, String IBAPIUsername, String IBAPIPassword) {
        this.FIXUserName = FIXUsername;
        this.FIXPassword = FIXPassword;
        this.IBAPIUserName = IBAPIUsername;
        this.IBAPIPassword = IBAPIPassword;
        ibapiCredentialsFromArgs = true;
        fixCredentialsFromArgs = true;
        message = "getting username and password from constructor (including FIX)";
    }

    private final String message;

    private volatile AbstractLoginHandler loginHandler = null;

    private final boolean ibapiCredentialsFromArgs;
    private final boolean fixCredentialsFromArgs;

    /**
     * IBAPI username - can either be supplied from the .ini file or as args[1]
     * NB: if IBAPI username is supplied in args[1], then the password must
     * be in args[2]. If IBAPI username is supplied in .ini, then the password must
     * also be in .ini.
     */
    private volatile String IBAPIUserName;

    /**
     * IBAPI password - can either be supplied from the .ini file or as args[2]
     */
    private volatile String IBAPIPassword;

    /**
     * FIX username - can either be supplied from the .ini file or as args[1]
     * NB: if username is supplied in args[1], then the password must
     * be in args[2], and the IBAPI username and password may be in 
     * args[3] and args[4]. If username is supplied in .ini, then the password must
     * also be in .ini.
     */
    private volatile String FIXUserName;

    /**
     * FIX password - can either be supplied from the .ini file or as args[2]
     */
    private volatile String FIXPassword;


    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default login manager: " + message);
    }

    @Override
    public String FIXPassword() {
        if (fixCredentialsFromArgs) return FIXPassword;
        return getFIXPasswordFromSettings();
    }

    @Override
    public String FIXUserName() {
        if (fixCredentialsFromArgs) return FIXUserName;
        return getFIXUserNameFromSettings();
    }

    @Override
    public String IBAPIPassword() {
        if (ibapiCredentialsFromArgs) return IBAPIPassword;
        return getTWSPasswordFromSettings();
    }

    @Override
    public String IBAPIUserName() {
        if (ibapiCredentialsFromArgs) return IBAPIUserName;
        return getTWSUserNameFromSettings();
    }

    @Override
    public JFrame getLoginFrame() {
        return super.getLoginFrame();
    }

    @Override
    public void setLoginFrame(JFrame window) {
        super.setLoginFrame(window);
    }

    @Override
    public AbstractLoginHandler getLoginHandler() {
        return loginHandler;
    }

    @Override
    public void setLoginHandler(AbstractLoginHandler handler) {
        loginHandler = handler;
    }

    private static String getFIXPasswordFromSettings() {
        String password = Settings.settings().getString("FIXPassword", "");
        return password;
    }

    private static String getFIXUserNameFromSettings() {
        return Settings.settings().getString("FIXLoginId", "");
    }

    private boolean getFIXUserNameAndPasswordFromArguments(String[] args) {
        if (args.length >= 3 && args.length <= 6) {
            FIXUserName = args[1];
            FIXPassword = args[2];
            return true;
        } else {
            return false;
        }
    }

    private static String getTWSPasswordFromSettings() {
        String password = Settings.settings().getString("IbPassword", "");
        return password;
    }

    private static String getTWSUserNameFromSettings() {
        return Settings.settings().getString("IbLoginId", "");
    }

    private boolean getTWSUserNameAndPasswordFromArguments(String[] args) {
        if (args.length == 5 || args.length == 6) {
            IBAPIUserName = args[3];
            IBAPIPassword = args[4];
            return true;
        
        }
        if (args.length == 3 || args.length == 4) {
            IBAPIUserName = args[1];
            IBAPIPassword = args[2];
            return true;
        }
        if (args.length <= 2) {
            return false;
        }
        Utils.logError("Incorrect number of arguments passed. quitting...");
        Utils.logRawToConsole("Number of arguments = " +args.length);
        for (int i = 0; i < args.length; i++) {
            Utils.logRawToConsole("arg[" + i + "]=" + args[i]);
        }
        Utils.exitWithError(ErrorCodes.INCORRECT_NUMBER_OF_ARGS);
        return false;
    }

}
