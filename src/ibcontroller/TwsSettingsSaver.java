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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

class TwsSettingsSaver {
    private static final TwsSettingsSaver instance = new TwsSettingsSaver();
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm");

    
    private TwsSettingsSaver() {};
    
    static TwsSettingsSaver getInstance() {return instance;}
    
    public void initialise() {
        // setting format: SaveTwsSettingsAt=hh:mm [hh:mm]...
        //             or: SaveTwsSettingsAt=Every n [{mins | hours}] [hh:mm [hh:mm]]
        String timesSetting = Settings.settings().getString("SaveTwsSettingsAt", "");
        if (timesSetting.length() == 0) return;
        
        String[] times = timesSetting.split("[ ]+");
        
        List<Date> saveTimes = null;
        
        try {
            if (!times[0].equalsIgnoreCase("Every")){
                saveTimes = convertSuppliedTimes(times);
            } else {
                saveTimes = generateSaveTimes(times);
            }

            for (Date c : saveTimes) {
                scheduleSave(c);
            }
            
        } catch (IBControllerException e) {
            Utils.logError("Invalid setting SaveTwsSettingsAt=" + timesSetting + ": " + e.getMessage() + "\nTWS Settings will not be saved automatically");
        }
        
    }
    
    private static Calendar adjustCalendar(Calendar calendar) {
        Calendar cal = (Calendar)calendar.clone();
        if (!cal.getTime().after(new Date())) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal;
    }
    
    private static List<Date> convertSuppliedTimes(String[] times) throws IBControllerException {
        List<Date> saveTimes = new ArrayList<Date>();
        for (String time : times) {
            saveTimes.add(adjustCalendar(getCalendarForTime(time)).getTime());
        }
        return saveTimes;
    }
    
    private static List<Date> generateSaveTimes(String[] times) throws IBControllerException {
        int interval = 0;
        try {
            interval = Integer.parseInt(times[1]);
        } catch (NumberFormatException e) {
            throw new IBControllerException("interval is '" + times[1] + "' but should be an integer");
        }
        
        int nextIndex = 2;
        
        if (times.length > 2) {
            if (times[2].equalsIgnoreCase("mins")) {
                nextIndex = 3;
            } else if (times[2].equalsIgnoreCase("hours")) {
                nextIndex = 3;
                interval = 60 * interval;
            }
        }
        if (interval < 1 || interval > 1439) {
            throw new IBControllerException("the saving interval must be between 1 and 1439 minutes");
        }
        
        String startTime = "00:00";
        String endTime = "24:00";
        if (times.length > nextIndex) {
            startTime = times[nextIndex];
            if (times.length > nextIndex + 1) {
                endTime = times[nextIndex + 1];
            }
        }
        
        Calendar startCal = getCalendarForTime(startTime);
        Utils.logToConsole("startCal.getTime() = " + (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(startCal.getTime()));
        Calendar endCal = getCalendarForTime(endTime);
        if (!startCal.before(endCal)) {
            endCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        Utils.logToConsole("endCal.getTime() = " + (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(endCal.getTime()));
      
        List<Date> saveTimes = new ArrayList<Date>();
        while (startCal.before(endCal)) {
            saveTimes.add(adjustCalendar(startCal).getTime());
            startCal.add(Calendar.MINUTE, interval);
        }
        saveTimes.add(adjustCalendar(endCal).getTime());
        
        return saveTimes;
    }
    
    private static Calendar getCalendarForTime(String time) throws IBControllerException {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(time));
        } catch (ParseException e) {
            throw new IBControllerException("value '" + time + "' should be in hh:mm format");
        }
        int saveHour = cal.get(Calendar.HOUR_OF_DAY);
        int saveMinute = cal.get(Calendar.MINUTE);
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, saveHour);
        cal.set(Calendar.MINUTE, saveMinute);
        cal.set(Calendar.SECOND, 0);
        return cal;
    }
    
    private static void scheduleSave(Date saveTime) {
        Utils.logToConsole("Tws settings will be saved at " + dateFormat.format(saveTime));

        MyScheduledExecutorService.getInstance().scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        Utils.logToConsole("Saving Tws settings");
                        Utils.invokeMenuItem(MainWindowManager.mainWindowManager().getMainWindow(), new String[] {"File", "Save Settings"});
                    }
                }, saveTime.getTime() - System.currentTimeMillis(), 86400000, TimeUnit.MILLISECONDS);
    }
    
}

