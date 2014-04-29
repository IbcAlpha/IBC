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

import javax.swing.*;
import javax.swing.tree.TreePath;

class ConfigureSocketPort implements Runnable{

    @Override
    public void run() {
        while (true) {
            GuiSynchronousExecutor.instance().execute(new Runnable() {
                public void run() {
                    configureIt();
                }
            });

            if (TwsListener.getConfigDialog() != null) {
                GuiDeferredExecutor.instance().execute(new Runnable(){
                    public void run() {completeConfigureItViaEditMenu();}
                });
                return;
            }
        }
    }

    private void configureIt() {
        if (TwsListener.getMainWindow() == null) {
            return;
        }
        JFrame jf = TwsListener.getMainWindow();
        if (jf == null) {
            Utils.err.println("Could not find main window to configure API");
            return;
        }

        JMenuItem jmi = Utils.findMenuItem(jf, new String[] {"Edit", "Global Configuration..."}); // TWS
        if (jmi != null) {
            configureItViaMenu(jmi);
            return;
        }

        jmi = Utils.findMenuItem(jf, new String[] {"Configure", "Settings"});  // IB Gateway
        if (jmi != null) {
            configureItViaMenu(jmi);
            return;
        }

        Utils.err.println("Could not find Edit > Global Configuration or Configure > Settings menus");
    }

    private void configureItViaMenu(final JMenuItem jmi) {
        // While TWS makes the menu item available, clicking it yields a console
        // error "Should not be called ahead of allowed features". We'll just
        // ignore it, given it's internal to TWS. We'll leave the isEnabled()
        // check in case they fix it in a later version of TWS.
        if (jmi.isEnabled()) {
            jmi.doClick();
        }
    }

    private void completeConfigureItViaEditMenu() {
        int portNumber = Settings.getInt("ForceSocketPort", 0);
        if (portNumber == 0) {
            return;
        }
        Utils.logToConsole("Performing port configuration");
        JDialog configDialog = null;
        while (configDialog == null) {
            configDialog = TwsListener.getConfigDialog();
        }

        if (configDialog == null) {
            Utils.err.println("Could not find the Global Configuration dialog");
            return;
        }
        JTree configTree = Utils.findTree(configDialog);
        if (configTree == null) {
            Utils.err.println("Could not find the config tree in the Global Configuration dialog");
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

        if (! Utils.setTextField(configDialog, 0, new Integer(portNumber).toString())) {
            Utils.err.println("Could not set socket port field.");
            return;
        }

        JTextField tf = Utils.findTextField(configDialog, 0);
        if (tf == null) {
            Utils.err.println("Could not find socket port field.");
            return;
        }

        tf.requestFocus();

        Utils.clickButton(configDialog, "OK");
        Utils.logToConsole("TWS has been configured to the new socket port number.");

        configDialog.setVisible(false);

        if (Settings.getBoolean("MinimizeMainWindow", false)) {
            TwsListener.getMainWindow().setExtendedState(java.awt.Frame.ICONIFIED);
        }
    }
}
