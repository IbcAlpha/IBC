::   If your TWS user id and password are not included in the IBController 
::   configuration file, set them here (do not encrypt the password):

set TWSUSERID=
set TWSPASSWORD=


::   The folder containing the IBController files:

set IBCDIR=C:\IBController


::   The location and filename of the IBController configuration file. This file should
::   be in a folder in your personal filestore, so that other users of your computer can't
::   access it. This folder and its contents should also be encrypted so that even users
::   with administrator privileges can't see the contents. Note that you can use the HOMEPATH
::   environment variable to address the root of your personal filestore (HOMEPATH is set
::   automatically by Windows):

set IBCINI="%HOMEPATH%\Documents\IBController\IBController.ini"


::   The folder where TWS is installed:

set TWSDIR=C:\Jts\


::   The classpath for TWS. The value below is correct for version
::   942 (you can verify which version of TWS you are using by going
::   to the Help | About Trader Workstation menu in TWS).
::
::   For other versions of TWS, the information needed may change.
::   You can find the required information in the shortcut created when you 
::   installed TWS. 
::
::   To locate this in Windows 7, right click on the start menu entry for 
::   TWS and click Properties. In Windows 8, locate the tile for TWS in 
::   the start screen, right click it, and select 'Open file location' on the menu 
::   bar at the bottom of the screen. 
::
::   In the field labelled 'Target', select everything after "-cp " up to the 
::   first subsequent space character, then press Ctrl-C to copy it to the 
::   clipboard, then paste it into the following command, replacing everything 
::   after the "=" character:

set TWSCP=jts.jar;total.2012.jar


::   Other Java VM options for TWS. You can find this information in the 
::   shortcut created when you installed TWS. (Note that in the shortcut, 
::   jclient/LoginFrame is NOT part of the Java options, nor is anything 
::   that comes after it, so don't include that here):

set JAVAOPTS=-Dsun.java2d.noddraw=true -Dswing.boldMetal=false -Dsun.locale.formatasdefault=true -Xmx1024M -XX:MaxPermSize=256M


pushd %TWSDIR%
:: prevent other Java tools interfering with IBController

setlocal
set JAVA_TOOL_OPTIONS
=
java.exe -cp  %TWSCP%;%IBCDIR%\IBController.jar %JAVAOPTS% ibcontroller.IBController %IBCINI% %TWSUSERID% %TWSPASSWORD%
popd

