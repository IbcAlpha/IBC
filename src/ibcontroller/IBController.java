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
import java.io.File;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;


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
 *                                          progess. INFO's may be suppressed using the SuppressInfoMessages option (see change 39).
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
 */

public class IBController {

    /**
     * starts up the TWS app.
     * @param args -
	 *    If length == 1, then args[0] is the path to the ini file.
	 *    If length == 0, we assume that the ini file is located in the current user
     *                    directory in a file called "IBController.ini".
	 *    If length == 2 and args[0] is "encrypt", we print out the encryption of args[1].
     */
    public static void main(String[] args) {
        load(args, false);
    }

    static void load(String[] args, boolean gatewayOnly) {
        _GatewayOnly = gatewayOnly;

        printProperties();

        checkArguments(args);

        getSettings(args);

        getTWSUserNameAndPassword(args);

        startIBControllerServer();

        startShutdownTimerIfRequired();

        createToolkitListener();

        startTwsOrGateway();
    }

    public IBController() {
        super();
    }

    private static boolean _GatewayOnly;

    /**
     * timer to shutdown at configured day and time
     */
    private static final Timer _Timer = new Timer(true);

    /**
     * username - can either be supplied from the .ini file or as args[1]
     * NB: if username is supplied in args[1], then the password must
     * be in args[2]. If username is supplied in .ini, then the password must
     * also be in .ini.
     */
    private static String _UserName;

    /**
     * unencrypted password - can either be supplied from the .ini file or as args[2]
     */
    private static String _Password;

    private static final List<WindowHandler> _WindowHandlers = new ArrayList<WindowHandler>();

    static {
        createWindowHandlers();
    }

    private static void checkArguments(String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("encrypt")) {
                System.out.println("========================================================================");
                System.out.println("");
                Utils.logToConsole("encryption of \"" + args[1] + "\" is \"" +
                               Encryptor.encrypt(args[1]) + "\"");
                System.out.println("");
                System.out.println("========================================================================");
                System.exit(0);
            } else {
                System.err.println("IBController: 2 arguments passed, but args[0] is not 'encrypt'. quitting...");
                System.exit(1);
            }
        }
    }

    private static void createToolkitListener() {
        TwsListener.initialise(_UserName, _Password, _WindowHandlers);
        Toolkit.getDefaultToolkit().addAWTEventListener(TwsListener.getInstance(), AWTEvent.WINDOW_EVENT_MASK);
    }

    private static void createWindowHandlers() {
        _WindowHandlers.add(new AcceptIncomingConnectionDialogHandler());
        _WindowHandlers.add(new BlindTradingWarningDialogHandler());
        _WindowHandlers.add(new ExitSessionFrameHandler());
        _WindowHandlers.add(new LoginFrameHandler());
        _WindowHandlers.add(new GatewayLoginFrameHandler());
        _WindowHandlers.add(new MainWindowFrameHandler());
        _WindowHandlers.add(new GatewayMainWindowFrameHandler());
        _WindowHandlers.add(new NewerVersionDialogHandler());
        _WindowHandlers.add(new NewerVersionFrameHandler());
        _WindowHandlers.add(new NotCurrentlyAvailableDialogHandler());
        _WindowHandlers.add(new TipOfTheDayDialogHandler());
        _WindowHandlers.add(new NSEComplianceFrameHandler());
        _WindowHandlers.add(new PasswordExpiryWarningFrameHandler());
        _WindowHandlers.add(new GlobalConfigurationDialogHandler());
        _WindowHandlers.add(new OrderConfirmationDialogHandler());
    }

    private static void getSettings(String[] args) {
        String iniPath;
        if (args.length == 0 || args[0].equals("NULL")) {
            iniPath = getWorkingDirectory() + "IBController." + getComputerUserName() + ".ini";
        } else {// args.length >= 1
            iniPath = args[0];
        }
        File finiPath = new File(iniPath);
        if (!finiPath.isFile() || !finiPath.exists()) {
            System.err.println("IBController: ini file \"" + iniPath +
                               "\" either does not exist, or is a directory.  quitting...");
            System.exit(1);
        }
        Utils.logToConsole("ini file is " + iniPath);
        Settings.load(iniPath);
    }

    private static Date getShutdownTime() {
        String shutdownTimeSetting = Settings.getString("ClosedownAt", "");
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
                System.err.println("Invalid ClosedownAt setting: should be: <day hh:mm>   eg Friday 22:00");
                System.exit(1);
            }
            return cal.getTime();
        }
    }

    private static String getTWSPasswordFromProperties() {
        String password = Settings.getString("IbPassword", "");
        if (password.length() != 0) {
            if (isPasswordEncrypted()) password = Encryptor.decrypt(password);
        }
        return password;
    }

    private static String getTWSSettingsDirectory() {
        String dir = Settings.getString("IbDir", "");
        if (dir.length() == 0) {
            System.err.println("IBController:  missing IbDir= entry in IBController.ini.  quitting...");
            System.exit(1);
        }
        return dir;
    }

    private static void getTWSUserNameAndPassword(String[] args) {
        if (! getTWSUserNameAndPasswordFromArguments(args)) {
            getTWSUserNameAndPasswordFromProperties();
        }
    }

    private static String getTWSUserNameFromProperties() {
        return Settings.getString("IbLoginId", "");
    }

    private static boolean getTWSUserNameAndPasswordFromArguments(String[] args) {
        if (args.length == 3) {
            _UserName = args[1];
            _Password = args[2];
            return true;
        } else {
            return false;
        }
    }

    private static void getTWSUserNameAndPasswordFromProperties() {
        _UserName = getTWSUserNameFromProperties();
        _Password = getTWSPasswordFromProperties();
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

    private static boolean isPasswordEncrypted() {
        return Settings.getBoolean("PasswordEncrypted", true);
    }

    private static void printProperties() {
        Properties p = System.getProperties();
        Enumeration i = p.keys();
        System.out.println("System Properties");
        System.out.println("------------------------------------------------------------");
        while (i.hasMoreElements()) {
            String props = (String) i.nextElement();
            System.out.println(props + " = " + (String) p.get(props));
        }
        System.out.println("------------------------------------------------------------");
    }

    private static void startGateway() {
        String[] twsArgs = new String[1];
        twsArgs[0] = getTWSSettingsDirectory();
        ibgateway.GWClient.main(twsArgs);
    }

    private static void startIBControllerServer() {
        Executor executor = new ThreadPerTaskExecutor();
        executor.execute(new IBControllerServer(_GatewayOnly));
    }

    private static void startShutdownTimerIfRequired() {
        Date shutdownTime = getShutdownTime();
        if (! (shutdownTime == null)) {
            Utils.logToConsole(((_GatewayOnly) ? "Gateway" : "TWS") +
                            " will be shut down at " +
                           (new SimpleDateFormat("yyyy/MM/dd HH:mm")).format(shutdownTime));
            _Timer.schedule(new TimerTask() {
                public void run() {
                    GuiExecutor.instance().execute(new StopTask(_GatewayOnly, null));
                    _Timer.cancel();
                }
            }, shutdownTime);
        }
    }

    private static void startTws() {
        String[] twsArgs = new String[1];
        twsArgs[0] = getTWSSettingsDirectory();
        jclient.LoginFrame.main(twsArgs);
    }

    private static void startTwsOrGateway() {
        if (_GatewayOnly) {
            startGateway();
        } else {
            startTws();
        }
    }

}

