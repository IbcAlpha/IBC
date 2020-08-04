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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import static java.util.stream.Collectors.toList;

class JtsIniManager {

    final static String LogonSectionHeader = "[Logon]";
    final static String IBGatewaySectionHeader = "[IBGateway]";
    final static String DisplayedProxyMsgSetting="displayedproxymsg";
    final static String DisplayedProxyMsgSetting_1=DisplayedProxyMsgSetting + "=1";
    final static String LocaleSetting = "Locale";
    final static String LocaleSetting_En = LocaleSetting + "=en";
    final static String S3storeSetting = "s3store";
    final static String S3storeSetting_False = S3storeSetting + "=false";
    final static String S3storeSetting_True = S3storeSetting + "=true";
    final static String ApiOnlySetting = "ApiOnly";
    final static String ApiOnlySetting_True = ApiOnlySetting + "=true";

    private static String jtsIniFilePath;
    private static File jtsIniFile;
    private static List<String> lines;

    /* when TWS starts, there must exist a jts.ini file in the TWS settings directory 
    *  containing at least the following minimum contents:
    *
    * [Logon]
    * Locale=en
    * displayedproxymsg=1
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

    static String getSetting(String section, String setting) {
        String key = setting + "=";
   
        boolean inSection = false;
        for (String l : lines) {
            if (l.compareTo(section) == 0) {
                inSection = true;
            } else if (l.startsWith("[")) {
                if (inSection) return "";
                inSection = false;
            } else if (inSection) {
                if (l.startsWith(key)) {
                    return l.substring(key.length()) + "";
                }
            }
        }

        return "";
    }

    static void reload() {
        loadIniFile();
    }

    private static void loadIniFile() {
        jtsIniFile = new File(jtsIniFilePath);
        if (jtsIniFile.isDirectory()) {
            Utils.exitWithError(ErrorCodes.ERROR_CODE_INVALID_JTSINI_PATH, 
                                jtsIniFilePath + " already exists but is a directory");
        }
        if (jtsIniFile.isFile()) {
            lines = getFileLines(jtsIniFile);
        }
    }

    private static void updateExistingFile() {
        Utils.logToConsole("Ensuring " + jtsIniFilePath + " contains required minimal lines");

        List<JtsIniSectionSetting> missingSettings = getMissingSettings();
        if (!missingSettings.isEmpty()) {
            Utils.logToConsole("Missing lines in " + jtsIniFilePath);
            jtsIniFile.delete();
            rewriteExistingFile(missingSettings);
        } else {
            Utils.logToConsole("Confirmed " + jtsIniFilePath + " contains required minimal lines");
        }
    }

    private static List<JtsIniSectionSetting> getMissingSettings() {
        List<JtsIniSectionSetting> missingSettings = new ArrayList<>();

        if (!findSettingAndLog(LogonSectionHeader, S3storeSetting, ""))
            missingSettings.add(new JtsIniSectionSetting(LogonSectionHeader, S3storeSetting_True));

        if (! findSettingAndLog(LogonSectionHeader, LocaleSetting, "en")) 
            missingSettings.add(new JtsIniSectionSetting(LogonSectionHeader, LocaleSetting_En));

        if (! findSettingAndLog(LogonSectionHeader, DisplayedProxyMsgSetting, "1"))
            missingSettings.add(new JtsIniSectionSetting(LogonSectionHeader, DisplayedProxyMsgSetting_1));

        if (! findSettingAndLog(IBGatewaySectionHeader, ApiOnlySetting, "true")) 
            missingSettings.add(new JtsIniSectionSetting(IBGatewaySectionHeader, ApiOnlySetting_True));

        return missingSettings;
    }

    private static boolean findSettingAndLog(String section, String setting, String expectedValue) {
        String value = getSetting(section, setting);
        boolean found = (value.length() != 0 && ((expectedValue.length() != 0) ? value.equals(expectedValue) : true));
        if (found) {
            Utils.logToConsole("Found setting: " + section + "/" + setting + "=" + value);
        } else {
            Utils.logToConsole("Can't find setting: " + section + "/" + setting + (expectedValue.length() != 0 ? "=" + expectedValue : ""));
        }
        return found;
    }

    private static List<String> getFileLines(File jtsIniFile) {
        List<String> linesList = null;

        try {
            linesList = Files.readAllLines(jtsIniFile.toPath());
        } catch (IOException e) {
            Utils.exitWithError(ErrorCodes.ERROR_CODE_IO_EXCEPTION_ON_JTSINI, 
                                "Unexpected IOException on " + jtsIniFile + ": " + e.getMessage());
        }
        return linesList;
    }

    private static void createMinimalFile() {
        Utils.logToConsole("Creating minimal " + jtsIniFilePath);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            writeIniFileLine(LogonSectionHeader, w);
            writeIniFileLine(S3storeSetting_True, w);
            writeIniFileLine(LocaleSetting_En, w);
            writeIniFileLine(DisplayedProxyMsgSetting_1, w);

            writeIniFileLine(IBGatewaySectionHeader, w);
            writeIniFileLine(ApiOnlySetting_True, w);
        } catch (IOException e) {
            Utils.exitWithError(ErrorCodes.ERROR_CODE_IO_EXCEPTION_ON_JTSINI, 
                                "Problem creating " + jtsIniFilePath + ": " + e.getMessage());
        }
    }

    private static void rewriteExistingFile(List<JtsIniSectionSetting> missingSettings) {
        Utils.logToConsole("Rewriting existing " + jtsIniFilePath);

        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            updateExistingSections(missingSettings, w);
            updateUnprocessedSettings(getUnprocessedSettings(missingSettings), w);
        } catch (IOException e){
            Utils.exitWithError(ErrorCodes.ERROR_CODE_IO_EXCEPTION_ON_JTSINI, 
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
