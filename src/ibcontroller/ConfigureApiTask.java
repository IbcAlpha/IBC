// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 Richard L King (rlking@aultan.com)
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
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

class ConfigureApiTask implements Runnable{

    private static final SwitchLock _Running = new SwitchLock();

    private final CommandChannel mChannel;

    ConfigureApiTask(final CommandChannel channel) {
        mChannel = channel;
    }

    @Override
    public void run() {
        if (! _Running.set()) {
            mChannel.writeNack("API configuration already in progress");
            return;
        }

        try {
            waitForMainWindow();
            configureAPI();
        } catch (Exception ex) {
            mChannel.writeNack(ex.getMessage());
        } finally {
            _Running.clear();
        }
    }

    private void configureAPI() {
        (new GuiSynchronousExecutor()).execute(new Runnable(){
            public void run() {configureIt();}
        });
    }

    private void configureIt() {
        JFrame jf = TwsListener.getMainWindow();
        if (jf == null) {
            System.err.println("IBControllerServer: could not find main window to configure API");
            return;
        }

        Utils.logToConsole("Attempting to configure API");

        JMenuBar jmb = Utils.findMenuBar(jf);
        if (jmb == null) {
            System.err.println("IBControllerServer: Could not find JMenuBar inside main window.");
            return;
        }

        JMenuItem jmi = Utils.findMenuItem(jmb, "Configure");
        if (jmi == null) {
            System.err.println("IBControllerServer: Could not find Configure menu inside menubar.");
            return;
        }

        jmi = Utils.findMenuItem(jmi, "API");
        if (jmi == null) {
            System.err.println("IBControllerServer: Could not find API menu inside Configure menu.");
            return;
        }

        jmi = Utils.findMenuItem(jmi,"Enable ActiveX and Socket Clients");
        if (jmi == null || !(jmi instanceof JCheckBoxMenuItem)) {
            System.err.println("IBControllerServer: Could not find Enable ActiveX menu inside API menu.");
            return;
        }

        JCheckBoxMenuItem cmi = (JCheckBoxMenuItem) jmi;
        if (!cmi.isSelected()) {
            cmi.doClick();
            Utils.logToConsole("TWS has been configured to accept API connections.");
            mChannel.writeAck("TWS has been configured to accept API connections.");
        } else {
            Utils.logToConsole("TWS is already configured to accept API connections.");
            mChannel.writeAck("TWS is already configured to accept API connections.");
        }
    }

    private Boolean waitForMainWindow() {
       if (TwsListener.getMainWindow() != null) {
            return true;
        } else {
            // Need to wait until TWS is finished loading.
            // Not really sure when this is, but we will wait 5 seconds
            // after finding the main window.

            int count = 0;
            int maxcount = 300;
            mChannel.writeInfo("Waiting for TWS to load");
            while (TwsListener.getMainWindow() == null) {
                if (count++ >= maxcount) {
                    System.err.println("IBControllerServer: IB TWS did not load successfully.");
                    mChannel.writeNack("IBControllerServer: IB TWS did not load successfully.");
                    return false;
                }
                if (count % 10 == 0) mChannel.writeInfo("Waiting for TWS to load");
                Utils.pause(1000);
            }
            Utils.pause(5000);
            return true;
        }
    }
}
