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

import java.io.File;
import java.util.Arrays;

class StopTask
        implements Runnable {

    private static final SwitchLock _Running = new SwitchLock();

    private final CommandChannel mChannel;
    private final boolean mForceColdRestart;
    private final String mReason;

    StopTask(final CommandChannel channel, final boolean forceColdRestart, final String reason) {
        mChannel = channel;
        mForceColdRestart = forceColdRestart;
        mReason = reason;
    }

    @Override
    public void run() {
        if (! _Running.set()) {
            Utils.logToConsole("STOP already in progress");
            writeNack("STOP already in progress");
            mChannel.close();
            return;
        }

        try {
            writeInfo("Closing IBC");
            if (mForceColdRestart) createColdRestartFlagFile();
            stop(mReason);
        } catch (Exception ex) {
            writeNack(ex.getMessage());
            Utils.exitWithException(ErrorCodes.UNHANDLED_EXCEPTION, ex);
        }
    }
    
    private void createColdRestartFlagFile() {
        try {
        new File(System.getProperty("jtsConfigDir") + 
                 File.separator + 
                 "COLDRESTART" + 
                 System.getProperty("ibcsessionid"))
                .createNewFile();
        } catch (java.io.IOException e) {
            Utils.exitWithException(ErrorCodes.UNHANDLED_EXCEPTION, e);
        }
    }

    public final static boolean shutdownInProgress()
    {
        return _Running.query();
    }

    private void stop(String reason) {
        try {
            writeAck("Shutting down: " + reason);
            if (mChannel != null) mChannel.close();
            if (LoginManager.loginManager().getLoginState() != LoginManager.LoginState.LOGGED_IN) {
                CommandServer.commandServer().shutdown();
                Utils.logToConsole("Login has not completed: exiting immediately");
                System.exit(0);
            } else {
                String[] closeMenuPath = SessionManager.isGateway() ? new String[] {"File", "Close"} : new String[] {"File", "Exit"};
                Utils.logToConsole("Login has completed: exiting via " + Arrays.deepToString(closeMenuPath) + " menu");
                Utils.invokeMenuItem(MainWindowManager.mainWindowManager().getMainWindow(), closeMenuPath);
            }
            
        } catch (IllegalStateException e) {
            Utils.exitWithException(ErrorCodes.UNHANDLED_EXCEPTION, e);
        }
    }

    private void writeAck(String message) {if (mChannel != null) mChannel.writeAck(message);}
    private void writeInfo(String message) {if (mChannel != null) mChannel.writeInfo(message);}
    private void writeNack(String message) {if (mChannel != null) mChannel.writeNack(message);}

}
