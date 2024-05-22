// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2019 Richard L King (rlking@aultan.com)
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import static java.util.stream.Collectors.toList;

class JtsIniManager {

    private final static String LOGON_SECTION_HEADER = "[Logon]";
    private final static String IBGATEWAY_SECTION_HEADER = "[IBGateway]";
    private final static String DISPLAYEDPROXYMSG_SETTING = "displayedproxymsg";
    private final static String DISPLAYEDPROXYMSG_SETTING_1 = DISPLAYEDPROXYMSG_SETTING + "=1";
    private final static String LOCALE_SETTING = "Locale";
    private final static String LOCALE_SETTING_EN = LOCALE_SETTING + "=en";
    private final static String S3STORE_SETTING = "s3store";
    private final static String S3STORE_SETTING_FALSE = S3STORE_SETTING + "=false";
    private final static String S3STORE_SETTING_TRUE = S3STORE_SETTING + "=true";
    private final static String USESSL_SETTING = "UseSSL";
    private final static String USESSL_SETTING_TRUE = USESSL_SETTING + "=true";
    private final static String APIONLY_SETTING = "ApiOnly";
    private final static String APIONLY_SETTING_TRUE = APIONLY_SETTING + "=true";
    private final static String APIONLY_SETTING_FALSE = APIONLY_SETTING + "=false";
    private final static String TRUSTED_IPS_SETTING = "TrustedIPs";
    private final static String LOCAL_SERVER_PORT = "LocalServerPort";
    
    private final static String LOCAL_HOST = "127.0.0.1";

    private static String jtsIniFilePath;
    private static File jtsIniFile;
    private static List<String> lines;
    
    private static boolean settingsUpdated;

    /* when TWS starts, there must exist a jts.ini file in the TWS settings directory 
    *  containing at least the following minimum contents:
    *
    * [Logon]
    * Locale=en
    * displayedproxymsg=1
    * UseSSL=true
    *
    * The Locale setting is needed to ensure that TWS/Gateway run in English, 
    * regardless of what the user might have previously set manually.
    *
    * The displayedproxymsg setting controls the display of a recently-introduced 
    * (May 2019) and annoying, factually incorrect, dialog regarding inability to 
    * access the internet. It's not really important to have this setting, since 
    * the dialog is non-modal and only appears once per settings location, but
    * it is very annoying!
    *
    * The UseSSL=true setting was added in v3.9.0 (August 2021) because IB started
    * insisting on use of SSL: if UseSSL=false was set, a dialog was displayed by
    * TWS and Gateway giving the user the choice to restart using SSL or to close
    * the program. Ensuring UseSSL=true avoids this situation even if the user has
    * not manually configured used of SSL (which could be done via the login
    * dialog). Note also that some Docker images start without a jts.ini
    * containing UseSSL=true, and this new check ensures that they run properly.
    * 
    * As a historical note, the following information describes problems that 
    * occurred up to about TWS 963. These problems were the original motivation 
    * for creating the JtsIniManager class. The processing for these settings is 
    * retained just in case anyone is still using an affected TWS/Gateway version.
    *
    * s3store=true
    *
    * If this file doesn't exist, or doesn't contain these lines, then TWS won't 
    * include the 'Store settings on server' checkbox in the login dialog, which
    * prevents IBC properly handling the StoreSettingsOnServer ini file
    * option.
    * 
    * Note that this problem seems to have been fixed in TWS 960, which displays
    * the login dialog correctly if there is no s3store setting. There are
    * specialised configurations where TWS must NOT offer this option: for example
    * where 'cross connect' is used (ie direct connection to IB's data centre 
    * rather than via the internet), and these configurations need s3store=false.
    * However TWS 960 will automatically include s3store=false when such a
    * configuration is used.
    *
    * Note also that this is not a problem for the Gateway, which doesn't provide  
    * the option to store the settings on the server. 
    *
    * However Gateway 960 has another problem. If its jts.ini does not contain the 
    * following, the gateway displays a login form that has a structure that 
    * IBC doesn't expect, and it can't find the trading mode selector.
    *
    * [IBGateway]
    * ApiOnly=true
    *
    * To avoid these problems, IBC ensures that a jts.ini exists which 
    * contains at least both sets of lines shown above, with the exception that if it 
    * already exists and contains s3store=false then this setting will not be altered.
    */
    static void initialise(String jtsIniPath) {
        jtsIniFilePath = jtsIniPath;
        loadIniFile();
        if (jtsIniFile.isFile()) {
            updateExistingFile();
        } else {
            createMinimalFile();
        }
    }
    
    private static int getSettingIndex(String section, String setting) {
        String key = setting + "=";

        boolean inSection = false;
        for (int index = 0; index < lines.size(); index++) {
            String l = lines.get(index);
            if (l.compareTo(section) == 0) {
                inSection = true;
            } else if (l.startsWith("[")) {
                if (inSection) return -1;
                inSection = false;
            } else if (inSection) {
                if (l.startsWith(key)) {
                    return index;
                }
            }
        }

        return -1;
    }

