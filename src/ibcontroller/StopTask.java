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

import javax.swing.JFrame;
import javax.swing.JMenuItem;

class StopTask
        implements Runnable {

    private static SwitchLock _Running = new SwitchLock();

    private final boolean mGateway;
    private final CommandChannel mChannel;

    public StopTask(boolean gateway, final CommandChannel channel) {
        this.mGateway = gateway;
        mChannel = channel;
    }

    public void run() {
        if (! _Running.set()) {
            writeNack("STOP already in progress");
            return;
        }

        try {
            writeInfo("Closing IBController");
            stop((mGateway) ? "Close" : "Exit");
        } catch (Exception ex) {
            writeNack(ex.getMessage());
        } finally {
            _Running.clear();
        }
    }

    private void stop(String stopCommand) {
        JFrame jf = TwsListener.getMainWindow();
        if (jf == null) {
            Utils.logToConsole("main window not yet found");
            writeNack("main window not yet found");
            return;
        }

        JMenuItem jmi = Utils.findMenuItem(jf, new String [] {"File", stopCommand});
        if (jmi == null) {
            System.err.println("IBController: Could not find File > " + stopCommand + " menu.");
            writeNack("File > " + stopCommand + " menu not found");
            return;
        }

        writeAck("");
        jmi.doClick();
    }

    private void writeAck(String message) {if (! (mChannel == null)) mChannel.writeAck(message);}
    private void writeInfo(String message) {if (! (mChannel == null)) mChannel.writeInfo(message);}
    private void writeNack(String message) {if (! (mChannel == null)) mChannel.writeNack(message);}

}
