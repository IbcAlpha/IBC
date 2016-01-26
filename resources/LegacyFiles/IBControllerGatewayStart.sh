#!/bin/bash
#   This command file starts the IB Gateway, which provides a low-resource capability
#   for running TWS API programs without the complex TWS user interface.

#   If your TWS user id and password are not included in the IBController 
#   configuration file, set them here (do not encrypt the password):

TWSUSERID=
TWSPASSWORD=


#   The folder containing the IBController files:

IBCDIR=/opt/IBController/


#   The location and filename of the IBController configuration file. This file should
#   be in a folder in your personal filestore, so that other users of your computer can't
#   access it. This folder and its contents should also be encrypted so that even users
#   with administrator privileges can't see the contents:

IBCINI="$HOME/IBController/IBController.ini"


#   The folder where TWS is installed:

TWSDIR=/opt/IBJts


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

JAVAOPTS='-Xmx768M -XX:MaxPermSize=256M'

pushd $TWSDIR
# prevent other Java tools interfering with IBController
unset JAVA_TOOL_OPTIONS
java -cp  $TWSCP:$IBCDIR/IBController.jar $JAVAOPTS ibcontroller.IBGatewayController $IBCINI $TWSUSERID $TWSPASSWORD
popd