    static void reload() {
        loadIniFile();
    }

    private static void loadIniFile() {
        jtsIniFile = new File(jtsIniFilePath);
        if (jtsIniFile.isDirectory()) {
            Utils.exitWithError(ErrorCodes.INVALID_JTSINI_PATH, 
                                jtsIniFilePath + " already exists but is a directory");
        }
        if (jtsIniFile.isFile()) {
            lines = getFileLines(jtsIniFile);
        }
    }

    private static void updateExistingFile() {
        Utils.logToConsole("Ensuring " + jtsIniFilePath + " contains required minimal lines");

        List<JtsIniSectionSetting> missingSettings = getMissingSettings();
        
        boolean rewrite = false;
        if (settingsUpdated) {
            Utils.logToConsole("Some settings updated in " + jtsIniFilePath);
            rewrite = true;
        }
        if (!missingSettings.isEmpty()) {
            Utils.logToConsole("Missing lines in " + jtsIniFilePath);
            rewrite = true;
        }
        if (rewrite){
            jtsIniFile.delete();
            rewriteExistingFile(missingSettings);
        } else {
            Utils.logToConsole("Confirmed " + jtsIniFilePath + " contains required minimal lines");
        }
    }

    private static List<JtsIniSectionSetting> getMissingSettings() {
        List<JtsIniSectionSetting> missingSettings = new ArrayList<>();

        if (SessionManager.isFIX()) {
            if (! findSettingAndLog(IBGATEWAY_SECTION_HEADER, APIONLY_SETTING, "true", true)) 
                missingSettings.add(new JtsIniSectionSetting(IBGATEWAY_SECTION_HEADER, APIONLY_SETTING_FALSE));
            
            String trustedIPs = Settings.settings().getString("TrustedTwsApiClientIPs", "");
            trustedIPs = LOCAL_HOST + (trustedIPs.equals("") ? "" : "," + trustedIPs);
            if (! findSettingAndLog(IBGATEWAY_SECTION_HEADER, TRUSTED_IPS_SETTING, trustedIPs, true))
                missingSettings.add(new JtsIniSectionSetting(IBGATEWAY_SECTION_HEADER, TRUSTED_IPS_SETTING + "=" + trustedIPs));
        
            String apiPort = Settings.settings().getString("OverrideTwsApiPort", "");
            if (! "".equals(apiPort)) {
                if (! findSettingAndLog(IBGATEWAY_SECTION_HEADER, LOCAL_SERVER_PORT, apiPort, true))
                    missingSettings.add(new JtsIniSectionSetting(IBGATEWAY_SECTION_HEADER, LOCAL_SERVER_PORT + "=" + apiPort));
            }
        } else {
            if (! findSettingAndLog(LOGON_SECTION_HEADER, S3STORE_SETTING, "true", false))
                missingSettings.add(new JtsIniSectionSetting(LOGON_SECTION_HEADER, S3STORE_SETTING_TRUE));

            if (! findSettingAndLog(LOGON_SECTION_HEADER, LOCALE_SETTING, "en", true)) 
                missingSettings.add(new JtsIniSectionSetting(LOGON_SECTION_HEADER, LOCALE_SETTING_EN));

            if (! findSettingAndLog(LOGON_SECTION_HEADER, DISPLAYEDPROXYMSG_SETTING, "1", true))
                missingSettings.add(new JtsIniSectionSetting(LOGON_SECTION_HEADER, DISPLAYEDPROXYMSG_SETTING_1));

            if (! findSettingAndLog(LOGON_SECTION_HEADER, USESSL_SETTING, "true", true))
                missingSettings.add(new JtsIniSectionSetting(LOGON_SECTION_HEADER, USESSL_SETTING_TRUE));

            if (! findSettingAndLog(IBGATEWAY_SECTION_HEADER, APIONLY_SETTING, "true", true)) 
                missingSettings.add(new JtsIniSectionSetting(IBGATEWAY_SECTION_HEADER, APIONLY_SETTING_TRUE));
            
            if (SessionManager.isGateway()){
                String trustedIPs = Settings.settings().getString("TrustedTwsApiClientIPs", "");
                trustedIPs = LOCAL_HOST + (trustedIPs.equals("") ? "" : "," + trustedIPs);
                if (! findSettingAndLog(IBGATEWAY_SECTION_HEADER, TRUSTED_IPS_SETTING, trustedIPs, true))
                    missingSettings.add(new JtsIniSectionSetting(IBGATEWAY_SECTION_HEADER, TRUSTED_IPS_SETTING + "=" + trustedIPs));
            }
        }
        return missingSettings;
    }

