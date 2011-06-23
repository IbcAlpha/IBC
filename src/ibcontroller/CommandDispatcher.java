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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class CommandDispatcher
        implements Runnable {

    private CommandChannel mChannel;
    private boolean mGateway;

    CommandDispatcher(CommandChannel channel, boolean gateway) {
        this.mChannel = channel;
        this.mGateway = gateway;
    }

    @Override public void run() {
        String cmd = mChannel.getCommand();
        while (cmd != null) {
            if (cmd.equalsIgnoreCase("EXIT")) {
                mChannel.writeAck("Goodbye");
                break;
            } else if (cmd.equalsIgnoreCase("STOP")) {
                handleStopCommand();
            } else if (cmd.equalsIgnoreCase("ENABLEAPI")) {
                handleEnableAPICommand();
            } else {
                handleInvalidCommand(cmd);
            }
            mChannel.writePrompt();
            cmd = mChannel.getCommand();
        }
        mChannel.close();
    }

    private void handleInvalidCommand(String cmd) {
        mChannel.writeNack("Command invalid");
        System.err.println("IBControllerServer: invalid command received: " + cmd);
    }

    private void handleEnableAPICommand() {
        if (mGateway) {
            mChannel.writeNack("ENABLEAPI is not valid for the IB Gateway");
            return;
        }

        Future<?> f = (Executors.newSingleThreadExecutor()).submit(new ConfigureApiTask(mChannel));

        // wait for the task to complete
        try{
            f.get();
        } catch (InterruptedException ie) {
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        }
   }

    private void handleStopCommand() {
        GuiExecutor.instance().execute(new StopTask(mGateway, mChannel));
    }

}
