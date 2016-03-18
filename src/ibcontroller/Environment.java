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

import java.util.concurrent.Callable;

/**
 * This class provides a simple dependency injection mechanism.
 * 
 */
public class Environment {
    private static Settings _settings;
    private static LoginManager _loginManager;
    private static MainWindowManager _mainWindowManager;
    private static ConfigDialogManager _configDialogManager;
    private static TradingModeManager _tradingModeManager;
    
    public static ConfigDialogManager configDialogManager() {
        return _configDialogManager;
    }
    
    public static LoginManager loginManager() {
    return _loginManager;
    }
    
    public static void load(Callable<Settings> settingsCreator, 
                            Callable<LoginManager> loginManagerCreator,
                            Callable<MainWindowManager> mainWindowManagerCreator,
                            Callable<ConfigDialogManager> configDialogManagerCreator,
                            Callable<TradingModeManager> tradingModeManagerCreator) throws Exception {
        if (settingsCreator != null) {
            _settings = settingsCreator.call();
        } else {
            _settings = new DefaultSettings();
        }
        
        if (loginManagerCreator != null ) {
            _loginManager = loginManagerCreator.call();
        } else {
            _loginManager = new DefaultLoginManager();
        } 
           
        if (mainWindowManagerCreator != null ) {
            _mainWindowManager = mainWindowManagerCreator.call();
        } else {
            _mainWindowManager = new DefaultMainWindowManager();
        }
        
        if (configDialogManagerCreator != null) {
            _configDialogManager = configDialogManagerCreator.call();
        } else {
            _configDialogManager = new DefaultConfigDialogManager();
        }
        
        if (tradingModeManagerCreator != null) {
            _tradingModeManager = tradingModeManagerCreator.call();
        } else {
            _tradingModeManager = new DefaultTradingModeManager();
        }
    }
    
    public static MainWindowManager mainWindowManager() {
        return _mainWindowManager;
    }
    
    public static Settings settings() {
        return _settings;
    }
    
    public static TradingModeManager tradingModeManager() {
        return _tradingModeManager;
    }
    
    static void verify() {
        if (_settings == null) _settings = new DefaultSettings();
        if (_loginManager == null ) _loginManager = new DefaultLoginManager();
        if (_mainWindowManager == null) _mainWindowManager = new DefaultMainWindowManager();
        if ( _configDialogManager == null) _configDialogManager = new DefaultConfigDialogManager();
        if (_tradingModeManager == null) _tradingModeManager = new DefaultTradingModeManager();
    }
}
