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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
Adds convenient functions for getting int, double and boolean valued
property values.
 */
class Settings {

    private static final Properties _Props = new Properties();

    private Settings() { }

    static void load(String path) {
        _Props.clear();
        try {
            File f = new File(path);
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            _Props.load(is);
            is.close();
        } catch (FileNotFoundException e) {
            Utils.logToConsole("Properties file " + path + " not found");
        } catch (IOException e) {
            Utils.logToConsole(
                    "Exception accessing Properties file " + path);
            System.out.println(e);
        }
    }

    /**
    returns the value associated with property named key.
    Returns defaultValue if no such property.
     */
    static String getString(String key,
                            String defaultValue) {
        String value = _Props.getProperty(key, defaultValue);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
    returns the int value associated with property named key.
    Returns defaultValue if there is no such property,
    or if the property value cannot be converted to an int.
     */
    static int getInt(String key,
                      int defaultValue) {
        String value = _Props.getProperty(key);

        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Utils.logToConsole(
                        "Invalid number \""
                        + value
                        + "\" for property \""
                        + key
                        + "\"");
            }
        }

        return defaultValue;
    }

    static char getChar(String key,
                        String defaultValue) {
        String value = _Props.getProperty(key, defaultValue);

        if (value == null || value.length() == 0) {
            return defaultValue.charAt(0);
        }

        if (value.length() != 1) {
            Utils.logToConsole(
                    "Invalid character \""
                    + value
                    + "\" for property \""
                    + key
                    + "\"");
        }

        return value.charAt(0);
    }

    /**
    returns the double value associated with property named key.
    Returns defaultVAlue if there is no such property,
    or if the property value cannot be converted to a double.
     */
    static double getDouble(String key,
                            double defaultValue) {
        String value = _Props.getProperty(key);

        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                Utils.logToConsole(
                        "Invalid number \""
                        + value
                        + "\" for property \""
                        + key
                        + "\"");
            }
        }

        return defaultValue;
    }

    /**
    returns the boolean value associated with property named key.
    Returns defaultValue if there is no such property,
    or if the property value cannot be converted to a boolean.
     */
    static boolean getBoolean(String key,
                              boolean defaultValue) {
        String value = _Props.getProperty(key);

        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                return true;
            } else if (value.equalsIgnoreCase("yes")) {
                return true;
            } else if (value.equalsIgnoreCase("false")) {
                return false;
            } else if (value.equalsIgnoreCase("no")) {
                return false;
            } else {
                return defaultValue;
            }
        }

        return defaultValue;
    }

}



