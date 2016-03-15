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

public class LoginCredentials {
    
    private LoginCredentials() {}
    
    /**
     * IBAPI username - can either be supplied from the .ini file or as args[1]
     * NB: if IBAPI username is supplied in args[1], then the password must
     * be in args[2]. If IBAPI username is supplied in .ini, then the password must
     * also be in .ini.
     */
    private static volatile String _IBAPIUserName;

    /**
     * unencrypted IBAPI password - can either be supplied from the .ini file or as args[2]
     */
    private static volatile String _IBAPIPassword;

    /**
     * FIX username - can either be supplied from the .ini file or as args[1]
     * NB: if username is supplied in args[1], then the password must
     * be in args[2], and the IBAPI username and password may be in 
     * args[3] and args[4]. If username is supplied in .ini, then the password must
     * also be in .ini.
     */
    private static volatile String _FIXUserName;

    /**
     * unencrypted FIX password - can either be supplied from the .ini file or as args[2]
     */
    private static volatile String _FIXPassword;

    
    static void initialise(String[] args) {
        getFIXUserNameAndPassword(args);
        getTWSUserNameAndPassword(args);
    }

    static String getFIXPassword() {
        return _FIXPassword;
    }

    static String getFIXUserName() {
        return _FIXUserName;
    }

    static String getIBAPIPassword() {
        return _IBAPIPassword;
    }

    static String getIBAPIUserName() {
        return _IBAPIUserName;
    }
    
    private static String getFIXPasswordFromProperties() {
        String password = Settings.getString("FIXPassword", "");
        if (password.length() != 0) {
            if (isFIXPasswordEncrypted()) password = Encryptor.decrypt(password);
        }
        return password;
    }

    private static void getFIXUserNameAndPassword(String[] args) {
        if (! getFIXUserNameAndPasswordFromArguments(args)) {
            getFIXUserNameAndPasswordFromProperties();
        }
    }

    private static String getFIXUserNameFromProperties() {
        return Settings.getString("FIXLoginId", "");
    }

    private static boolean getFIXUserNameAndPasswordFromArguments(String[] args) {
        if (args.length == 3 || args.length == 5) {
            _FIXUserName = args[1];
            _FIXPassword = args[2];
            return true;
        } else {
            return false;
        }
    }

    private static void getFIXUserNameAndPasswordFromProperties() {
        _FIXUserName = getFIXUserNameFromProperties();
        _FIXPassword = getFIXPasswordFromProperties();
    }
    
    private static String getTWSPasswordFromProperties() {
        String password = Settings.getString("IbPassword", "");
        if (password.length() != 0) {
            if (isTwsPasswordEncrypted()) password = Encryptor.decrypt(password);
        }
        return password;
    }

    private static void getTWSUserNameAndPassword(String[] args) {
        if (! getTWSUserNameAndPasswordFromArguments(args)) {
            getTWSUserNameAndPasswordFromProperties();
        }
    }

    private static String getTWSUserNameFromProperties() {
        return Settings.getString("IbLoginId", "");
    }

    private static boolean getTWSUserNameAndPasswordFromArguments(String[] args) {
        if (Settings.getBoolean("FIX", false)) {
            if (args.length == 5) {
                _IBAPIUserName = args[3];
                _IBAPIPassword = args[4];
                return true;
            } else {
                return false;
            }
        } else if (args.length == 3) {
            _IBAPIUserName = args[1];
            _IBAPIPassword = args[2];
            return true;
        } else {
            return false;
        }
    }

    private static void getTWSUserNameAndPasswordFromProperties() {
        _IBAPIUserName = getTWSUserNameFromProperties();
        _IBAPIPassword = getTWSPasswordFromProperties();
    }

    private static boolean isFIXPasswordEncrypted() {
        return Settings.getBoolean("FIXPasswordEncrypted", true);
    }

    private static boolean isTwsPasswordEncrypted() {
        return Settings.getBoolean("PasswordEncrypted", true);
    }

}
