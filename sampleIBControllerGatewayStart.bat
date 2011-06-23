::   This command file starts the IB Gateway, which provides a low-resource capability
::   for running TWS API programs without the complex TWS user interface.

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


::   The classpath for the IB Gateway. The value below is correct for version 
::   918.6 (you can verify which version of the Gateway you are using by going
::   to the Help | About IB Gateway menu in the Gateway UI).
::
::   For other versions of the IB Gateway, the information needed may change.
::   You can find the required information in the gateway shortcut created when you 
::   installed TWS. To locate this, right click on the start menu entry for 
::   the IB Gateway and click Properties. In the field labelled 'Target', select 
::   everything after "-cp " up to the first subsequent space character, 
::   then press Ctrl-C to copy it to the clipboard, then paste it into the 
::   following command, replacing everything after the "=" character:

set TWSCP=jts.jar;hsqldb.jar;jcommon-1.0.12.jar;jhall.jar;other.jar;rss.jar


::   Other Java VM options for the IB Gateway. You can find this information in the 
::   shortcut created when you installed TWS. (Note that in the shortcut, 
::   ibgateway.GWClient is NOT part of the Java options, nor is anything 
::   that comes after it, so don't include that here):

set JAVAOPTS=-Dsun.java2d.noddraw=true -Xmx512M -XX:MaxPermSize=128M


pushd %TWSDIR%
java.exe -cp  %TWSCP%;%IBCDIR%\IBController.jar %JAVAOPTS% ibcontroller.IBGatewayController %IBCINI% %TWSUSERID% %TWSPASSWORD%
popd
