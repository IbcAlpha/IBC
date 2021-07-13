@echo off
setlocal enableextensions enabledelayedexpansion


::=============================================================================+
::                                                                             +
::   Stops Interactive Brokers' IB Gateway or Trader Workstation (TWS).        + 
::                                                                             +
::=============================================================================+


set TWS_MAJOR_VRSN=978
set CONFIG=%USERPROFILE%\Documents\IBC\config.ini
set TRADING_MODE=
set TWOFA_TIMEOUT_ACTION=exit
set IBC_PATH=%~dp0.
set TWS_PATH=%SYSTEMDRIVE%\Jts
set TWS_SETTINGS_PATH=
set LOG_PATH=%IBC_PATH%\Logs
set TWSUSERID=
set TWSPASSWORD=
set FIXUSERID=
set FIXPASSWORD=
set JAVA_PATH=
set HIDE=
set PRESS_ANY_KEY_ON_ERROR=1


::  PLEASE DON'T CHANGE ANYTHING BELOW THIS LINE !!
::==============================================================================

::   Notes:
::

::   TWS_MAJOR_VRSN
::
::     Specifies the major version number of Gateway to be run. If you are 
::     unsure of which version number to use, run the Gateway manually from the 
::     icon on the desktop, then click Help > About IB Gateway. In the 
::     displayed information you'll see a line similar to this:
::
::       Build 954.2a, Oct 30, 2015 4:07:54 PM
::
::     Here the major version number is 954. Do not include the rest of the 
::     version number in this setting.


::   CONFIG
::
::     This is the location and filename of the IBC configuration file.
::     This file should be in a folder in your personal filestore, so that
::     other users of your computer can't access it. This folder and its 
::     contents should also be encrypted so that even users with administrator 
::     privileges can't see the contents. Note that you can use the USERPROFILE
::     environment variable to address the root of your personal filestore
::     (they are set automatically by Windows).


::   IBC_PATH
::
::     The folder containing the IBC files. 


::   TWS_PATH
::
::     The folder where TWS is installed. The TWS installer always installs to 
::     C:\Jts. Note that even if you have installed from a Gateway download
::     rather than a TWS download, you should still use this default setting.
::     It is possible to move the TWS installation to a different folder, but
::     there are virtually no good reasons for doing so.


::   TWS_SETTINGS_PATH
::
::     The folder where TWS is to store its settings.  This setting is ignored
::     if the IbDir setting in the configuration file is specified. If no value 
::     is specified in either place, the settings are stored in the TWS_PATH 
::     folder.


::   LOG_PATH
::
::     Specifies the folder where diagnostic information is to be logged while 
::     this command file is running. This information is very valuable when 
::     troubleshooting problems, so it is advisable to always have this set to
::     a valid location, especially when setting up IBC. You must
::     have write access to the specified folder.
::
::     Once everything runs properly, you can prevent further logging by 
::     removing the value as show below (but this is not recommended): 
::
::     set LOG_PATH=


::   JAVA_PATH
::
::     IB's installer for TWS/Gateway includes a hidden version of Java which 
::     IB have used to develop and test that particular version. This means that
::     it is not necessary to separately install Java. If there is a separate
::     Java installation, that does not matter: it won't be used by IBC 
::     or TWS/Gateway unless you set the path to it here. You should not do this 
::     without a very good reason.


::   HIDE
::
::     If set to YES or TRUE, the diagnostic window that contains information 
::     about the running TWS, and where to find the log file, will be minimized 
::     to the taskbar. If not set, or set to any other value, the window will be 
::     displayed. Values are not case-sensitive so for example yEs and yes will 
::     be interpeted as YES. (Note that when the /INLINE argument is supplied,
::     this setting has no effect.)


::   PRESS_ANY_KEY_ON_ERROR
::
::     If set to 1, the diagnostic window will remain open and prompt user for
::     pressing any key in order to let them read error message. This behaviour
::     however is not desirable when the script runs in non-interactive mode
::     (e.g. when it is started by task scheduler). In such case please
::     set it to 0 or make it empty.


::   End of Notes:
::==============================================================================

set APP=STOP
set TITLE=IBC (%APP% %TWS_MAJOR_VRSN%)
if /I "%HIDE%" == "YES" (
    set MIN=/Min
) else if /I "%HIDE%" == "TRUE" (
    set MIN=/Min
) else (
    set MIN=
)

id /

if /I "%~1" == "/INLINE" (
    set INLINE=1
    "%IBC_PATH%\scripts\DisplayBannerAndLaunch.bat" %~2
) else (
    set INLINE=0
    start "%TITLE%" %MIN% "%IBC_PATH%\scripts\DisplayBannerAndLaunch.bat" %~1
)
exit /B
