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

public abstract class TradingModeManager {
    private static TradingModeManager _TradingModeManager;

    public static void initialise(TradingModeManager tradingModeManager){
        if (tradingModeManager == null) throw new IllegalArgumentException("tradingModeManager");
        _TradingModeManager = tradingModeManager;
    }

    public static TradingModeManager tradingModeManager() {
        return _TradingModeManager;
    }

    public abstract void logDiagnosticMessage();


    public static final String TRADING_MODE_LIVE = "live";
    public static final String TRADING_MODE_PAPER = "paper";

    public abstract String getTradingMode();

}
