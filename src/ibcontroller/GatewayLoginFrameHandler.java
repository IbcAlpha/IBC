// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
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

import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

final class GatewayLoginFrameHandler extends AbstractLoginHandler {
    
    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;

        return (SwingUtils.titleContains(window, "IB Gateway") &&
               (SwingUtils.findButton(window, "Login") != null));
    }

    @Override
    protected final boolean initialise(final Window window, int eventID) throws IBControllerException {
        selectGatewayMode(window);
        setTradingModeCombo(window);
        return true;
    }
    
    @Override
    protected final boolean preLogin(final Window window, int eventID) throws IBControllerException {
        boolean result;
        if (Settings.settings().getBoolean("FIX", false)) {
            result = setMissingFIXCredentials(window);
        } else {
            result =setMissingIBAPICredentials(window);
        }
        return result;
    }
    
    private boolean setMissingFIXCredentials(Window window) {
        boolean result = false;
        if (LoginManager.loginManager().FIXUserName().length() == 0) {
            setMissingCredential(window, 0);
        } else if (LoginManager.loginManager().FIXPassword().length() == 0) {
            setMissingCredential(window, 1);
        } else if (LoginManager.loginManager().IBAPIUserName().length() != 0 || LoginManager.loginManager().IBAPIPassword().length() != 0) {
            if (LoginManager.loginManager().IBAPIUserName().length() == 0) {
                setMissingCredential(window, 3);
            } else if (LoginManager.loginManager().IBAPIPassword().length() == 0) {
                setMissingCredential(window, 4);
            } else {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    private boolean setMissingIBAPICredentials(Window window) {
        boolean result = false;
        if (LoginManager.loginManager().IBAPIUserName().length() == 0) {
            setMissingCredential(window, 0);
        } else if (LoginManager.loginManager().IBAPIPassword().length() == 0) {
            setMissingCredential(window, 1);
        } else {
            result = true;
        }
        return result;
    }

    @Override
    protected final boolean setFields(Window window, int eventID) throws IBControllerException {
        if (Settings.settings().getBoolean("FIX", false)) {
            setCredential(window, "FIX user name", 0, LoginManager.loginManager().FIXUserName());
            setCredential(window, "FIX password", 1, LoginManager.loginManager().FIXPassword());
            setCredential(window, "IBAPI user name", 3, LoginManager.loginManager().IBAPIUserName());
            setCredential(window, "IBAPI password", 4, LoginManager.loginManager().IBAPIPassword());
        } else {
            setCredential(window, "IBAPI user name", 0, LoginManager.loginManager().IBAPIUserName());
            setCredential(window, "IBAPI password", 1, LoginManager.loginManager().IBAPIPassword());
        }
        return true;
    }
    
    private void selectGatewayMode(Window window) throws IBControllerException {
        if (Settings.settings().getBoolean("FIX", false)) {
            switchToFIX(window);
        } else {
            switchToIBAPI(window);
        }
    }
    
    private void switchToFIX(Window window) throws IBControllerException {
        JRadioButton button = SwingUtils.findRadioButton(window, "FIX CTCI");
        if (button == null) throw new IBControllerException("FIX CTCI radio button");
        
        if (! button.isSelected()) button.doClick();
    }
    
    private void switchToIBAPI(Window window) throws IBControllerException {
        JRadioButton button = SwingUtils.findRadioButton(window, "IB API");
        if (button == null) button = SwingUtils.findRadioButton(window, "TWS/API") ;
        if (button == null) throw new IBControllerException("IB API radio button");
        
        if (! button.isSelected()) button.doClick();
    }

}
