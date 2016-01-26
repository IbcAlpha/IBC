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


::   The folder where the TWS program files are installed. The main program file is jts.jar,
::   so set this to the folder that contains that file. By default, for TWS versions before
::   TWS 952, this folder will be C:\Jts\. For later versions, the folder will be 
::   C:\Jts\nnn\jars, where nnn is the TWS major version number.

set TWSDIR=C:\Jts\952\jars


::   The classpath for TWS. The value below is correct for versions
::   952 and 954 (you can verify which version of TWS you are using by going
::   to the Help | About Trader Workstation menu in TWS).
::
::   For other versions of TWS, the information needed may be different.
::   First, look in the folder where TWS is installed (typically C:\Jts).
::   If this folder contains a file called StartTWS.bat, open the file with
::   a text editor and look at the line beginning with START. Select 
::   everything after "-cp " up to the first subsequent space character,
::   then press Ctrl-C to copy it to the clipboard, then paste it into the
::   "set TWSCP=" command below, replacing everything after the "=" character.
::
::   If the StartTWS.bat file doesn't exist, you'll need to look in  the 
::   shortcut created when you installed TWS. How to find this depends on 
::   which version of Windows you're using:
::
::   - Windows 7: right click on the start menu entry for TWS and click 
::     'Properties'. 
::
::   - Windows 8: locate the tile for TWS in the start screen (or the 
::     'all apps' screen), right click it, and select 'Open file location'
::     on the menu bar at the bottom of the screen. Then right click on the
::     highlighted entry in the File Explorer window, and click 'Properties'.
::
::   - Windows 8.1: locate the tile for TWS in the start screen (or the 
::     'all apps' screen), right click it, and select 'Open file location'
::     in the context menu. Then right click on the highlighted entry in the
::     File Explorer window, and click 'Properties'.
::
::   - Windows 10: locate the tile for TWS in the start menu (or the 
::     'all apps' list), right click it, and select 'Open file location'
::     in the context menu. Then right click on the highlighted entry in
::     the File Explorer window, and click 'Properties'.
::
::   In the field labelled 'Target', select everything after "-cp " up to the 
::   first subsequent space character, then press Ctrl-C to copy it to the 
::   clipboard, then paste it into the "set TWSCP=" command below, replacing
::   everything after the "=" character:

set TWSCP=jts.jar;total.jar


::   Other Java VM options for TWS.
::
::   For TWS 952 and later, you can find this information in the tws.vmoptions 
::   file, which is located in C:\Jts\nnn where nnn is the TWS major version 
::   number.
::
::   For version prior to TWS 952, you can find this information in the 
::   START line in StartTWS.bat (or the shortcut created when you installed 
::   TWS if StartTWS.bat doesn't exist). Note that jclient/LoginFrame is
::   NOT part of the Java options, nor is anything that comes after it, 
::   so don't include that here):

set JAVAOPTS=-Dsun.java2d.noddraw=true -Dswing.boldMetal=false -Dsun.locale.formatasdefault=true -Xmx1024M


pushd %TWSDIR%

:: prevent other Java tools interfering with IBController
setlocal
set JAVA_TOOL_OPTIONS=

%SystemDrive%\ProgramData\Oracle\Java\javapath\java.exe -cp  %TWSCP%;%IBCDIR%\IBController.jar %JAVAOPTS% ibcontroller.IBController %IBCINI% %TWSUSERID% %TWSPASSWORD%
popd

