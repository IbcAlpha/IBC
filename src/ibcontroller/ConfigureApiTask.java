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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

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

    private void completeConfigureItViaEditMenu() {
        Utils.logToConsole("Completing ENABLEAPI configuration");
        JDialog configDialog = TwsListener.getConfigDialog();
        if (configDialog == null) {
            System.err.println("IBControllerServer: could not find the Global Configuration dialog");
            mChannel.writeNack("Global Configuration dialog not found");
            return;
        }
        JTree configTree = Utils.findTree(configDialog);
        if (configTree == null) {
            System.err.println("IBControllerServer: could not find the config tree in the Global Configuration dialog");
            mChannel.writeNack("config tree not found");
            return;
        }
        TreePath tp = new TreePath(configTree.getModel().getRoot());
        Object node = Utils.findChildNode(configTree.getModel(), configTree.getModel().getRoot(), "API");
        tp = tp.pathByAddingChild(node);

        // later versions of TWS have a Settings node below the API node
        node = Utils.findChildNode(configTree.getModel(), node, "Settings");
        if (!(node == null)) tp = tp.pathByAddingChild(node);

        Utils.logToConsole("getExpandsSelectedPaths = " + configTree.getExpandsSelectedPaths());
        Utils.logToConsole("Selection path = " + tp.toString());
        configTree.setSelectionPath(tp);

        JCheckBox cb = Utils.findCheckBox(configDialog, "Enable ActiveX and Socket Clients");
        if (cb == null) {
            System.err.println("IBControllerServer: Could not find Enable ActiveX checkbox inside API menu.");
            mChannel.writeNack("Enable ActiveX checkbox not found");
            return;
        }
        if (!cb.isSelected()) {
            cb.doClick();
            Utils.clickButton(configDialog, "OK");
            Utils.logToConsole("TWS has been configured to accept API connections.");
            mChannel.writeAck("configured");
        } else {
            Utils.logToConsole("TWS is already configured to accept API connections.");
            mChannel.writeAck("already configured");
        }

        configDialog.setVisible(false);
    }

    private void configureAPI() {
        GuiSynchronousExecutor.instance().execute(new Runnable(){
            public void run() {configureIt();}
        });
    }

    private void configureIt() {
        JFrame jf = TwsListener.getMainWindow();
        if (jf == null) {
            System.err.println("IBControllerServer: could not find main window to configure API");
             mChannel.writeNack("main window not found");
            return;
        }

        Utils.logToConsole("Attempting to configure API");

        JMenuItem jmi = Utils.findMenuItem(jf, new String[] {"Configure", "API", "Enable ActiveX and Socket Clients"});
        if (jmi != null) {
            configureItViaConfigureMenu((JCheckBoxMenuItem)jmi);
        } else {
            jmi = Utils.findMenuItem(jf, new String[] {"Edit", "Global Configuration..."});
            if (jmi != null) {
                configureItViaEditMenu(jmi);
            } else {
                System.err.println("IBControllerServer: could not find Configure > API > Enable ActiveX or Edit > Global Configuration menus");
                mChannel.writeNack("Configure > API > Enable ActiveX or Edit > Global Configuration menus not found");
            }
        }
   }

    private boolean configureItViaConfigureMenu(JCheckBoxMenuItem cmi) {
        if (!cmi.isSelected()) {
            cmi.doClick();
            Utils.logToConsole("TWS has been configured to accept API connections.");
            mChannel.writeAck("configured");
        } else {
            Utils.logToConsole("TWS is already configured to accept API connections.");
            mChannel.writeAck("already configured");
        }
        return true;
    }

    private void configureItViaEditMenu(JMenuItem jmi) {
        Utils.logToConsole("Click Edit > Global Configuration...");
        jmi.doClick();

        GuiDeferredExecutor.instance().execute(new Runnable(){
            public void run() {completeConfigureItViaEditMenu();}
        });

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
