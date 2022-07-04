// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2021 Richard L King (rlking@aultan.com)
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

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javax.swing.JDialog;
import java.util.regex.*;

public class TooManyFailedLoginAttemptsDialogHandler implements WindowHandler {
        @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handleWindow(Window window, int eventID) {
        // this dialog will contain a text area with a message like this:
        //      "Too many failed login attempts. Please wait 53 seconds before attempting to re-login again."
        // or like this:
        //      "Too many failed login attempts. Please wait 4 minutes & 47 seconds before attempting to re-login again."
        //
            String message = SwingUtils.findTextArea(window, "Too many failed login attempts").getText();
            Utils.logToConsole(message);
            Pattern p = Pattern.compile("(?:Too many failed login attempts. Please wait (?:(\\d\\d?) minute(?:s)? )?(?:& )?(?:(\\d\\d?) second(?:s)?)?)?");
            Matcher m = p.matcher(message);
            String minutes = "";
            String seconds = "";
            if (m.find()) {
                minutes = m.group(1);
                if (minutes == null || minutes.isEmpty()) minutes = "0";
                seconds = m.group(2);
                if (seconds == null || seconds.isEmpty()) seconds = "0";
            }
            Duration waitfor = Duration.parse("PT" + minutes + "M" + seconds + "S").plus(Duration.ofSeconds(3));

            if (Settings.settings().getBoolean("ReloginAfterSecondFactorAuthenticationTimeout", false)) {
                Utils.logToConsole("Will re-login at " + Utils.formatDate(LocalDateTime.now().plus(waitfor)) + 
                                    "; login number: " + 
                                    (LoginManager.loginManager().getLoginHandler().currentLoginAttemptNumber() + 1));

                MyScheduledExecutorService.getInstance().schedule(() -> {
                    GuiDeferredExecutor.instance().execute(
                        () -> {
                            LoginManager.loginManager().getLoginHandler().initiateLogin(LoginManager.loginManager().getLoginFrame());
                        }
                    );
                }, waitfor.getSeconds(), TimeUnit.SECONDS);

                if (!SwingUtils.clickButton(window, "OK")) {
                    Utils.logError("could not dismiss \"Too many failed login attempts\" dialog because we could not find one of the controls.");
                }
            }
    }

    @Override
    public boolean recogniseWindow(Window window) {
        if (! (window instanceof JDialog)) return false;

        return (SwingUtils.findTextArea(window, "Too many failed login attempts") != null);
    }

}
