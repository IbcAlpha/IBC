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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DefaultSettings extends Settings {

    private final Properties props = new Properties();
    private String path;

    public DefaultSettings() {
        load(generateDefaultIniPath());
    }

    public DefaultSettings(String[] args) {
        load(getSettingsPath(args));
    }

    public DefaultSettings(String path) {
        load(path);
    }

    private void load(String path) {
        this.path = path;
        props.clear();
        try {
            File f = new File(path);
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            props.load(is);
            is.close();

            Utils.logRawToConsole("IBC Settings:");
            Object[] keys = props.stringPropertyNames().toArray();
            java.util.Arrays.sort(keys);
            for (Object key : keys){
                Utils.logRawToConsole("    " + key + "=" + getSettingSanitisedValue(key.toString()));
            }
            Utils.logRawToConsole("End IBC Settings\n" );
        } catch (FileNotFoundException e) {
            Utils.logToConsole("Properties file " + path + " not found");
        } catch (IOException e) {
            Utils.logToConsole(
                    "Exception accessing Properties file " + path);
            Utils.logToConsole(e.toString());
        }
    }
    
    private String getSettingSanitisedValue(String key) {
        if (key.equalsIgnoreCase("FIXLoginId") ||
                key.equalsIgnoreCase("FIXPassword") ||
                key.equalsIgnoreCase("IbLoginId") ||
                key.equalsIgnoreCase("IbPassword")) {
            return "***";
        }
        return props.getProperty(key.toString());
    }

    static String generateDefaultIniPath() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return System.getenv("HOMEDRIVE") + 
                    System.getenv("HOMEPATH") + File.separator + 
                    "Documents" + File.separator + 
                    "IBC" + File.separator + 
                    "config.ini";
        } else {
            return System.getProperty("user.home") + File.separator + 
                    "ibc" + File.separator + 
                    "config.ini";
        }
    }

    static String getSettingsPath(String [] args) {
        String iniPath;
        if (args.length == 0 || args[0].equalsIgnoreCase("NULL")) {
            iniPath = getWorkingDirectory() + "config." + getComputerUserName() + ".ini";
        } else if (args[0].length() == 0) {
            iniPath = generateDefaultIniPath();
        } else {// args.length >= 1
            iniPath = args[0];
        }
        File finiPath = new File(iniPath);
        if (!finiPath.isFile() || !finiPath.exists()) {
            Utils.exitWithError(ErrorCodes.INI_FILE_DOES_NOT_EXIST,  "ini file \"" + iniPath +
                               "\" either does not exist, or is a directory.  quitting...");
        }
        return iniPath;
    }

    private static String getComputerUserName() {
        StringBuilder sb = new StringBuilder(System.getProperty("user.name"));
        int i;
        for (i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                continue;
            }
            if (c >= 'A' && c <= 'Z') {
                sb.setCharAt(i, Character.toLowerCase(c));
            } else {
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString();
    }

    private static String getWorkingDirectory() {
        return System.getProperty("user.dir") + File.separator;
    }

    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default settings provider: ini file is " + path);
    }

    /**
    returns the value associated with property named key.
    Returns defaultValue if no such property.
     * @param key
     * @param defaultValue
     * @return 
     */
    @Override
    public String getString(String key,
                            String defaultValue) {
        String value = props.getProperty(key, defaultValue);

        // handle key=[empty string] in .ini file 
        if (value.isEmpty()) {
            value = defaultValue;
        }
        return value;
    }

    /**
    returns the int value associated with property named key.
    Returns defaultValue if there is no such property,
    or if the property value cannot be converted to an int.
     * @param key
     * @param defaultValue
     * @return 
     */
    @Override
    public int getInt(String key,
                      int defaultValue) {
        String value = props.getProperty(key);

        // handle key missing or key=[empty string] in .ini file 
        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Utils.logToConsole(
                    "Invalid number \""
                    + value
                    + "\" for property \""
                    + key
                    + "\"");
            return defaultValue;
        }
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    @Override
    public char getChar(String key,
                        String defaultValue) {
        String value = props.getProperty(key, defaultValue);

        // handle key missing or key=[empty string] in .ini file 
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
     * @param key
     * @param defaultValue
     * @return 
     */
    @Override
    public double getDouble(String key,
                            double defaultValue) {
        String value = props.getProperty(key);

        // handle key missing or key=[empty string] in .ini file 
        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Utils.logToConsole(
                    "Invalid number \""
                    + value
                    + "\" for property \""
                    + key
                    + "\"");
            return defaultValue;
        }
    }

    /**
    returns the boolean value associated with property named key.
    Returns defaultValue if there is no such property,
    or if the property value cannot be converted to a boolean.
     * @param key
     * @param defaultValue
     * @return 
     */
    @Override
    public boolean getBoolean(String key,
                              boolean defaultValue) {
        String value = props.getProperty(key);

        // handle key missing or key=[empty string] in .ini file 
        if (value == null || value.length() == 0) {
            return defaultValue;
        }

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

}
