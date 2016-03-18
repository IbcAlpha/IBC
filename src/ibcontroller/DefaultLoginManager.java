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

public class DefaultLoginManager implements LoginManager {
    
    public DefaultLoginManager() {
        getFIXUserNameAndPasswordFromSettings();
        getTWSUserNameAndPasswordFromSettings();
    }
    
    public DefaultLoginManager(String[] args) {
        getFIXUserNameAndPassword(args);
        getTWSUserNameAndPassword(args);
    }
    
    public DefaultLoginManager(String username, String password) {
        IBAPIUserName = username;
        IBAPIPassword = password;
    }
    
    public DefaultLoginManager(String FIXUsername, String FIXPassword, String IBAPIUsername, String IBAPIPassword) {
        this.FIXUserName = FIXUsername;
        this.FIXPassword = FIXPassword;
        this.IBAPIUserName = IBAPIUsername;
        this.IBAPIPassword = IBAPIPassword;
    }
    
    private volatile JFrame loginFrame = null;

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
    public String FIXPassword() {
        return FIXPassword;
    }

    @Override
    public String FIXUserName() {
        return FIXUserName;
    }

    @Override
    public String IBAPIPassword() {
        return IBAPIPassword;
    }

    @Override
    public String IBAPIUserName() {
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
        String password = Environment.settings().getString("FIXPassword", "");
        if (password.length() != 0) {
            if (isFIXPasswordEncrypted()) password = Encryptor.decrypt(password);
        }
        return password;
    }

    private void getFIXUserNameAndPassword(String[] args) {
        if (! getFIXUserNameAndPasswordFromArguments(args)) {
            getFIXUserNameAndPasswordFromSettings();
        }
    }

    private static String getFIXUserNameFromSettings() {
        return Environment.settings().getString("FIXLoginId", "");
    }

    private boolean getFIXUserNameAndPasswordFromArguments(String[] args) {
        if (args.length == 3 || args.length == 5) {
            FIXUserName = args[1];
            FIXPassword = args[2];
            return true;
        } else {
            return false;
        }
    }

    private void getFIXUserNameAndPasswordFromSettings() {
        FIXUserName = getFIXUserNameFromSettings();
        FIXPassword = getFIXPasswordFromSettings();
    }
    
    private static String getTWSPasswordFromSettings() {
        String password = Environment.settings().getString("IbPassword", "");
        if (password.length() != 0) {
            if (isTwsPasswordEncrypted()) password = Encryptor.decrypt(password);
        }
        return password;
    }

    private void getTWSUserNameAndPassword(String[] args) {
        if (! getTWSUserNameAndPasswordFromArguments(args)) {
            getTWSUserNameAndPasswordFromSettings();
        }
    }

    private static String getTWSUserNameFromSettings() {
        return Environment.settings().getString("IbLoginId", "");
    }

    private boolean getTWSUserNameAndPasswordFromArguments(String[] args) {
        if (Environment.settings().getBoolean("FIX", false)) {
            if (args.length == 5) {
                IBAPIUserName = args[3];
                IBAPIPassword = args[4];
                return true;
            } else {
                return false;
            }
        } else if (args.length == 3) {
            IBAPIUserName = args[1];
            IBAPIPassword = args[2];
            return true;
        } else {
            return false;
        }
    }

    private void getTWSUserNameAndPasswordFromSettings() {
        IBAPIUserName = getTWSUserNameFromSettings();
        IBAPIPassword = getTWSPasswordFromSettings();
    }

    private static boolean isFIXPasswordEncrypted() {
        return Environment.settings().getBoolean("FIXPasswordEncrypted", true);
    }

    private static boolean isTwsPasswordEncrypted() {
        return Environment.settings().getBoolean("PasswordEncrypted", true);
    }

}
