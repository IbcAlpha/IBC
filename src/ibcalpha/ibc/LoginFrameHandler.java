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
import javax.swing.JFrame;

final class LoginFrameHandler extends AbstractLoginHandler {

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;

        // we check for the presence of the Login button because 
        // TWS displays a different (information-only) dialog, also 
        // entitled Login, when it's trying to reconnect
        return ((SwingUtils.titleEquals(window, "New Login") ||
                SwingUtils.titleEquals(window, "Login")) &&
                (SwingUtils.findButton(window, "Login") != null ||
                SwingUtils.findButton(window, "Log In") != null ||          // TWS 974+
                SwingUtils.findButton(window, "Paper Log In") != null));    // TWS 974+
    }

    @Override
    protected final boolean initialise(final Window window, int eventID) throws IbcException {
        setTradingMode(window);

        JtsIniManager.reload();     // because TWS/Gateway modify the jts.ini file before this point
        String s3Store = JtsIniManager.getSetting(JtsIniManager.LogonSectionHeader, JtsIniManager.S3storeSetting);
        if (s3Store.compareToIgnoreCase("true") == 0 && Settings.settings().getString("StoreSettingsOnServer", "").length() != 0) {
            final String STORE_SETTINGS_ON_SERVER_CHECKBOX = "Use/store settings on server";
            if (! SwingUtils.setCheckBoxSelected(
                    window,
                    STORE_SETTINGS_ON_SERVER_CHECKBOX,
                    Settings.settings().getBoolean("StoreSettingsOnServer", false))) throw new IbcException(STORE_SETTINGS_ON_SERVER_CHECKBOX);
        }
        return true;
    }
    
    @Override
    protected final boolean preLogin(final Window window, int eventID) throws IbcException {
        if (LoginManager.loginManager().IBAPIUserName().length() == 0) {
            setMissingCredential(window, 0);
        } else if (LoginManager.loginManager().IBAPIPassword().length() == 0) {
            setMissingCredential(window, 1);
        } else {
            return true;
        }
        return false;
    }
    
    @Override
    protected final boolean setFields(Window window, int eventID) throws IbcException {
        setCredential(window, "IBAPI user name", 0, LoginManager.loginManager().IBAPIUserName());
        setCredential(window, "IBAPI password", 1, LoginManager.loginManager().IBAPIPassword());
        return true;
    }
    
}

