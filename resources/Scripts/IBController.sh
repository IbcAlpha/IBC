#!/bin/bash

# Note that this command file is a 'service file' intended to be called from 
# higher level command files. There should be no reason for the end user to modify 
# it in any way. So PLEASE DON'T CHANGE IT UNLESS YOU KNOW WHAT YOU'RE DOING!

showUsage () {
echo
echo Runs IBController, thus loading TWS or the IB Gateway
echo
echo Usage:
echo
echo IBController twsVersion [-g \| --gateway] [--tws-path=twsPath] [--ibc-path=ibcPath]
echo "             [--ibc-ini=ibcIni] [--java-path=javaPath]"
echo "             [--user=userId] [--pw=password]"
echo
echo "  twsVersion              The major version number for TWS"
echo
echo "  -g or --gateway         Indicates that the IB Gateway is to be loaded rather"
echo "                          than TWS"
echo
echo "  twsPath                 Path to the TWS installation folder. Defaults to"
echo "                          ~/Jts"
echo
echo "  ibcPath                 Path to the IBController installation folder."
echo "                          Defaults to /opt/IBController"
echo
echo "  ibcIni                  The location and filename of the IBController "
echo "                          configuration file. Defaults to "
echo "                          ~/IBController/IBController.ini"
echo
echo "  javaPath                Path to the folder containing the java executable to"
echo "                          be used to run IBController. Defaults to the java"
echo "                          executable included in the TWS installation; failing "
echo "                          that, to the Oracle Java installation"
echo
echo "  userId                  IB account user id"
echo
echo "  password                IB account password"
echo
}

if [[ "$1" = "" || "$1" = "-?" || "$1" = "-h" || "$1" = "--HELP" ]]; then
	showUsage
	exit 0
fi


# Some constants

E_NO_JAVA=1
E_NO_TWS_VERSION=2
E_INVALID_ARG=3
E_TWS_VERSION_NOT_INSTALLED=4
E_IBC_PATH_NOT_EXIST=5
E_IBC_INI_NOT_EXIST=6
E_TWS_VMOPTIONS_NOT_FOUND=7

ENTRY_POINT_TWS=ibcontroller.IBController
ENTRY_POINT_GATEWAY=ibcontroller.IBGatewayController

ENTRY_POINT=$ENTRY_POINT_TWS

shopt -s nocasematch

for arg
do
	if [[ "$arg" = "-g" ]]; then
		ENTRY_POINT=$ENTRY_POINT_GATEWAY
	elif [[ "$arg" = "--gateway" ]]; then
		ENTRY_POINT=$ENTRY_POINT_GATEWAY
	elif [[ "${arg:0:11}" = "--tws-path=" ]]; then
		TWS_PATH=${arg:11}
	elif [[ "${arg:0:11}" = "--ibc-path=" ]]; then
		IBC_PATH=${arg:11}
	elif [[ "${arg:0:10}" = "--ibc-ini=" ]]; then
		IBC_INI=${arg:10}
	elif [[ "${arg:0:12}" = "--java-path=" ]]; then
		JAVA_PATH=${arg:12}
	elif [[ "${arg:0:7}" = "--user=" ]]; then
		IB_USER_ID=${arg:7}
	elif [[ "${arg:0:5}" = "--pw=" ]]; then
		IB_PASSWORD=${arg:5}
	elif [[ "${arg:0:1}" = "-" ]]; then
		echo Invalid parameter $arg
		exit $E_INVALID_ARG
	elif [[ "$TWS_VERSION" = "" ]]; then
		TWS_VERSION=$arg
	else
		echo Invalid parameter $arg
		exit $E_INVALID_ARG
	fi
done

#======================== Check everything ready to proceed ================

if [ "$TWS_VERSION" = "" ]; then
	echo TWS major version number has not been supplied - it must be the first argument
	exit $E_NO_TWS_VERSION
fi

if [ "$TWS_PATH" = "" ]; then TWS_PATH=~/Jts ;fi
if [ "$IBC_PATH" = "" ]; then IBC_PATH=/opt/IBController ;fi
if [ "$IBC_INI" = "" ]; then IBC_INI=~/IBController/IBController.ini ;fi

TWS_JARS=$TWS_PATH/$TWS_VERSION/jars

