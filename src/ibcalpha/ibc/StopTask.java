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

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

class StopTask
        implements Runnable {

    private static final SwitchLock _Running = new SwitchLock();

    private final CommandChannel mChannel;

    public StopTask(final CommandChannel channel) {
        mChannel = channel;
    }

    @Override
    public void run() {
        if (! _Running.set()) {
            writeNack("STOP already in progress");
            return;
        }

        try {
            MyCachedThreadPool.getInstance().shutdownNow();
            MyScheduledExecutorService.getInstance().shutdownNow();

            writeInfo("Closing IBC");
            stop();
        } catch (Exception ex) {
            writeNack(ex.getMessage());
        }
    }

    public final static boolean shutdownInProgress()
    {
        return _Running.query();
    }

    private void stop() {
        JFrame jf = MainWindowManager.mainWindowManager().getMainWindow();

        WindowEvent wev = new WindowEvent(jf, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);

        writeAck("Shutting down");
    }

    private void writeAck(String message) {if (! (mChannel == null)) mChannel.writeAck(message);}
    private void writeInfo(String message) {if (! (mChannel == null)) mChannel.writeInfo(message);}
    private void writeNack(String message) {if (! (mChannel == null)) mChannel.writeNack(message);}

}
