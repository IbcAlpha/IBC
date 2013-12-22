#!/bin/bash
#   This command file starts the IB Gateway, which provides a low-resource capability
#   for running TWS API programs without the complex TWS user interface.

#   If your TWS user id and password are not included in the IBController 
#   configuration file, set them here (do not encrypt the password):

TWSUSERID=
TWSPASSWORD=


#   The folder containing the IBController files:

IBCDIR=/home/<username>/IBController/


#   The location and filename of the IBController configuration file:

IBCINI=/home/<username>/IBController/IBController.ini


#   The folder where TWS is installed:

TWSDIR=/home/<username>/IBJts


#   The classpath for the IB Gateway. The value below is correct for version
#   942 (you can verify which version of TWS you are using by going
#   to the Help | About Trader Workstation menu in TWS).
#
#   For other versions of the IB Gateway, the information needed may change.
#   You can find the required information in the UNIX/MacOsX download instructions
#   for TWS on the IB website, which includes a sample command to start TWS.
#
#   You must include everything after "-cp " up to the 
#   first subsequent space character:

TWSCP=jts.jar:total.2013.jar


#   Other Java VM options for the IB Gateway. You can find this information in the 
#   UNIX/MacosX download instructions for TWS on the IB website. (Note that
#   ibgateway.GWClient is NOT part of the Java options, nor is anything 
#   that comes after it, so don't include that here):

JAVAOPTS=-Xmx512M -XX:MaxPermSize=128M 

pushd $TWSDIR
java -cp  $TWSCP:$IBCDIR/IBController.jar $JAVAOPTS ibcontroller.IBGatewayController $IBCINI $TWSUSERID $TWSPASSWORD
popd

