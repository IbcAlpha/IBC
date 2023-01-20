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

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

class CommandDispatcher
        implements Runnable {

    private final CommandChannel mChannel;

    CommandDispatcher(CommandChannel channel) {
        mChannel = channel;
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
            } else if (cmd.equalsIgnoreCase("RECONNECTDATA")) {
            	handleReconnectDataCommand();
            } else if (cmd.equalsIgnoreCase("RECONNECTACCOUNT")) {
            	handleReconnectAccountCommand();
            } else if (cmd.equalsIgnoreCase("RESTART")) {
            	handleRestartCommand();
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
        Utils.logError("CommandServer: invalid command received: " + cmd);
    }

    private void handleEnableAPICommand() {
        if (SessionManager.isGateway()) {
            mChannel.writeNack("ENABLEAPI is not valid for the IB Gateway");
            return;
        }

        // run on the current thread
        (new ConfigurationTask(new EnableApiTask(mChannel))).execute();
   }

    private void handleReconnectDataCommand() {
        JFrame jf = MainWindowManager.mainWindowManager().getMainWindow(1, TimeUnit.MILLISECONDS);

        int modifiers = KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK;
        KeyEvent pressed=new KeyEvent(jf,  KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, KeyEvent.VK_F, KeyEvent.CHAR_UNDEFINED);
        KeyEvent typed=new KeyEvent(jf, KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers, KeyEvent.VK_UNDEFINED, 'F' );
        KeyEvent released=new KeyEvent(jf, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, KeyEvent.VK_F,  KeyEvent.CHAR_UNDEFINED );
        jf.dispatchEvent(pressed);
        jf.dispatchEvent(typed);
        jf.dispatchEvent(released);
  
        mChannel.writeAck("");
   }

    private void handleReconnectAccountCommand() {
        JFrame jf = MainWindowManager.mainWindowManager().getMainWindow();

        int modifiers = KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK;
        KeyEvent pressed=new KeyEvent(jf,  KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, KeyEvent.VK_R, KeyEvent.CHAR_UNDEFINED);
        KeyEvent typed=new KeyEvent(jf, KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers, KeyEvent.VK_UNDEFINED, 'R' );
        KeyEvent released=new KeyEvent(jf, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, KeyEvent.VK_R,  KeyEvent.CHAR_UNDEFINED );
        jf.dispatchEvent(pressed);
        jf.dispatchEvent(typed);
        jf.dispatchEvent(released);

        mChannel.writeAck("");
    }

    private void handleStopCommand() {
        (new StopTask(mChannel)).run();     // run on the current thread
    }
    
    private void handleRestartCommand() {
        if (MainWindowManager.mainWindowManager().isGateway()) {
            LocalTime now = LocalTime.now();
            LocalTime restartTime;
            if (now.getSecond() < 20) {
                restartTime = now.withMinute(now.getMinute() + 1);
            } else {
                restartTime = now.withMinute(now.getMinute() + 2);
            }
            Utils.logToConsole("Setting auto-restart time to " + restartTime.format(DateTimeFormatter.ofPattern("KK:mm a")));
            (new ConfigurationTask(new ConfigureAutoLogoffOrRestartTimeTask(
                                            "Auto restart", 
                                            restartTime)
                                    )
            ).executeAsync();
        } else {
            Utils.invokeMenuItem(MainWindowManager.mainWindowManager().getMainWindow(), new String[] {"File", "Restart..."});
        }
        mChannel.writeAck("Restart in progress");
        mChannel.close();
    }

}
