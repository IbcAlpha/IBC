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

public class TradingModeManager {
    
    private TradingModeManager() {}
    
    static final String TRADING_MODE_LIVE = "live";
    static final String TRADING_MODE_PAPER = "paper";
    
    /**
     * Indicates whether the live or paper trading account is to be used.
     * Must be in either args[1] (if there are two args), or args[3] (if there are 
     * four args), or args[5] (if there are six args)
     */
    private static String _TradingMode;
    
    static void initialise(String[] args) {
        if (args.length == 0) {
            _TradingMode = TRADING_MODE_LIVE;
        } else if (args.length == 2) {
            _TradingMode = args[1];
        } else if (args.length == 4) {
            _TradingMode = args[3];
        } else if (args.length == 6) {
            _TradingMode = args[5];
        }
        
        if (_TradingMode == null) {
            _TradingMode = Settings.getString("TradingMode", TRADING_MODE_LIVE);
        }

        if (!(_TradingMode.equals(TRADING_MODE_LIVE) || _TradingMode.equals(TRADING_MODE_PAPER))) {
                Utils.logError("Invalid Trading Mode argument or .ini file setting: " + _TradingMode);
                System.exit(1);
        }
        
    }

    static String getTradingMode() {
        return _TradingMode;
    }
    
}
