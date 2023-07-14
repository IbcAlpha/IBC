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

public class DefaultTradingModeManager extends TradingModeManager {

    private String tradingMode;

    public DefaultTradingModeManager() {
        fromSettings = false;
        setTradingMode(TRADING_MODE_LIVE);
        message = "parameterless constructor (trading mode live assumed)";
    }

    public DefaultTradingModeManager(String tradingMode) {
        fromSettings = false;
        setTradingMode(tradingMode);
        message = "constructor parameter tradingMode=" + tradingMode;
    }

    /*
     * Must be in either args[1] (if there are two args), or args[3] (if there are 
     * four args), or args[5] (if there are six args)
    */
    public DefaultTradingModeManager(String[] args) {
        if (args.length == 0) {
            setTradingMode(TRADING_MODE_LIVE);
        } else if (args.length == 2) {
            setTradingMode(args[1]);
        } else if (args.length == 4) {
            setTradingMode(args[3]);
        } else if (args.length == 6) {
            setTradingMode(args[5]);
        }

        if (tradingMode != null) {
            fromSettings = false;
            message = "constructor parameter args: tradingMode=" + tradingMode;
        } else {
            fromSettings = true;
            message = "constructor parameter args but trading mode not present - will be taken from settings";
        }
    }

    private void setTradingMode(String value) {
        if (!(value.equalsIgnoreCase(TRADING_MODE_LIVE) || value.equalsIgnoreCase(TRADING_MODE_PAPER))) {
                Utils.exitWithError(ErrorCodes.INVALID_TRADING_MODE, "Invalid Trading Mode argument or .ini file setting: " + tradingMode);
        }
        tradingMode = value;
    }

    private final String message;
    private final boolean fromSettings;

    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default trading mode manager: " + message);
    }

    @Override
    public String getTradingMode() {
        if (fromSettings) {
            setTradingMode( Settings.settings().getString("TradingMode", TRADING_MODE_LIVE));
            Utils.logToConsole("trading mode from settings: tradingMode=" + tradingMode);
        }
        return tradingMode;
    }

}
