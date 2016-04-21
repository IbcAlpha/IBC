// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2016 Richard L King (rlking@aultan.com)
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

import javax.swing.JFrame;

public class DefaultLoginManager extends LoginManager {
    
    public DefaultLoginManager() {
        /* don't actually get the credentials yet because the settings 
         * provider might be changed
         */
        fromSettings = true;
        message = "getting username and password from settings";
    }
    
    public DefaultLoginManager(String[] args) {
        if (isFIX()) {
            getTWSUserNameAndPasswordFromArguments(args);
            fromSettings = !getFIXUserNameAndPasswordFromArguments(args);
        } else {
            fromSettings = !getTWSUserNameAndPasswordFromArguments(args);
        }
        if (fromSettings) {
            message = "getting username and password from args but not found. Will get from settings";
        } else {
            message = "getting username and password from args";
        }
    }
    
    public DefaultLoginManager(String username, String password) {
        IBAPIUserName = username;
        IBAPIPassword = password;
        fromSettings = false;
        message = "getting username and password from constructor";
    }
    
    public DefaultLoginManager(String FIXUsername, String FIXPassword, String IBAPIUsername, String IBAPIPassword) {
        this.FIXUserName = FIXUsername;
        this.FIXPassword = FIXPassword;
        this.IBAPIUserName = IBAPIUsername;
        this.IBAPIPassword = IBAPIPassword;
        fromSettings = false;
        message = "getting username and password from constructor (including FIX)";
    }
    
    private final String message;
    
    private volatile JFrame loginFrame = null;
    
    private boolean fromSettings;

    /**
     * IBAPI username - can either be supplied from the .ini file or as args[1]
     * NB: if IBAPI username is supplied in args[1], then the password must
     * be in args[2]. If IBAPI username is supplied in .ini, then the password must
     * also be in .ini.
     */
    private volatile String IBAPIUserName;

    /**
     * unencrypted IBAPI password - can either be supplied from the .ini file or as args[2]
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
     * unencrypted FIX password - can either be supplied from the .ini file or as args[2]
     */
    private volatile String FIXPassword;

    
    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default login manager: " + message);
    }

    @Override
    public String FIXPassword() {
        if (fromSettings) return getFIXPasswordFromSettings();
        return FIXPassword;
    }

    @Override
    public String FIXUserName() {
        if (fromSettings) return getFIXUserNameFromSettings();
        return FIXUserName;
    }

    @Override
    public String IBAPIPassword() {
        if (fromSettings) return getTWSPasswordFromSettings();
        return IBAPIPassword;
    }

    @Override
    public String IBAPIUserName() {
        if (fromSettings) return getTWSUserNameFromSettings();
        return IBAPIUserName;
    }
    
    @Override
    public JFrame getLoginFrame() {
        return loginFrame;
    }

    @Override
    public void setLoginFrame(JFrame window) {
        loginFrame = window;
    }

    private static String getFIXPasswordFromSettings() {
        String password = Settings.settings().getString("FIXPassword", "");
        if (password.length() != 0) {
            if (isFIXPasswordEncrypted()) password = Encryptor.decrypt(password);
        }
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
        if (password.length() != 0) {
            if (isTwsPasswordEncrypted()) password = Encryptor.decrypt(password);
        }
        return password;
    }

    private static String getTWSUserNameFromSettings() {
        return Settings.settings().getString("IbLoginId", "");
    }

    private boolean getTWSUserNameAndPasswordFromArguments(String[] args) {
        if (isFIX()) {
            if (args.length == 5 || args.length == 6) {
                IBAPIUserName = args[3];
                IBAPIPassword = args[4];
                return true;
            } else {
                return false;
            }
        } else if (args.length == 3 || args.length == 4) {
            IBAPIUserName = args[1];
            IBAPIPassword = args[2];
            return true;
        } else if (args.length == 5 || args.length == 6) {
            Utils.logError("Incorrect number of arguments passed. quitting...");
            Utils.logRawToConsole("Number of arguments = " +args.length + " which is only permitted if FIX=yes");
            for (String arg : args) {
                Utils.logRawToConsole(arg);
            }
            System.exit(1);
            return false;
        } else {
            return false;
        }
    }
    
    private static boolean isFIX() {
        return Settings.settings().getBoolean("FIX", false);
    }

    private static boolean isFIXPasswordEncrypted() {
        return Settings.settings().getBoolean("FIXPasswordEncrypted", true);
    }

    private static boolean isTwsPasswordEncrypted() {
        return Settings.settings().getBoolean("PasswordEncrypted", true);
    }

}
