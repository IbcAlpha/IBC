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
    
    public abstract void logDiagnosticMessage();
    
    public abstract String FIXPassword();

    public abstract String FIXUserName();

    public abstract String IBAPIPassword();

    public abstract String IBAPIUserName();

    public abstract JFrame getLoginFrame();

    public abstract void setLoginFrame(JFrame window);
    
}
