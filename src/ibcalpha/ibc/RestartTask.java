// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2023 Richard L King (rlking@aultan.com)
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import static java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import static java.lang.Thread.sleep;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;

class RestartTask
        implements Runnable {

    private static final SwitchLock _Running = new SwitchLock();

    private final CommandChannel mChannel;

    RestartTask(final CommandChannel channel) {
        mChannel = channel;
    }

    @Override
    public void run() {
        if (! _Running.set()) {
            Utils.logToConsole("RESTART already in progress");
            writeNack("RESTART already in progress");
            mChannel.close();
            return;
        }

        try {
            writeInfo("Restarting TWS");
            restart();
        } catch (Exception ex) {
            writeNack(ex.getMessage());
            Utils.exitWithException(ErrorCodes.UNHANDLED_EXCEPTION, ex);
        }
    }
    
    void restart() {
        if (Utils.invokeMenuItem(MainWindowManager.mainWindowManager().getMainWindow(), new String[] {"File", "Restart..."})) {
            writeAck("Restart in progress");
            mChannel.close();
            return;
        }
        
        while (LocalTime.now().getSecond() >= 58) {
            try {sleep(1);} catch (InterruptedException e) {}
        }

        LocalTime now = LocalTime.now();

        int newHour = now.getHour();
        int newMinute = now.getMinute() + 1;
        if (newMinute > 59) {
            newMinute = newMinute - 60;
            newHour = newHour + 1;
        }
        if (newHour > 23) {
            newHour = newHour - 24;
        }
        LocalTime restartTime = now.withHour(newHour).withMinute(newMinute).withSecond(0);

        Utils.logToConsole("Setting auto-restart time to " + restartTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        (new ConfigurationTask(new ConfigureAutoLogoffOrRestartTimeTask(
                                        "Auto restart", 
                                        restartTime)
                                )
        ).executeAsync();

        writeAck("Restarting at "  + restartTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        mChannel.close();

        try {
            if (!GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isWindowTranslucencySupported(TRANSLUCENT)) return;
        } catch (IllegalComponentStateException e) {
            return;
        }

        JFrame window = MainWindowManager.mainWindowManager().getMainWindow();
        window.setOpacity(0.80f);
        
        RestartTask.Countdown countdown = new RestartTask.Countdown(restartTime.isBefore(LocalTime.now()) 
                                            ? restartTime.atDate(LocalDate.now().plusDays(1)) 
                                            : restartTime.atDate(LocalDate.now()));
        window.setGlassPane(countdown);
        countdown.setVisible(true);
    }    

    private void writeAck(String message) {if (mChannel != null) mChannel.writeAck(message);}
    private void writeInfo(String message) {if (mChannel != null) mChannel.writeInfo(message);}
    private void writeNack(String message) {if (mChannel != null) mChannel.writeNack(message);}

    private class Countdown extends JPanel {
        private  static final long serialVersionUID = 1L;
        
        private final Font font = new Font("Arial", Font.BOLD, 36);
        private final LocalDateTime countdownTo;
        private Duration secsRemaining;
        
        public Countdown(final LocalDateTime countdownTo) {
            this.setSize(250, 250);
            this.setOpaque(false);
            this.countdownTo = countdownTo;
            MyScheduledExecutorService.getInstance().scheduleAtFixedRate(  () -> {
                                                secsRemaining = Duration.between(LocalDateTime.now(), this.countdownTo);
                                                this.repaint();
                                            }, 
                                            0, 
                                            1, 
                                            TimeUnit.SECONDS);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);       
            
            g.setColor(Color.WHITE);
            g.setFont(font);
            if (secsRemaining.isNegative()) {
                g.drawString("Restart in progress", 50, 300);
            } else {
                g.drawString("Restarting in " + secsRemaining.getSeconds() + " seconds", 50, 300);
            }
        }  
    }
}
