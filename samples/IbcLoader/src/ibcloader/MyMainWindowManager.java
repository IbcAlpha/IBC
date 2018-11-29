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

package ibcloader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JFrame;
import static java.awt.GraphicsDevice.WindowTranslucency.*;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;

public class MyMainWindowManager extends ibcalpha.ibc.DefaultMainWindowManager {

    public MyMainWindowManager(boolean isGateway) {
        super(isGateway);
    }
    
    @Override
    public void setMainWindow(JFrame window) {
        super.setMainWindow(window);
        
        try {
            if (!GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isWindowTranslucencySupported(TRANSLUCENT)) return;
        } catch (IllegalComponentStateException e) {
            return;
        }

        window.setOpacity(0.80f);
        
        SimpleClock clock = new SimpleClock();
        window.setGlassPane(clock);
        clock.setVisible(true);
    }
    
    private class SimpleClock extends JPanel {
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        private final Font font = new Font("Arial", Font.BOLD, 36);
        private LocalDateTime currentTime;
        
        public SimpleClock() {
            this.setSize(250, 250);
            this.setOpaque(false);
            currentTime = LocalDateTime.now();
            final int offset = (1000000000 - currentTime.getNano()) / 1000000;
            scheduler.scheduleAtFixedRate(  () -> {
                                                currentTime = LocalDateTime.now();
                                                this.repaint();
                                            }, 
                                            offset, 
                                            1000, 
                                            TimeUnit.MILLISECONDS);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);       
            
            g.setColor(Color.WHITE);
            g.setFont(font);
            g.drawString(currentTime.format(formatter), 50, 200);
        }  
        
    }
}
