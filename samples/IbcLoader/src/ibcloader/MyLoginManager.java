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

package ibcloader;

import ibcalpha.ibc.AbstractLoginHandler;
import javax.swing.JFrame;

public class MyLoginManager extends ibcalpha.ibc.LoginManager {

    private volatile JFrame loginFrame = null;
    
    private volatile AbstractLoginHandler loginHandler = null;

    @Override
    public void logDiagnosticMessage() {
        System.out.println("using MyLoginManager provider");
    }

    @Override
    public String FIXPassword() {
        return "";
    }

    @Override
    public String FIXUserName() {
        return "";
    }

    @Override
    public String IBAPIPassword() {
        return "password";
    }

    @Override
    public String IBAPIUserName() {
        return "username";
    }

    @Override
    public JFrame getLoginFrame() {
        return loginFrame;
    }

    @Override
    public void setLoginFrame(JFrame window) {
        loginFrame = window;
    }
    
    @Override
    public AbstractLoginHandler getLoginHandler() {
        return loginHandler;
    }

    @Override
    public void setLoginHandler(AbstractLoginHandler handler) {
        loginHandler = handler;
    }
}