    private static boolean findSettingAndLog(String section, String setting, String expectedValue, boolean updateIfDifferent) {
        int index = getSettingIndex(section, setting);
        
        if (index == -1) {
            Utils.logToConsole("Can't find setting: " + section + "/" + setting + (expectedValue.length() != 0 ? "=" + expectedValue : ""));
            return false;
        }

        String value = lines.get(index).substring(setting.length()+1) + "";

        if (!updateIfDifferent || value.equals(expectedValue)){
            Utils.logToConsole("Found setting: " + section + "/" + setting + "=" + value);
        } else {
            Utils.logToConsole("Found setting: " + section + "/" + setting + "=" + value + ": updating value to " + expectedValue);
            updateSetting(index, expectedValue);
        }
        return true;
    }
    
    private static void updateSetting(int index, String newValue) {
        String line = lines.get(index);
        String settingName = line.substring(0, line.indexOf("="));
        lines.set(index, settingName + "=" + newValue);
        settingsUpdated = true;
    }

    private static List<String> getFileLines(File jtsIniFile) {
        List<String> linesList = null;

        try {
            linesList = Files.readAllLines(jtsIniFile.toPath());
        } catch (IOException e) {
            Utils.exitWithError(ErrorCodes.IO_EXCEPTION_ON_JTSINI, 
                                "Unexpected IOException on " + jtsIniFile + ": " + e.getMessage());
        }
        return linesList;
    }

    private static void createMinimalFile() {
        Utils.logToConsole("Creating minimal " + jtsIniFilePath);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            writeIniFileLine(LOGON_SECTION_HEADER, w);
            writeIniFileLine(S3STORE_SETTING_TRUE, w);
            writeIniFileLine(LOCALE_SETTING_EN, w);
            writeIniFileLine(DISPLAYEDPROXYMSG_SETTING_1, w);
            writeIniFileLine(USESSL_SETTING_TRUE, w);

            writeIniFileLine(IBGATEWAY_SECTION_HEADER, w);
            writeIniFileLine(APIONLY_SETTING_TRUE, w);
        } catch (IOException e) {
            Utils.exitWithError(ErrorCodes.IO_EXCEPTION_ON_JTSINI, 
                                "Problem creating " + jtsIniFilePath + ": " + e.getMessage());
        }
    }

    private static void rewriteExistingFile(List<JtsIniSectionSetting> missingSettings) {
        Utils.logToConsole("Rewriting existing " + jtsIniFilePath);

        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            updateExistingSections(missingSettings, w);
            updateUnprocessedSettings(getUnprocessedSettings(missingSettings), w);
        } catch (IOException e){
            Utils.exitWithError(ErrorCodes.IO_EXCEPTION_ON_JTSINI, 
                                "Problem writing to " + jtsIniFilePath + ": " + e.getMessage());
        }
    }

    private static void updateExistingSections(
            List<JtsIniSectionSetting> missingSettings, 
            BufferedWriter w ) throws IOException {
        String currentSection = "";
        for (String line : lines) {
            if (line.length() != 0 && line.startsWith("[")) {
                writeMissingSettingsToSection(currentSection, missingSettings, w);
                currentSection = line;
            } 
            writeIniFileLine(line, w);
        }
        writeMissingSettingsToSection(currentSection, missingSettings, w);
    }

    private static List<JtsIniSectionSetting> getUnprocessedSettings(
            List<JtsIniSectionSetting> missingSettings) {
        return missingSettings.stream()
                               .filter(s -> !s.isProcessed)
                               .collect(toList());
    }

    private static void updateUnprocessedSettings(
            List<JtsIniSectionSetting> unprocessedSettings, 
            BufferedWriter w ) throws IOException {
        if (unprocessedSettings.isEmpty()) return;

        String currentSection = "";
        for (JtsIniSectionSetting s : unprocessedSettings){
            if (!s.section.equals(currentSection)) {
                currentSection = s.section;
                writeIniFileLine(currentSection, w);
            }
            writeIniFileLine(s.setting, w);
        }
    }

    private static void writeMissingSettingsToSection(
            String currentSection, 
            List<JtsIniSectionSetting> missingSettings,
            BufferedWriter w) throws IOException {

        if (currentSection.length() == 0) return;

        ListIterator<JtsIniSectionSetting> missingSettingsIt = missingSettings.listIterator();
        while (missingSettingsIt.hasNext()) {
            JtsIniSectionSetting missingSetting = missingSettingsIt.next();
            if (missingSetting.section.equals(currentSection)) {
                writeIniFileLine(missingSetting.setting, w);
                missingSetting.isProcessed = true;
            }
        }
    }

    private static void writeIniFileLine(String line, BufferedWriter w) throws IOException {
        Utils.logToConsole("    jts.ini: " + line);
        w.write(line);
        w.newLine();
    }

}
