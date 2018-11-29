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

public class MySettings extends ibcalpha.ibc.Settings {

    @Override
    public void logDiagnosticMessage() {
        System.out.println("using MySettings settings provider");
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        if ("AcceptNonBrokerageAccountWarning".compareTo(key) == 0) {
            return true;
        } else {
            return defaultValue;
        }
    }

    @Override
    public char getChar(String key, String defaultValue) {
        return defaultValue.charAt(0);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getInt(String key, int defaultValue) {
        if ("CommandServerPort".compareTo(key) == 0) {
            return 7462;
        } else {
            return defaultValue;
        }
    }

    @Override
    public String getString(String key, String defaultValue) {
        if ("IbDir".compareTo(key) == 0) {
            return "C:\\IbcLoader\\JtsSettings";
        } else if ("ExistingSessionDetectedAction".compareTo(key) == 0) {
            return "primary";
        } else {
            return defaultValue;
        }
    }
    
}
