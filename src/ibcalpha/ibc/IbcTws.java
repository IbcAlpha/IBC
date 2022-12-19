public class IbcTws {

    private IbcTws() { }

    public static void main(final String[] args) throws Exception {
        if (Thread.getDefaultUncaughtExceptionHandler() == null) {
            Thread.setDefaultUncaughtExceptionHandler(new ibcalpha.ibc.UncaughtExceptionHandler());
        }
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
         * It accepts only one argument which should be the path.
         * 
         * where:
         * 
         *      <iniFile>       ::= NULL | path-and-filename-of-.ini-file 
         * 
         */
        if (args.length != 1) {
            Utils.logError("Incorrect number of arguments passed. quitting...");
            Utils.logRawToConsole("Number of arguments = " +args.length);
            for (String arg : args) {
                Utils.logRawToConsole(arg);
            }
            Utils.exitWithError(ErrorCodes.ERROR_CODE_INCORRECT_NUMBER_OF_ARGUMENTS);
        }
    }

    public static void load() {
        try {
            printVersionInfo();

            printProperties();

            Settings.settings().logDiagnosticMessage();
            LoginManager.loginManager().logDiagnosticMessage();
            MainWindowManager.mainWindowManager().logDiagnosticMessage();
            TradingModeManager.tradingModeManager().logDiagnosticMessage();
            ConfigDialogManager.configDialogManager().logDiagnosticMessage();

            boolean isGateway = MainWindowManager.mainWindowManager().isGateway();

            startCommandServer(isGateway);

            startShutdownTimerIfRequired(isGateway);

            createToolkitListener();

            startSavingTwsSettingsAutomatically();

            startTwsOrGateway(isGateway);
        } catch (IllegalStateException e) {
            if (e.getMessage().equalsIgnoreCase("Shutdown in progress")) {
                // an exception with this message can occur if a STOP command is
                // processed by IBC while TWS/Gateway is still in early stages
                // of initialisation
                Utils.exitWithoutError();
            }
        }
    }

    public static void printVersionInfo() {
        Utils.logToConsole("version: " + IbcVersionInfo.IBC_VERSION);
    }

    private static void createToolkitListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new TwsListener(createWindowHandlers()), AWTEvent.WINDOW_EVENT_MASK);
    }

    private static List<WindowHandler> createWindowHandlers() {
        List<WindowHandler> windowHandlers = new ArrayList<>();

        windowHandlers.add(new AcceptIncomingConnectionDialogHandler());
        windowHandlers.add(new BlindTradingWarningDialogHandler());
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

        // this line must come before the one for SecurityCodeDialogHandler
        // because both contain an "Enter Read Only" button
        windowHandlers.add(SecondFactorAuthenticationDialogHandler.getInstance());
        windowHandlers.add(new SecurityCodeDialogHandler());
        
        windowHandlers.add(new ReloginDialogHandler());
        windowHandlers.add(new NonBrokerageAccountDialogHandler());
        windowHandlers.add(new ExitConfirmationDialogHandler());
        windowHandlers.add(new TradingLoginHandoffDialogHandler());
        windowHandlers.add(new LoginFailedDialogHandler());
        windowHandlers.add(new TooManyFailedLoginAttemptsDialogHandler());
        windowHandlers.add(new ShutdownProgressDialogHandler());
        windowHandlers.add(new BidAskLastSizeDisplayUpdateDialogHandler());
        windowHandlers.add(new LoginErrorDialogHandler());
        windowHandlers.add(new CryptoOrderConfirmationDialogHandler());
        windowHandlers.add(new AutoRestartConfirmationDialog());
                
        
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
                boolean dailyShutdown = false;
                try {
                    cal.setTime((new SimpleDateFormat("E HH:mm")).parse(shutdownTimeSetting));
                    dailyShutdown = false;
                } catch (ParseException e) {
                    try {
                        String today = (new SimpleDateFormat("E")).format(cal.getTime());
                        cal.setTime((new SimpleDateFormat("E HH:mm")).parse(today + " " + shutdownTimeSetting));
                        dailyShutdown = true;
                    } catch (ParseException x) {
                        throw x;
                    }
                }
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
                    if (dailyShutdown) {
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        cal.add(Calendar.DAY_OF_MONTH, 7);
                    }
                }
            } catch (ParseException e) {
                Utils.exitWithError(ErrorCodes.ERROR_CODE_INVALID_CLOSEDOWN_AT_SETTING, 
                                    "Invalid ClosedownAt setting: '" + shutdownTimeSetting + "'; format should be: <[day ]hh:mm>   eg 22:00 or Friday 22:00");
            }
            return cal.getTime();
        }
    }

    private static String getJtsIniFilePath() {
        return getTWSSettingsDirectory() + File.separatorChar + "jts.ini";
    }

    private static String getTWSSettingsDirectory() {
        String path = Settings.settings().getString("IbDir", System.getProperty("user.dir"));
        try {
            Files.createDirectories(Paths.get(path));
        } catch (FileAlreadyExistsException ex) {
            Utils.exitWithError(ErrorCodes.ERROR_CODE_CANT_CREATE_TWS_SETTINGS_DIRECTORY, 
                                "Failed to create TWS settings directory at: " + path + "; a file of that name already exists");
        } catch (IOException ex) {
            Utils.exitWithException(ErrorCodes.ERROR_CODE_CANT_CREATE_TWS_SETTINGS_DIRECTORY, ex);
        }
        return path;
    }

    private static void printProperties() {
        Properties p = System.getProperties();
        Enumeration<Object> i = p.keys();
        Utils.logRawToConsole("System Properties");
        Utils.logRawToConsole("------------------------------------------------------------");
        while (i.hasMoreElements()) {
            String props = (String) i.nextElement();
            String vals = (String) p.get(props);
            if (props.equals("sun.java.command")) {
                //hide credentials 
                String[] args = vals.split(" ");
                for (int j = 2; j < args.length - 1; j++) {
                    args[j] = "***";
                }
                vals = String.join(" ", args);
            }
            Utils.logRawToConsole(props + " = " + vals);
        }
        Utils.logRawToConsole("------------------------------------------------------------");
    }

    private static void startGateway() {
        String[] twsArgs = new String[1];
        twsArgs[0] = getTWSSettingsDirectory();
        try {
            Utils.logToConsole("Starting Gateway");
            LoginManager.loginManager().startSession();
            ibgateway.GWClient.main(twsArgs);
        } catch (Throwable t) {
            Utils.logError("Can't find the Gateway entry point: ibgateway.GWClient.main. Gateway is not correctly installed.");
            t.printStackTrace(Utils.getErrStream());
            Utils.exitWithError(ErrorCodes.ERROR_CODE_CANT_FIND_ENTRYPOINT);
        }
    }

    private static void startCommandServer(boolean isGateway) {
        MyCachedThreadPool.getInstance().execute(new CommandServer(isGateway));
    }

    private static void startShutdownTimerIfRequired(boolean isGateway) {
        Date shutdownTime = getShutdownTime();
        if (! (shutdownTime == null)) {
            long delay = shutdownTime.getTime() - System.currentTimeMillis();
            Utils.logToConsole((isGateway ? "Gateway" : "TWS") +
                            " will be shut down at " +
                           (new SimpleDateFormat("yyyy/MM/dd HH:mm")).format(shutdownTime));
            MyScheduledExecutorService.getInstance().schedule(() -> {
                MyCachedThreadPool.getInstance().execute(new StopTask(null, isGateway));
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private static void startTws() {
        if (Settings.settings().getBoolean("ShowAllTrades", false)) {
            Utils.showTradesLogWindow();
        }
        String[] twsArgs = new String[1];
        twsArgs[0] = getTWSSettingsDirectory();
        try {
            Utils.logToConsole("Starting TWS");
            LoginManager.loginManager().startSession();
            jclient.LoginFrame.main(twsArgs);
        } catch (Throwable t) {
            Utils.logError("Can't find the TWS entry point: jclient.LoginFrame.main; TWS is not correctly installed.");
            t.printStackTrace(Utils.getErrStream());
            Utils.exitWithError(ErrorCodes.ERROR_CODE_CANT_FIND_ENTRYPOINT);
        }
    }

    private static void startTwsOrGateway(boolean isGateway) {
        Utils.logToConsole("TWS Settings directory is: " + getTWSSettingsDirectory());
        JtsIniManager.initialise(getJtsIniFilePath());
        if (isGateway) {
            startGateway();
        } else {
            startTws();
        }

        int portNumber = Settings.settings().getInt("OverrideTwsApiPort", 0);
        if (portNumber != 0) (new ConfigurationTask(new ConfigureTwsApiPortTask(portNumber))).executeAsync();

        if (!Settings.settings().getString("ReadOnlyApi", "").equals("")) {
            (new ConfigurationTask(new ConfigureReadOnlyApiTask(Settings.settings().getBoolean("ReadOnlyApi",true)))).executeAsync();
        }

        String sendMarketDataInLots = Settings.settings().getString("SendMarketDataInLotsForUSstocks", "");
        if (!sendMarketDataInLots.equals("")) {
            (new ConfigurationTask(new ConfigureSendMarketDataInLotsForUSstocksTask(Settings.settings().getBoolean("SendMarketDataInLotsForUSstocks", true)))).executeAsync();
        }
        
        String autoLogoffTime = Settings.settings().getString("AutoLogoffTime", "");
        String autoRestartTime = Settings.settings().getString("AutoRestartTime", "");
        if (!autoRestartTime.equals("")) {
            (new ConfigurationTask(new ConfigureAutoLogoffOrRestartTimeTask("Auto restart", autoRestartTime))).executeAsync();
            if (!autoLogoffTime.equals("")) {
                Utils.logToConsole("AutoLogoffTime is ignored because AutoRestartTime is also set");
            }
        } else if (!autoLogoffTime.equals("")) {
            (new ConfigurationTask(new ConfigureAutoLogoffOrRestartTimeTask("Auto logoff", autoLogoffTime))).executeAsync();
        }

        Utils.sendConsoleOutputToTwsLog(!Settings.settings().getBoolean("LogToConsole", false));
    }

    private static void startSavingTwsSettingsAutomatically() {
        TwsSettingsSaver.getInstance().initialise();
    }

}