if [[ ! -e "$TWS_JARS" ]]; then
	echo TWS version $TWS_VERSION is not installed
	exit $E_TWS_VERSION_NOT_INSTALLED
fi

if [[ ! -e  "$IBC_PATH" ]]; then
	echo IBController path: $IBC_PATH does not exist
	exit $E_IBC_PATH_NOT_EXIST
fi

if [[ ! -e "$IBC_INI" ]]; then
	echo IBController configuration file: $IBC_INI  does not exist
	exit $E_IBC_INI_NOT_EXIST
fi

if [[ ! -e "$TWS_PATH/$TWS_VERSION/tws.vmoptions" ]]; then
	echo $TWS_PATH/$TWS_VERSION/tws.vmoptions does not exist
	exit $E_TWS_VMOPTIONS_NOT_FOUND
fi

if [[ -n $JAVA_PATH ]]; then
	if [[ ! -e "$JAVA_PATH/java" ]]; then 
		echo $JAVA_PATH/java does not exist
		exit $E_NO_JAVA
	fi
fi


echo =================================

echo Generating the classpath

for JAR in $TWS_JARS/*.jar; do
	if [[ -n $IBC_CLASSPATH ]]; then
		IBC_CLASSPATH=$IBC_CLASSPATH: 
	fi
	IBC_CLASSPATH=$IBC_CLASSPATH$JAR
done
IBC_CLASSPATH=$IBC_CLASSPATH:$IBC_PATH/IBController.jar

echo Classpath=$IBC_CLASSPATH
echo

#======================== Generate the JAVA VM options =====================

echo Generating the JAVA VM options

declare -a VM_OPTIONS
index=0
while read LINE; do
	if [[ ! "${LINE:0:1}" = "#" && ! "${LINE:0:2}" = "-D" ]]; then
		VM_OPTIONS[$index]="$LINE"
		((index++))
	fi
done < <( cat "$TWS_PATH/$TWS_VERSION/tws.vmoptions" )

JAVA_VM_OPTIONS=${VM_OPTIONS[*]}
echo Java VM Options=$JAVA_VM_OPTIONS
echo

#======================== Determine the location of java.exe ===============

echo Determining the location of java.exe 

if [[ ! -n $JAVA_PATH ]]; then
	if [[ -e "$TWS_PATH/$TWS_VERSION/.install4j/pref_jre.cfg" ]]; then
		read JAVA_PATH < $TWS_PATH/$TWS_VERSION/.install4j/pref_jre.cfg
		JAVA_PATH=$JAVA_PATH/bin
		echo $JAVA_PATH
		if [[ ! -e "$JAVA_PATH/java" ]]; then JAVA_PATH= ;fi
	fi
fi

if [[ ! -n $JAVA_PATH ]]; then
	if [[ -e "$TWS_PATH/$TWS_VERSION/.install4j/inst_jre.cfg" ]]; then
		read JAVA_PATH < $TWS_PATH/$TWS_VERSION/.install4j/inst_jre.cfg
		JAVA_PATH=$JAVA_PATH/bin
		echo $JAVA_PATH
		if [[ ! -e "$JAVA_PATH/java" ]]; then JAVA_PATH= ;fi
	fi
fi

if [[ ! -n $JAVA_PATH ]]; then
	echo Can\'t find suitable Java installation
	exit $E_NO_JAVA
fi

echo Location of java.exe=$JAVA_PATH
echo

#======================== Start IBController ===============================

# prevent other Java tools interfering with IBController
JAVA_TOOL_OPTIONS=

pushd $TWS_PATH

if [[ "$ENTRY_POINT" = "$ENTRY_POINT_TWS" ]]; then
	echo Starting IBController with this command:
else
	echo Starting IBGateway with this command:
fi
echo $JAVA_PATH/java -cp  $IBC_CLASSPATH $JAVA_VM_OPTIONS $ENTRY_POINT "$IBC_INI" $IB_USER_ID $IB_PASSWORD
echo
$JAVA_PATH/java -cp  $IBC_CLASSPATH $JAVA_VM_OPTIONS $ENTRY_POINT "$IBC_INI" $IB_USER_ID $IB_PASSWORD

popd

exit 0


