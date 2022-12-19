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

    public DefaultSettings(String[] args) {
        load(getSettingsPath(args));
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
        return props.getProperty(key.toString());
    }

    static String getSettingsPath(String [] args) {
        String iniPath;
        if args[0].equalsIgnoreCase("NULL") {
            Utils.logError("path argument is NULL. quitting...");
            Utils.logRawToConsole("args = " +args);
            Utils.exitWithError(ErrorCodes.ERROR_CODE_INI_FILE_NOT_EXIST, "path argument is NULL. quitting...");
        } else {
            iniPath = args[0];
            File finiPath = new File(iniPath);
            if (!finiPath.isFile() || !finiPath.exists()) {
                Utils.exitWithError(ErrorCodes.ERROR_CODE_INI_FILE_NOT_EXIST,  "ini file \"" + iniPath +
                                "\" either does not exist, or is a directory.  quitting...");
            }
            return iniPath;
        }
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
