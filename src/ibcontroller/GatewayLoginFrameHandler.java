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
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

class GatewayLoginFrameHandler  implements WindowHandler {

    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(final Window window, int eventID) {
        try {
            selectGatewayMode(window);
            if (setFields(window)) doLogin(window);
        } catch (IBControllerException e) {
            Utils.err.println("IBController: could not login: could not find control: " + e.getMessage());
        }
    }
    
    private void selectGatewayMode(Window window) throws IBControllerException {
        if (Settings.getBoolean("FIX", false)) {
            switchToFIX(window);
        } else {
            switchToIBAPI(window);
        }
    }
    
    private void switchToFIX(Window window) throws IBControllerException {
        JRadioButton button = Utils.findRadioButton(window, "FIX CTCI");
        if (button == null) throw new IBControllerException("FIX CTCI radio button");
        
        if (! button.isSelected()) button.doClick();
    }
    
    private void switchToIBAPI(Window window) throws IBControllerException {
        JRadioButton button = Utils.findRadioButton(window, "IB API");
        if (button == null) button = Utils.findRadioButton(window, "TWS/API") ;
        if (button == null) throw new IBControllerException("IB API radio button");
        
        if (! button.isSelected()) button.doClick();
    }
        
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JFrame)) return false;

        return (Utils.titleContains(window, "IB Gateway") &&
               (Utils.findButton(window, "Login") != null));
    }

    private boolean setFields(final Window window) throws IBControllerException {
        boolean isFIXMode = Settings.getBoolean("FIX", false);
        
        if (isFIXMode) {
            if (! Utils.setTextField(window, 0, TwsListener.getFIXUserName())) throw new IBControllerException("FIX user name");
            if (! Utils.setTextField(window, 1, TwsListener.getFIXPassword())) throw new IBControllerException("FIX password");
            if (! Utils.setTextField(window, 3, TwsListener.getIBAPIUserName())) throw new IBControllerException("IBAPI user name");
            if (! Utils.setTextField(window, 4, TwsListener.getIBAPIPassword())) throw new IBControllerException("IBAPI password");
        } else {
            if (! Utils.setTextField(window, 0, TwsListener.getIBAPIUserName())) throw new IBControllerException("IBAPI user name");
            if (! Utils.setTextField(window, 1, TwsListener.getIBAPIPassword()))  throw new IBControllerException("IBAPI password");
        }
            
        if (isFIXMode) {
            if (TwsListener.getFIXUserName().length() == 0) {
                Utils.findTextField(window, 0).requestFocus();
                return false;
            }
            if (TwsListener.getFIXPassword().length() == 0) {
                Utils.findTextField(window, 1).requestFocus();
                return false;
            }
            if (TwsListener.getIBAPIUserName().length() != 0) {
                if (TwsListener.getIBAPIPassword().length() == 0) {
                    Utils.findTextField(window, 4).requestFocus();
                    return false;
                }
            }
        } else {
            if (TwsListener.getIBAPIUserName().length() == 0) {
                Utils.findTextField(window, 0).requestFocus();
                return false;
            }
            if (TwsListener.getIBAPIPassword().length() == 0) {
                Utils.findTextField(window, 1).requestFocus();
                return false;
            }
        }
        return true;
    }

    private void doLogin(final Window window) throws IBControllerException {
        if (Utils.findButton(window, "Login") == null) throw new IBControllerException("Login button");

        GuiDeferredExecutor.instance().execute(new Runnable() {
            public void run() {
                Utils.clickButton(window, "Login");
            }
        });
    }

}
