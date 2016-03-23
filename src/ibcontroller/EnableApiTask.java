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
import java.util.concurrent.FutureTask;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

class EnableApiTask implements Runnable{

    private static final SwitchLock _Running = new SwitchLock();

    private final CommandChannel mChannel;

    EnableApiTask(final CommandChannel channel) {
        mChannel = channel;
    }

    @Override public void run() {
        if (! _Running.set()) {
            mChannel.writeNack("API configuration already in progress");
            return;
        }

        try {
            final JDialog configDialog = ConfigDialogManager.configDialogManager().getConfigDialog();    // blocks the thread until the config dialog is available
            
            FutureTask<Integer> t = new FutureTask<>(new Runnable(){
                @Override public void run() {configureAPI(configDialog);}
            }, 0);
            GuiExecutor.instance().execute(t);
            t.get();
        } catch (InterruptedException | ExecutionException e) {
            Utils.logError("IBControllerServer: " + e.getMessage());
            mChannel.writeNack(e.getMessage());
        } finally {
            _Running.clear();
        }
    }

    private void configureAPI(JDialog configDialog) {
        try {
            Utils.logToConsole("Doing ENABLEAPI configuration");
            
            if (!Utils.selectConfigSection(configDialog, new String[] {"API","Settings"}))
                // older versions of TWS don't have the Settings node below the API node
                Utils.selectConfigSection(configDialog, new String[] {"API"});

            JCheckBox cb = SwingUtils.findCheckBox(configDialog, "Enable ActiveX and Socket Clients");
            if (cb == null) throw new IBControllerException("could not find Enable ActiveX checkbox");

            if (!cb.isSelected()) {
                cb.doClick();
                SwingUtils.clickButton(configDialog, "OK");
                Utils.logToConsole("TWS has been configured to accept API connections");
                mChannel.writeAck("configured");
            } else {
                Utils.logToConsole("TWS is already configured to accept API connections");
                mChannel.writeAck("already configured");
            }

            configDialog.setVisible(false);
        } catch (IBControllerException e) {
            Utils.logError("IBControllerServer: " + e.getMessage());
            mChannel.writeNack(e.getMessage());
        }
    }

}
