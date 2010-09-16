::   If your TWS user id and password are not included in the IBController 
::   configuration file, set them here (do not encrypt the password):

set TWSUSERID=
set TWSPASSWORD=


::   The folder containing the IBController files:

set IBCDIR=C:\IBController


::   The location and filename of the IBController configuration file:

set IBCINI=C:\IBController\ibcontroller.ini


::   The folder where TWS is installed:

set TWSDIR=C:\Jts\


::   The classpath for TWS. The value below is correct for versions of
::   TWS up to and including Build 903.7 (you can verify which version
::   of TWS you are using by going to the Help | ABout Trader Workstation
::   menu in TWS).
::
::   For later versions of TWS, the information needed may change.
::   You can find the required information in the shortcut created when you 
::   installed TWS. To locate this, right click on the start menu entry for 
::   TWS and click Properties. In the field labelled 'Target', select 
::   everything after "-cp " up to the first subsequent space character, 
::   then press Ctrl-C to copy it to the clipboard, then paste it into the 
::   following command, replacing everything after the "=" character:

set TWSCP=jts.jar;pluginsupport.jar;hsqldb.jar;jcommon-1.0.12.jar;jfreechart-1.0.9.jar;jhall.jar;other.jar;rss.jar;riskfeed.jar


::   Other Java VM options for TWS. You can find this information in the 
::   shortcut created when you installed TWS. (Note that in the shortcut, 
::   jclient.LoginFrame is NOT part of the Java options, nor is anything 
::   that comes after it, so don't include that here):

set JAVAOPTS=-Dsun.java2d.noddraw=true -Xmx512M


pushd %TWSDIR%
java.exe -cp  %TWSCP%;%IBCDIR%\IBController.jar %JAVAOPTS% ibcontroller.IBController %IBCINI% %TWSUSERID% %TWSPASSWORD%
popd

