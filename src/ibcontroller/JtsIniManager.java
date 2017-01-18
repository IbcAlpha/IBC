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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

class JtsIniManager {
    
    final static String LogonSectionHeader = "[Logon]";
    final static String IBGatewaySectionHeader = "[IBGateway]";
    final static String S3storeSetting = "s3store";
    static String S3storeFalseSetting = S3storeSetting + "=false";
    static String S3storeTrueSetting = S3storeSetting + "=true";
    final static String ApiOnlySetting = "ApiOnly";
    final static String ApiOnlyTrueSetting = ApiOnlySetting + "=true";

    private static String jtsIniFilePath;
    private static File jtsIniFile;
    private static List<String> lines;
    
    static void initialise(String jtsIniPath) {
        jtsIniFilePath = jtsIniPath;
    }
    
    /* when TWS starts, there must exist a jts.ini file in the TWS settings directory 
    *  containing at least the following minimum contents:
    *
    * [Logon]
    * s3store=true
    *
    * If this file doesn't exist, or doesn't contain these lines, then TWS won't 
    * include the 'Store settings on server' checkbox in the login dialog, which
    * prevents IBController properly handling the StoreSettingsOnServer ini file
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
    * IBController doesn't expect, and it can't find the trading mode selector.
    *
    * [IBGateway]
    * ApiOnly=true
    *
    * To avoid these problems, IBController ensures that a jts.ini exists which 
    * contains at least both sets of lines shown above, with the exception that if it 
    * already exists and contains s3store=false then this setting will not be altered.
    */
    static void ensureValidJtsIniFile() {
        loadIniFile();
        if (jtsIniFile.isFile()) {
            updateExistingFile();
        } else {
            createMinimalFile();
        }
    }
    
    static String getSetting(String section, String setting) {
        String key = setting + "=";
        boolean found = false;
        String l = "";

        ListIterator<String> it = lines.listIterator();
        while (it.hasNext() && ! found) {
            l = it.next();
            if (l.compareTo(section) == 0) {
                while (it.hasNext() && ! found) {
                    l = it.next();
                    if (l.startsWith(key)) found = true;
                    if (l.startsWith("[")) return null;
                }
            } 
        }
        
        if (! found) return null;
        String value = l.substring(key.length());
        return value;
    }
    
    static void reload() {
        loadIniFile();
    }
    
    private static void loadIniFile() {
        jtsIniFile = new File(jtsIniFilePath);
        if (jtsIniFile.isDirectory()) {
            Utils.logError(jtsIniFilePath + " already exists but is a directory");
            System.exit(1);
        }
        if (jtsIniFile.isFile()) {
            lines = getFileLines(jtsIniFile);
        }
    }
    
    private static void updateExistingFile() {
        Utils.logToConsole("Ensuring " + jtsIniFile.getPath() + " contains required minimal lines");

        if (!existingFileOk()) {
            Utils.logToConsole("Missing lines in " + jtsIniFile.getPath());
            jtsIniFile.delete();
            rewriteExistingFile();
        } else {
            Utils.logToConsole("Confirmed " + jtsIniFile.getPath() + " contains required minimal lines");
        }
    }
    
    private static boolean existingFileOk() {
        return (findSettingInSection(LogonSectionHeader, S3storeFalseSetting) || 
                findSettingInSection(LogonSectionHeader, S3storeTrueSetting)) &&
                findSettingInSection(IBGatewaySectionHeader, ApiOnlyTrueSetting);
    }
    
    private static boolean findSettingInSection(String section, String setting) {
        ListIterator<String> it = lines.listIterator();
        boolean error = false;
        while (it.hasNext() && ! error) {
            String l = it.next();
            if (l.compareTo(section) == 0) {
                Utils.logToConsole("Found section: " + section);
                while (it.hasNext() && ! error) {
                    l = it.next();
                    if (l.startsWith("[")) {
                        error = true;
                    } else if (l.compareTo(setting) == 0) {
                        Utils.logToConsole("Found setting: " + setting);
                        return true;
                    }
                }
            } 
        }
        return false;
    }
    
    private static List<String> getFileLines (File jtsIniFile) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new FileReader(jtsIniFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            Utils.logError("Unexpected IOException on " + jtsIniFile + ": " + e.getMessage());
            System.exit(1);
        }
        return lines;
    }
    
    private static void createMinimalFile() {
        Utils.logToConsole("Creating minimal " + jtsIniFile.getPath());
        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            writeLogonSectionHeader(w);
            writeS3store(w);
            writeIBGatewaySectionHeader(w);
            writeApiOnly(w);
        } catch (IOException e) {
            Utils.logError("Problem creating " + jtsIniFile.getPath() + ": " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void rewriteExistingFile() {
        Utils.logToConsole("Rewriting existing " + jtsIniFile.getPath());
        boolean foundLogon = false;
        boolean foundIBGateway = false;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            ListIterator<String> it = lines.listIterator();
            int index = 0;
            while (it.hasNext()) {
                index++;
                String l = it.next();
                if (l.compareTo(LogonSectionHeader) == 0) {
                    foundLogon = true;
                    index = processSection(lines, index, new String[] {S3storeTrueSetting, S3storeFalseSetting}, w);
                    it = lines.listIterator(index);
                } else if (l.compareTo(IBGatewaySectionHeader) == 0) {
                    foundIBGateway = true;
                    index = processSection(lines, index, new String[] {ApiOnlyTrueSetting}, w);
                    it = lines.listIterator(index);
                } else {
                    w.write(l);
                    w.newLine();
                }
            }
            if (! foundLogon) {
                writeLogonSectionHeader(w);
                writeS3store(w);
            }
            if (! foundIBGateway) {
                writeIBGatewaySectionHeader(w);
                writeApiOnly(w);
            }
        } catch (IOException e){
            Utils.logError("Problem writing to " + jtsIniFile.getPath() + ": " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static int processSection(
            List<String> lines, 
            int startIndex, 
            String[] settings,  
            BufferedWriter w) throws IOException {
        int index = startIndex;
        boolean found = false;
        ListIterator<String> it = lines.listIterator(index);
        while (it.hasNext()) {
            index++;
            String l = it.next();
            for (String s : settings) {
                if (l.compareTo(s) == 0) {
                    found = true;
                    break;
                }
            }
            if (l.startsWith("[")) {
                if (! found) w.write(settings[0]);
                break;
            } else {
                w.write(l);
                w.newLine();
            }
        }
        return index;
    }

    private static void writeApiOnly(BufferedWriter w) throws IOException {
        w.write(ApiOnlySetting);
        w.newLine();
    }
    
    private static void writeS3store(BufferedWriter w) throws IOException {
        w.write(S3storeTrueSetting);
        w.newLine();
    }
    
    private static void writeLogonSectionHeader(BufferedWriter w) throws IOException {
        w.write(LogonSectionHeader);
        w.newLine();
    }
    
    private static void writeIBGatewaySectionHeader(BufferedWriter w) throws IOException {
        w.write(IBGatewaySectionHeader);
        w.newLine();
    }
    
}
