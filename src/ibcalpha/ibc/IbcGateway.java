package ibcalpha.ibc;

import static ibcalpha.ibc.IbcTws.checkArguments;
import static ibcalpha.ibc.IbcTws.setupDefaultEnvironment;

public class IbcGateway {
    public static void main(String[] args) throws Exception {
        if (Thread.getDefaultUncaughtExceptionHandler() == null) {
            Thread.setDefaultUncaughtExceptionHandler(new ibcalpha.ibc.UncaughtExceptionHandler());
        }
        checkArguments(args);
        setupDefaultEnvironment(args, true);
        IbcTws.load();
    }

    public static void printVersionInfo() {
        IbcTws.printVersionInfo();
    }

}
