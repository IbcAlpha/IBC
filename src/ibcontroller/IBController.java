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

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.imageio.IIOException;
import javax.transaction.InvalidTransactionException;


/**
 * @author stevek
 *
 * This is our way of automating the TWS app so it does not require human interaction.
 * IBController is a class whose main starts up the TWS api, and which
 * monitors the application for certain events, such as the login dialog,
 * after which it can automatically respond to these events.
 * Upon seeing the login dialog, it fills out the username and pwd and presses the button.
 * Upon seeing the "allow incoming connection dialog it presses the yes button.
 *
 * This code is based original code by Ken Geis (ken_geis@telocity.com).
 *
 * Amendment history:
 *   Date     Author	           Change # Description
 *   -------- -------------------- -------- -----------
 *   20040316 Richard King         1        Handle new .ini file entry IbAutoClosedown.
 *   20040316 Richard King         2        Handle "Accept incoming connection" dialog with changed title.
 *   20040802 Steven Kearns        3        Incorporated Richard King's changes into my latest version.
 *   20060203 Richard King         4        Changed the string used for recognising the "Newer Version" dialog.
 *   20060203 Richard King         5        Changed the way that the Exit Session Setting dialog handling
 *                                          works. It now sets the autologoff time to 5 minutes before the
 *                                          current time.
 *  20060404 Richard King          6        Changed to cater for the new Login dialog title in TWS 857 (was
 *                                          "New Login", now "Login").
 *  20060404 Richard King          7        Added an option to specify a day of the week and a time when
 *                                          IBController should tidily close TWS and shut down.
 *  20060404 Richard King          8        Fixed an unimportant bug in the handling of the TWS Exit Session
 *                                          dialogue. Where the time configured in TWS for autologoff was
 *                                          within five minutes after midnight, IBController would set it to
 *                                          just before midday instead of just before midnight.
 *  20070205 Richard King          9        Fixed another unimportant bug in the TWS Exit Session dialog
 *                                          handler.
 *  20070205 Richard King          10       Now caters for the variant of the TWS 'Newer version' dialog that
 *                                          has Yes and No buttons instead of an Ok button.
 *  20070226 Richard King          11       Fixed a bug in the implementation of the WAITINIT command.
 *  20070226 Richard King          12       Fixed a bug in ConfigureApiTask.
 *  20070226 Richard King          13       Username and password can now be supplied as args[1] and args[2]
 *                                          rather than via the .ini file. Password must not be encrypted.
 *  20070305 Richard King          14       Removed the check for null title in 'newer version' dialogs because
 *                                          Linux TWSs sometimes display this dialog with a non-null title.
 *  20070305 Richard King          15       For the Linux versions of the 'newer version' dialog, click 'No'
 *                                          instead of 'Yes'.
 *  20070305 Richard King          16       Added private static final long serialVersionUID to ScriptProperties.java
 *                                          to avoid a warning given by some compilers.
 *  20070903 Richard King          17       Removed the test for !wasOpen when checking for the Exit Session Setting dialog.
 *                                 18       Added a PasswordEncypted option. If set to 'no', the password
 *                                          in the .ini file is treated as not encrypted. The default is 'yes'.
 *                                 19       Added timestamps to system.out writes.
 *  20071213 Richard King          20       Now recognises the German Exit Session dialogue.
 *  20100201 Richard King          21       The code has been completely refactored to improve readability and
 *                                          maintainability. Some redundant code has been removed.
 *                                 22       Added a MinimizeMainWindow option. Setting this to 'yes' causes the
 *                                          TWS main window to be minimised when TWS is started. The default is 'no'.
 *                                 23       Added an AllowBlindTrading option. Setting this to 'yes' causes the
 *                                          warning message output by TWS to be dismissed (by clicking the 'Yes'
 *                                          button) when attempting to place an order for a contract for which the
 *                                          user has no market data subscription. The default is 'no'.
 *                                 24       Added a StoreSettingsOnServer option. Setting this to 'yes' sets the
 *                                          corresponding option in the TWS login dialog, resulting in TWS settings
 *                                          being stored on IB's servers. The default is 'no'.
 *                                 25       The WAITINIT command to IBController Server has been changed to ENABLEAPI.
 *  20100204 Richard King          26       Changed so that if username and/or password are not supplied in either the
 *                                          commnd line args or the config file, then TWS prompts for them rather than
 *                                          IBController shutting down with an error.
 *  20100317 Richard King          27       Changed to handle the new Tip of the Day dialog in TWS 903 which has
 *                                          a different title.
 *  20100322 Richard King          28       Login processing has been modified to enable and click the Login button
 *                                          after a brief delay, because TWS 903 no longer enables the Login button
 *                                          when the username and password fields are filled in programatically.
 *  20100329 Richard King          29       Fixed a bug where the ENABLEAPI command was not handled correctly if
 *                                          issued before TWS's main window was loaded.
 *  20100331 Richard King          30       Fixed a bug where streams of error messages were written to System.err
 *                                          if an IBControllerServer client disconnected by resetting the connection
 *                                          (as opposed to using the Exit command).
 *  20100419 Richard King          31       TWS 903.7 has changed the 'Newer version' notification from a JDialog to
 *                                          a JFrame. The NewerVersionFrameHandler class has been added to handle this.
 *  20100419 Richard King          32       Improved Change # 28 to keep periodically clicking the Login button after
 *                                          enabling it, until it becomes disabled. This overcomes the problem that the
 *                                          necessary delay time in the original approach was unknown, and seemed to vary
 *                                          from machine to machine and from time to time.
 *  20100420 Richard King          33       Support for the IB Gateway added. Only the IB API mode of the gateway
 *                                          is supported, not the FIX mode.
 *  20100420 Richard King          34       Fixed a bug where the tidy shutdown time was not accurately observed.
 *  20100521 Richard King          35       Changed the Gateway login handler to periodically click the login button
 *                                          until the login dialog becomes invisible: some users found that the first
 *                                          click on the login button had no effect
 *  20100728 Richard King          36       Addressed some threading and synchronization issues. Thanks to Brent Boyer
 *                                          for pointing them out.
 *                                 37       Commands to IBControllerServer now produce the following responses:
 *                                              OK info
 *                                              ERROR info
 *                                              INFO info
 *                                          Where 'info' is a text string. OK and ERROR are final responses. INFO is an
 *                                          intermediate response that may be sent to provide information about the command's
 *                                          progress. INFO's may be suppressed using the SuppressInfoMessages option (see change 39).
 *                                 38       Added a CommandPrompt option. The specified string is output by the server when
 *                                          the connection to IBControllerServer is first opened and after the completion
 *                                          of each command. If no string is specified, no prompt is issued.
 *                                 39       Added a SuppressInfoMessages option. If set to 'yes', only the final response from
 *                                          a command to IBControllerServer is sent - any intermediate information messages
 *                                          are suppressed. The default is 'yes'.
 *  20100816 Richard King          40       Added handlers for the Password Notice and NSE Compliance dialogs displayed by
 *                                          Indian versions of TWS. By default the Password Notice is not dismissed, but the
 *                                          NSE Compliance notice is dismissed. This behaviour may be overridden using the
 *                                          new DismissPasswordExpiryWarning and DismissNSEComplianceNotice .ini file options.
 *  20100902 Richard King          41       Changed MainWindowFrameHandler to recognise the new main window title introduced
 *                                          in TWS 907.
 *  20100905 Shane Cusson          42       Added GPL licensing, added to SourceForge.net
 *  20110623 Richard King          43       Changed some window handlers to reflect changes in titles etc in TWS 918.6.
 *                                 44       Enhanced the ENABLEAPI command implementation to cater for the fact that
 *                                          the Configure top-level menu was removed in TWS 909. API configuration can
 *                                          now only be done via the Edit > Global Configuration... menu.
 *                                 45       Added an AutoConfirmOrders option. If set to yes, then when orders are placed using the
 *                                          BookTrader in TWS, the confirmation dialog is automatically handled, thereby
 *                                          effectively restoring the one-click trading that was removed in TWS 906. The default
 *                                          is 'no', requiring the user to manually confirm each trade.
 *                                 46       Fixed the NewerVersionDialogHandler: the text to be searched for (in current TWS versions)
 *                                          is contained in a JOptionPane, not a JLabel.
 *  20131218 Richard King          47       Modified the AcceptIncomingConnectionDialogHandler to not check the contents of the 
 *                                          title bar, since this varies with different versions of TWS and is not necessary
 *                                          to successfully identify the dialog.
 *                                 48       Added an AcceptIncomingConnectionAction setting. If set to 'accept', IBController
 *                                          automatically accepts the incoming connection request. If set to 'reject', IBController
 *                                          automatically rejects the incoming connection request. If set to 'manual', IBController
 *                                          does nothing and the user must decide whether to accept or reject the incoming connection 
 *                                          request. The default is 'accept'.
 *                                 49       Improved handling of the Exit Session Setting dialog. In TWS 942, the caption is only included
 *                                          the first time the dialog is displayed. However TWS always displays the same instance
 *                                          of the dialog, so a reference to the dialog is stored the first time it is displayed, and
 *                                          is used to detect subsequent displays.
 *                                 50       Added a ShowAllTrades setting. If this is set to yes, IBController causes TWS to display the 
 *                                          Trades log at startup, and sets the 'All' checkbox to ensure that the API reports all executions
 *                                          that have occurred during the past week. Moreover, any attempt by the user to change any of the 
 *                                          'Show trades' checkboxes is ignored; similarly if the user closes the Trades log, it is 
 *                                          immediately re-displayed with the 'All' checkbox set. If set to 'no', IBController does not
 *                                          interact with the Trades log. The default is no.
 *                                 51       Added RECONNECTACCOUNT and RECONNECTDATA commands. RECONNECTACCOUNT causes TWS to disconnect from
 *                                          the IB account server and then reconnect (the same as the user pressing Ctrl-Alt-R). 
 *                                          RECONNECTDATA causes TWS to disconnect from all market data farms and then reconnect (the same 
 *                                          as the user pressing Ctrl-Alt-F). Thanks to Cheung Kwok Fai for suggesting this and supplying the
 *                                          relevant code edits.
 *                                 52       Added an ExistingSessionDetectedAction setting. When TWS logs on it checks to see whether the 
 *                                          account is already logged in. If so it displays a dialog: this setting instructs TWS how to proceed. If set
 *                                          to 'primary', TWS ends the other session and continues with the new session. If set to
 *                                          'secondary', TWS exits so that the other session is unaffected. If set to 'manual', the user must 
 *                                          handle the dialog. The default is 'manual'.
 *                                 53       Change # 45 above has been removed because firstly, it was not correctly implemented, and
 *                                          secondly current versions of TWS enable the user to instruct TWS not to show the order
 *                                          confirmation dialog. The legal restrictions that resulted in one-click trading via the BookTrader
 *                                          being removed in TWS906 appear to have been lifted.
 *                                 54       Added a LogToConsole setting. If set to 'yes', all logging output from IBController is to the console
 *                                          and may be directed into a file using the normal > or >> command line redirection operators. If set to 'no', 
 *                                          output from IBController that is logged after it has loaded TWS appears in the TWS logfile. The default is 'no'.
 *  20140228 Richard King          55       Added the ability to run the FIX CTCI gateway. There are these new settings:
 *                                                  FIX                     if yes, use the FIX CTCI login, otherwise the IB API gateway login (default no)
 *                                                  FIXLoginId              username for the FIX account
 *                                                  FIXPassword             password for the FIX account
 *                                                  FIXPasswordEncrypted    yes or no (default yes)
 *                                          If market data connection via the gateway is also needed, the existing IbLoginId and IbPassword settings
 *                                          are used as well as the FIX settings.
 *                                          The FIX username and password may also be supplied as the second and third command line args. In
 *                                          this case, the market data connection username and password may be supplied as the fourth and
 *                                          fifth command line args.
 * 
 * With the move to Github, the value of recording details of amendments here is questionable, and this practice has therefore been 
 * discontinued.
 * 
 */

public class IBController {

    /**
     * starts up the TWS app.
     * @param args -
	 *    If length == 1, then args[0] is the path to the ini file.
	 *    If length == 0, we assume that the ini file is located in the current user
     *                    directory in a file called "IBController.ini".
	 *    If length == 2 and args[0] is "encrypt", we print out the encryption of args[1].
     * @throws java.lang.Exception
     */

    private IBController() { }

    public static void main(final String[] args) throws Exception {
        checkArguments(args);
        setupDefaultEnvironment(args, false);
        load();
    }
    
    static void setupDefaultEnvironment(final String[] args, final boolean isGateway) throws Exception {
        Settings.initialise(new DefaultSettings(args));
        LoginManager.initialise(new DefaultLoginManager(args));
        MainWindowManager.initialise(new DefaultMainWindowManager(isGateway));
        TradingModeManager.initialise(new DefaultTradingModeManager(args));
    }

    static void checkArguments(String[] args) {
        /**
         * Allowable parameter combinations:
         * 
         * 1. No parameters
         * 
         * 2. ENCRYPT <password>
         * 
         * 3. <iniFile> [<tradingMode>]
         * 
         * 4. <iniFile> <apiUserName> <apiPassword> [<tradingMode>]
         * 
         * 5. <iniFile> <fixUserName> <fixPassword> <apiUserName> <apiPassword> [<tradingMode>]
         * 
         * where:
         * 
         *      <iniFile>       ::= NULL | path-and-filename-of-.ini-file 
         * 
         *      <tradingMode>   ::= blank | LIVETRADING | PAPERTRADING
         * 
         *      <apiUserName>   ::= blank | username-for-TWS
         * 
         *      <apiPassword>   ::= blank | password-for-TWS
         * 
         *      <fixUserName>   ::= blank | username-for-FIX-CTCI-Gateway
         * 
         *      <fixPassword>   ::= blank | password-for-FIX-CTCI-Gateway
         * 
         */
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("encrypt")) {
                Utils.logRawToConsole("========================================================================");
                Utils.logRawToConsole("");
                Utils.logToConsole("encryption of \"" + args[1] + "\" is \"" +
                               Encryptor.encrypt(args[1]) + "\"");
                Utils.logRawToConsole("");
                Utils.logRawToConsole("========================================================================");
                System.exit(0);
            }
        } else if (args.length > 6) {
            Utils.logError("Incorrect number of arguments passed. quitting...");
            Utils.logRawToConsole("Number of arguments = " +args.length);
            for (String arg : args) {
                Utils.logRawToConsole(arg);
            }
            System.exit(1);
        }
    }

    public static void load() {
        printProperties();
        
        Settings.settings().logDiagnosticMessage();
        LoginManager.loginManager().logDiagnosticMessage();
        MainWindowManager.mainWindowManager().logDiagnosticMessage();
        TradingModeManager.tradingModeManager().logDiagnosticMessage();
        ConfigDialogManager.configDialogManager().logDiagnosticMessage();
        
        boolean isGateway = MainWindowManager.mainWindowManager().isGateway();
        
        startIBControllerServer(isGateway);

        startShutdownTimerIfRequired(isGateway);

        createToolkitListener();
        
        startSavingTwsSettingsAutomatically();

        startTwsOrGateway(isGateway);
    }

    private static void createToolkitListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new TwsListener(createWindowHandlers()), AWTEvent.WINDOW_EVENT_MASK);
    }

    private static List<WindowHandler> createWindowHandlers() {
        List<WindowHandler> windowHandlers = new ArrayList<WindowHandler>();

        windowHandlers.add(new AcceptIncomingConnectionDialogHandler());
        windowHandlers.add(new BlindTradingWarningDialogHandler());
        windowHandlers.add(new ExitSessionFrameHandler());
        windowHandlers.add(new LoginFrameHandler());
        windowHandlers.add(new GatewayLoginFrameHandler());
        windowHandlers.add(new MainWindowFrameHandler());
        windowHandlers.add(new GatewayMainWindowFrameHandler());
        windowHandlers.add(new NewerVersionDialogHandler());
        windowHandlers.add(new NewerVersionFrameHandler());
        windowHandlers.add(new NotCurrentlyAvailableDialogHandler());
        windowHandlers.add(new TipOfTheDayDialogHandler());
        windowHandlers.add(new NSEComplianceFrameHandler());
        windowHandlers.add(new PasswordExpiryWarningFrameHandler());
        windowHandlers.add(new GlobalConfigurationDialogHandler());
        windowHandlers.add(new TradesFrameHandler());
        windowHandlers.add(new ExistingSessionDetectedDialogHandler());
        windowHandlers.add(new ApiChangeConfirmationDialogHandler());
        windowHandlers.add(new SplashFrameHandler());
        windowHandlers.add(new SecurityCodeDialogHandler());
        windowHandlers.add(new ReloginDialogHandler());
        windowHandlers.add(new NonBrokerageAccountDialogHandler());
        
        return windowHandlers;
    }
    
    private static Date getShutdownTime() {
        String shutdownTimeSetting = Settings.settings().getString("ClosedownAt", "");
        if (shutdownTimeSetting.length() == 0) {
            return null;
        } else {
            int shutdownDayOfWeek;
            int shutdownHour;
            int shutdownMinute;
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime((new SimpleDateFormat("E HH:mm")).parse(shutdownTimeSetting));
                shutdownDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                shutdownHour = cal.get(Calendar.HOUR_OF_DAY);
                shutdownMinute = cal.get(Calendar.MINUTE);
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.set(Calendar.HOUR_OF_DAY, shutdownHour);
                cal.set(Calendar.MINUTE, shutdownMinute);
                cal.set(Calendar.SECOND, 0);
                cal.add(Calendar.DAY_OF_MONTH,
                        (shutdownDayOfWeek + 7 -
                         cal.get(Calendar.DAY_OF_WEEK)) % 7);
                if (!cal.getTime().after(new Date())) {
                    cal.add(Calendar.DAY_OF_MONTH, 7);
                }
            } catch (ParseException e) {
                Utils.logError("Invalid ClosedownAt setting: should be: <day hh:mm>   eg Friday 22:00");
                System.exit(1);
            }
            return cal.getTime();
        }
    }

    private static String getTWSSettingsDirectory() {
        return Settings.settings().getString("IbDir", System.getProperty("user.dir"));
    }

    private static void printProperties() {
        Properties p = System.getProperties();
        Enumeration<Object> i = p.keys();
        Utils.logRawToConsole("System Properties");
        Utils.logRawToConsole("------------------------------------------------------------");
        while (i.hasMoreElements()) {
            String props = (String) i.nextElement();
            Utils.logRawToConsole(props + " = " + (String) p.get(props));
        }
        Utils.logRawToConsole("------------------------------------------------------------");
    }

    private static void startGateway() {
        String[] twsArgs = new String[1];
        twsArgs[0] = getTWSSettingsDirectory();
        Utils.logToConsole("TWS settings directory is " + twsArgs[0]);
        try {
            ibgateway.GWClient.main(twsArgs);
        } catch (Throwable t) {
            Utils.logError("Can't find the Gateway entry point: ibgateway.GWClient.main. Gateway is not correctly installed.");
            t.printStackTrace(Utils.getErrStream());
            System.exit(1);
        }
    }

    private static void startIBControllerServer(boolean isGateway) {
        MyCachedThreadPool.getInstance().execute(new IBControllerServer(isGateway));
    }

    private static void startShutdownTimerIfRequired(boolean isGateway) {
        Date shutdownTime = getShutdownTime();
        if (! (shutdownTime == null)) {
            long delay = shutdownTime.getTime() - System.currentTimeMillis();
            Utils.logToConsole((isGateway ? "Gateway" : "TWS") +
                            " will be shut down at " +
                           (new SimpleDateFormat("yyyy/MM/dd HH:mm")).format(shutdownTime));
            MyScheduledExecutorService.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    MyCachedThreadPool.getInstance().execute(new StopTask(null));
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private static void startTws() {
        ensureJtsIniExists();
        if (Settings.settings().getBoolean("ShowAllTrades", false)) {
            Utils.showTradesLogWindow();
        }
        String[] twsArgs = new String[1];
        twsArgs[0] = getTWSSettingsDirectory();
        Utils.logToConsole("TWS settings directory is " + twsArgs[0]);
        try {
            jclient.LoginFrame.main(twsArgs);
        } catch (Throwable t) {
            Utils.logError("Can't find the TWS entry point: jclient.LoginFrame.main; TWS is not correctly installed.");
            t.printStackTrace(Utils.getErrStream());
            System.exit(1);
        }
    }
    
    private static void ensureJtsIniExists() {
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
        * Note that this is not a problem for the Gateway, which doesn't provide the 
        * option to store the settings on the server.
        *
        */
        File jtsIniFile = getJtsIniFile();
        if (jtsIniFile.isFile()) {
            updateExistingJtsIniFile(jtsIniFile);
        } else {
            createMinimalJtsIniFile(jtsIniFile);
        }
    }
    
    private static File getJtsIniFile() {
        String jtsIniPath = getTWSSettingsDirectory() + File.separatorChar + "jts.ini";
        File jtsIniFile = new File(jtsIniPath);
        if (jtsIniFile.isDirectory()) {
            Utils.logError(jtsIniPath + " already exists but is a directory");
            System.exit(1);
        }
        return jtsIniFile;
    }
    
    private static void updateExistingJtsIniFile(File jtsIniFile) {
        Utils.logToConsole("Ensuring " + jtsIniFile.getPath() + " contains s3store=true");

        List<String> lines = getJtsIniFileLines(jtsIniFile);
        jtsIniFile.delete();
        rewriteExistingJtsIniFileLines(jtsIniFile, lines);
    }
    
    private static List<String> getJtsIniFileLines (File jtsIniFile) {
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
    
    private static void createMinimalJtsIniFile(File jtsIniFile) {
        Utils.logToConsole("Creating minimal " + jtsIniFile.getPath());
        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            writeRequiredJtsIniFileLines(w, jtsIniFile);
        } catch (IOException e) {
            Utils.logError("Problem creating " + jtsIniFile.getPath() + ": " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void writeRequiredJtsIniFileLines(BufferedWriter w, File jtsIniFile) {
        Utils.logToConsole("Writing required lines to "  + jtsIniFile.getPath());
        try {
            w.write("[Logon]");
            w.newLine();
            w.write("s3store=true");
            w.newLine();
        } catch (IOException e) {
            Utils.logError("Problem writing to " + jtsIniFile.getPath() + ": " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void rewriteExistingJtsIniFileLines(File jtsIniFile, List<String> lines) {
        boolean foundLogon = false;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(jtsIniFile))) {
            for (String l:lines) {
                if (l.compareTo("[Logon]") == 0) {
                    foundLogon = true;
                    writeRequiredJtsIniFileLines(w, jtsIniFile);
                } else if (l.compareTo("s3store=true") != 0) {
                    w.write(l);
                    w.newLine();
                }
            }
            if (! foundLogon) {
                writeRequiredJtsIniFileLines(w, jtsIniFile);
            }
        } catch (IOException e){
            Utils.logError("Problem writing to " + jtsIniFile.getPath() + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static void startTwsOrGateway(boolean isGateway) {
        if (isGateway) {
            startGateway();
        } else {
            startTws();
        }

        int portNumber = Settings.settings().getInt("ForceTwsApiPort", 0);
        if (portNumber != 0) MyCachedThreadPool.getInstance().execute(new ConfigureTwsApiPortTask(portNumber, isGateway));

        Utils.sendConsoleOutputToTwsLog(!Settings.settings().getBoolean("LogToConsole", false));
    }
    
    private static void startSavingTwsSettingsAutomatically() {
        TwsSettingsSaver.getInstance().initialise();
    }

}

