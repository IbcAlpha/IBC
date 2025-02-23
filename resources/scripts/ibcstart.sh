#!/bin/bash

# Note that this command file is a 'service file' intended to be called from
# higher level command files. There should be no reason for the end user to modify
# it in any way. So PLEASE DON'T CHANGE IT UNLESS YOU KNOW WHAT YOU'RE DOING!

showUsage () {
echo
echo "Runs IBC, thus loading TWS or the IB Gateway"
echo
echo "Usage:"
echo
echo "ibcstart twsVersion [-g \| --gateway] [--tws-path=twsPath]"
echo "             [--tws-settings-path=twsSettingsPath] [--ibc-path=ibcPath]"
echo "             [--ibc-ini=ibcIni] [--java-path=javaPath]"
echo "             [--user=userid] [--pw=password]"
echo "             [--fix-user=fixuserid] [--fix-pw=fixpassword]"
echo "             [--mode=tradingMode]"
echo "             [--on2fatimeout=2fatimeoutaction]"
echo
echo "  twsVersion              The major version number for TWS"
echo
echo "  -g or --gateway         Indicates that the IB Gateway is to be loaded rather"
echo "                          than TWS"
echo
echo "  twsPath                 Path to the TWS installation folder. Defaults to"
echo "                          ~/Jts on Linux, ~/Applications on OS X"
echo
echo "  twsSettingsPath         Path to the TWS settings folder. Defaults to"
echo "                          the twsPath argument"
echo
echo "  ibcPath                 Path to the IBC installation folder."
echo "                          Defaults to /opt/ibc"
echo
echo "  ibcIni                  The location and filename of the IBC "
echo "                          configuration file. Defaults to "
echo "                          ~/ibc/config.ini"
echo
echo "  javaPath                Path to the folder containing the java executable to"
echo "                          be used to run IBC. Defaults to the java"
echo "                          executable included in the TWS installation; failing "
echo "                          that, to the Oracle Java installation"
echo
echo "  userid                  IB account user id"
echo
echo "  password                IB account password"
echo
echo "  fixuserid               FIX account user id (only if -g or --gateway)"
echo
echo "  fixpassword             FIX account password (only if -g or --gateway)"
echo
echo "  tradingMode             Indicates whether the live account or the paper "
echo "                          trading account will be used. Allowed values are:"
echo
echo "                              live"
echo "                              paper"
echo
echo "                          These values are not case-sensitive."
echo
echo "  2fatimeoutaction       Indicates what to do if IBC exits due to second factor"
echo "                         authentication timeout. Allowed values are:"
echo
echo "                              restart"
echo "                              exit"
echo
}

if [[ "$1" = "" || "$1" = "-?" || "$1" = "-h" || "$1" = "--HELP" ]]; then
	showUsage
	exit 0
fi

error_exit() {
	error_number=$1
	error_message=$2
	error_message1=$3
	error_message2=$4
	>&2 echo
	>&2 echo =========================== An error has occurred =============================
	>&2 echo
	>&2 echo
	>&2 echo
	>&2 echo -e "Error: ${error_message}"
	if [[ -n "${error_message1}" ]]; then
		>&2 echo -e "       ${error_message1}"
	fi
	if [[ -n "${error_message2}" ]]; then
		>&2 echo -e "       ${error_message2}"
	fi
	>&2 exit "${error_number}"
}


# Some constants

E_NO_JAVA=1
E_NO_TWS_VERSION=2
E_INVALID_ARG=3
E_TWS_VERSION_NOT_INSTALLED=4
E_IBC_PATH_NOT_EXIST=5
E_IBC_INI_NOT_EXIST=6
E_TWS_VMOPTIONS_NOT_FOUND=7
E_UNKNOWN_OPERATING_SYSTEM=8

# errorlevel set by IBC if second factor authentication dialog times out and
# ExitAfterSecondFactorAuthenticationTimeout setting is true
let E_2FA_DIALOG_TIMED_OUT=$((1111 % 256))

# errorlevel set by IBC if login dialog is not displayed within the time
# specified in the LoginDialogDisplayTimeout setting
E_LOGIN_DIALOG_DISPLAY_TIMEOUT=$((1112 % 256))


ENTRY_POINT_TWS=ibcalpha.ibc.IbcTws
ENTRY_POINT_GATEWAY=ibcalpha.ibc.IbcGateway

OS_LINUX=Linux
OS_OSX='OS X'

entry_point=$ENTRY_POINT_TWS
program=TWS

if [[ $OSTYPE = [lL]inux* ]]; then
	os=$OS_LINUX
elif [[ $(uname) = [dD]arwin* ]]; then
	os=$OS_OSX
else
	error_exit $E_UNKNOWN_OPERATING_SYSTEM "Can't detect operating system"
fi

shopt -s nocasematch extglob

echo "Parsing arguments"

for arg
do
	if [[ "$arg" = "-g" ]]; then
		entry_point=$ENTRY_POINT_GATEWAY
		program=Gateway
	elif [[ "$arg" = "--gateway" ]]; then
		entry_point=$ENTRY_POINT_GATEWAY
		program=Gateway
	elif [[ "${arg:0:11}" = "--tws-path=" ]]; then
		tws_path=${arg:11}
	elif [[ "${arg:0:20}" = "--tws-settings-path=" ]]; then
		tws_settings_path=${arg:20}
		tws_settings_path=${tws_settings_path%%+(/)}
	elif [[ "${arg:0:11}" = "--ibc-path=" ]]; then
		ibc_path=${arg:11}
	elif [[ "${arg:0:10}" = "--ibc-ini=" ]]; then
		ibc_ini=${arg:10}
	elif [[ "${arg:0:12}" = "--java-path=" ]]; then
		java_path=${arg:12}
	elif [[ "${arg:0:7}" = "--user=" ]]; then
		ib_user_id=${arg:7}
	elif [[ "${arg:0:5}" = "--pw=" ]]; then
		ib_password=${arg:5}
	elif [[ "${arg:0:11}" = "--fix-user=" ]]; then
		fix_user_id=${arg:11}
	elif [[ "${arg:0:9}" = "--fix-pw=" ]]; then
		fix_password=${arg:9}
	elif [[ "${arg:0:7}" = "--mode=" ]]; then
		mode=${arg:7}
    elif [[ "${arg:0:15}" = "--on2fatimeout=" ]]; then
	    twofa_to_action=${arg:15}
	elif [[ "${arg:0:1}" = "-" ]]; then
		error_exit $E_INVALID_ARG "Invalid parameter '${arg}'"
	elif [[ "$tws_version" = "" ]]; then
		tws_version=$arg
	else
		error_exit $E_INVALID_ARG "Invalid parameter '${arg}'"
	fi
done

if [[ -n "${fix_user_id}" || -n "${fix_password}" ]]; then
	if [[ ! "${program}" = "GATEWAY" ]]; then
		error_exit ${E_INVALID_ARG} "FIX user id and FIX password are only valid for the Gateway"
	fi
fi

mode_upper=$(echo ${mode} | tr '[:lower:]' '[:upper:]')
if [[ -n "${mode_upper}" && ! "${mode_upper}" = "LIVE" && ! "${mode_upper}" = "PAPER" ]]; then
	error_exit	${E_INVALID_ARG} "Trading mode set to ${mode} but must be either 'live' or 'paper' (case-insensitive)"
fi


twofa_to_action_upper=$(echo ${twofa_to_action} | tr '[:lower:]' '[:upper:]')
if [[ -n "${twofa_to_action_upper}" && ! "${twofa_to_action_upper}" = "RESTART" && ! "${twofa_to_action_upper}" = "EXIT" ]]; then
	error_exit	${E_INVALID_ARG} "2FA timeout action set to ${twofa_to_action} but must be either 'restart' or 'exit' (case-insensitive)"
fi

echo
echo -e "================================================================================"
echo
echo -e "Starting IBC version ${IBC_VRSN} on $(date +"%Y-%m-%d") at $(date +%T)"
echo
echo -e "Operating system: $(uname -a)"
echo

# log the arguments

echo Arguments:
echo
echo -e "TWS version = ${tws_version}"
echo -e "Program = ${program}"
echo -e "Entry point = ${entry_point}"
echo -e "--tws-path = ${tws_path}"
echo -e "--tws-settings-path = ${tws_settings_path}"
echo -e "--ibc-path = ${ibc_path}"
echo -e "--ibc-ini = ${ibc_ini}"
echo -e "--mode = ${mode}"
echo -e "--java-path = ${java_path}"
if [[ -z "${ib_user_id}" && -z "${ib_password}" ]]; then
	echo -e "--user ="
	echo -e "--pw ="
else
	echo -e "--user = ***"
	echo -e "--pw = ***"
fi
if [[ "${entry_point}" = "${ENTRY_POINT_GATEWAY}" ]]; then
	if [[ -z "${fix_user_id}" || -z "${fix_password}" ]]; then
		echo -e "--fix-user ="
		echo -e "--fix-pw ="
	else
		echo -e "--fix-user = ***"
		echo -e "--fix-pw = ***"
	fi
fi
echo

#======================== Check everything ready to proceed ================

if [ "$tws_version" = "" ]; then
	error_exit $E_NO_TWS_VERSION "TWS major version number has not been supplied"
fi

if [ "$os" = "$OS_LINUX" ]; then
	if [ "$tws_path" = "" ]; then tws_path=~/Jts ;fi
	if [ "$tws_settings_path" = "" ]; then tws_settings_path="${tws_path}" ;fi
	tws_program_path="${tws_path}/${tws_version}"
	gateway_program_path="${tws_path}/ibgateway/${tws_version}"
else
	if [ "$tws_path" = "" ]; then tws_path=~/Applications ;fi
	if [ "$tws_settings_path" = "" ]; then tws_settings_path=~/Jts ;fi
	tws_program_path="${tws_path}/Trader Workstation ${tws_version}"
	gateway_program_path="${tws_path}/IB Gateway ${tws_version}"
fi
if [ "$ibc_path" = "" ]; then ibc_path=/opt/ibc ;fi
if [ "$ibc_ini" = "" ]; then ibc_ini=~/ibc/config.ini ;fi

if [[ "${program}" = "TWS" ]] ; then
	program_path="${tws_program_path}"
	alt_program_path="${gateway_program_path}"
	vmoptions_source="${program_path}/tws.vmoptions"
	alt_vmoptions_source="${alt_program_path}/ibgateway.vmoptions"
else
	program_path="${gateway_program_path}"
	alt_program_path="${tws_program_path}"
	vmoptions_source="${program_path}/ibgateway.vmoptions"
	alt_vmoptions_source="${alt_program_path}/tws.vmoptions"
fi

if [[ ! -e "${program_path}/jars" ]]; then
	program_path="${alt_program_path}"
	vmoptions_source="${alt_vmoptions_source}"
fi
jars="${program_path}/jars"
install4j="${program_path}/.install4j"
	
if [[ ! -e "$tws_settings_path" ]]; then
	error_exit $E_IBC_PATH_NOT_EXIST "TWS settings path: $tws_settings_path does not exist"
fi

if [[ ! -e "$jars" ]]; then
	error_exit $E_TWS_VERSION_NOT_INSTALLED "Offline TWS/Gateway version $tws_version is not installed: can't find jars folder" \
	                                        "Make sure you install the offline version of TWS/Gateway" \
                                            "IBC does not work with the auto-updating TWS/Gateway"
fi

if [[ ! -e  "$ibc_path" ]]; then
	error_exit $E_IBC_PATH_NOT_EXIST "IBC path: $ibc_path does not exist"
fi

if [[ ! -e "$ibc_ini" ]]; then
	error_exit $E_IBC_INI_NOT_EXIST "IBC configuration file: $ibc_ini  does not exist"
fi

if [[ ! -e "$vmoptions_source" ]]; then
	error_exit $E_TWS_VMOPTIONS_NOT_FOUND "Neither tws.vmoptions nor ibgateway.vmoptions could be found"
fi

if [[ -n "$java_path" ]]; then
	if [[ ! -e "$java_path/java" ]]; then
		error_exit $E_NO_JAVA "Java installaton at $java_path/java does not exist"
	fi
fi


echo =================================

echo Generating the classpath

for jar in "${jars}"/*.jar; do
	if [[ -n "${ibc_classpath}" ]]; then
		ibc_classpath="${ibc_classpath}:"
	fi
	ibc_classpath="${ibc_classpath}${jar}"
done
ibc_classpath="${ibc_classpath}:$install4j/i4jruntime.jar:${ibc_path}/IBC.jar"

echo -e "Classpath=$ibc_classpath"
echo

#======================== Generate the JAVA VM options =====================

echo Generating the JAVA VM options

declare -a vm_options
index=0
if [[ "$os" = "$OS_LINUX" ]]; then
	while read line; do
		if [[ -n ${line} && ! "${line:0:1}" = "#" && ! "${line:0:2}" = "-D" ]]; then
			vm_options[$index]="$line"
			((index++))
		fi
	done <<< $(cat ${vmoptions_source})
elif [[ "$os" = "$OS_OSX" ]]; then
	while read line; do
		if [[ -n ${line} && ! "${line:0:1}" = "#" && ! "${line:0:2}" = "-D" ]]; then
			vm_options[$index]="$line"
			((index++))
		fi
	done < <( cat "$vmoptions_source" )
fi

java_vm_options=${vm_options[*]}
java_vm_options="$java_vm_options -Dtwslaunch.autoupdate.serviceImpl=com.ib.tws.twslaunch.install4j.Install4jAutoUpdateService"
java_vm_options="$java_vm_options -Dchannel=latest"
java_vm_options="$java_vm_options -Dexe4j.isInstall4j=true"
java_vm_options="$java_vm_options -Dinstall4jType=standalone"
java_vm_options="$java_vm_options -DjtsConfigDir=${tws_settings_path}"

ibc_session_id=$(mktemp -u XXXXXXXX)
java_vm_options="$java_vm_options -Dibcsessionid=$ibc_session_id"


function find_auto_restart {
	local autorestart_path=""
	local f=""
	restarted_needed=
	for i in $(find $tws_settings_path -type f -name "autorestart"); do
		local x=${i/$tws_settings_path/}
		local y=$(echo $x | xargs dirname)/.
		local e=$(echo "$y" | cut -d/ -f3)
		if [[ "$e" = "." ]]; then
			if [[ -z $f ]]; then
				f="$i"
				echo "autorestart file found at $f"
				autorestart_path=$(echo "$y" | cut -d/ -f2)
			else
				autorestart_path=
				echo "WARNING: deleting extra autorestart file found at $i"
				rm $i
				echo "WARNING: deleting first autorestart file found"
				rm $f
			fi
		fi
	done

	if [[ -z $autorestart_path ]]; then
		if [[ -n $f ]]; then
			echo "*******************************************************************************"
			echo "WARNING: More than one autorestart file was found. IBC can't determine which is"
			echo "         the right one, so they've all been deleted. Full authentication will"
			echo "         be required."
			echo
			echo "         If you have two or more TWS/Gateway instances with the same setting"
			echo "         for TWS_SETTINGS_PATH, you should ensure that they are configured with"
			echo "         different autorestart times, to avoid creation of multiple autorestart"
			echo "         files."
			echo "*******************************************************************************"
			echo
			restarted_needed=yes
		else 
			echo "autorestart file not found"
			echo
			restarted_needed=
		fi
	else
		echo "AUTORESTART_OPTION is -Drestart=${autorestart_path}"
		autorestart_option=" -Drestart=${autorestart_path}"
		restarted_needed=yes
	fi
}

find_auto_restart

echo -e "Java VM Options=$java_vm_options$autorestart_option"
echo

#======================== Determine the location of java executable ========

echo Determining the location of java executable

# preferably use java supplied with TWS installation

# Read a path from config file. If it contains a java executable,
# return the path to the executable. Return an empty string otherwise.
function read_from_config {
	path=$1
	if [[ -e "$path" ]]; then
		read java_path_from_config < "$path"
		if [[ -e "$java_path_from_config/bin/java" ]]; then
			echo -e "$java_path_from_config/bin"
		else
			>&2 echo -e "Could not find $java_path_from_config/bin/java"
			echo ""
		fi
	else
		echo ""
	fi
}

if [[ "$os" = "$OS_LINUX" ]]; then
	if [[ ! -n "$java_path" ]]; then
		java_path=$(read_from_config "$install4j/pref_jre.cfg")
	fi
	if [[ ! -n "$java_path" ]]; then
		java_path=$(read_from_config "$install4j/inst_jre.cfg")
	fi
elif [[ "$os" = "$OS_OSX" ]]; then
	java_path="$install4j/jre.bundle/Contents/Home/jre/bin"
	if [[ ! -e "$java_path/java" ]]; then
		java_path="$install4j/jre.bundle/Contents/Home/bin"
	fi
fi

# alternatively use installed java
if [[ ! -n "$java_path" ]]; then
	if type -p java > /dev/null; then
		echo Found java executable in PATH
		java_path=$(which java)
	elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
		echo Found java executable in JAVA_HOME
		java_path="$JAVA_HOME/bin/java"
	fi
fi

if [[ -z "$java_path" ]]; then
	error_exit $E_NO_JAVA "Can\'t find suitable Java installation"
elif [[ ! -e "$java_path/java" ]]; then
	error_exit $E_NO_JAVA "No java executable found in supplied path $java_path"
fi

if [[ $("$java_path/java" -XshowSettings:properties 2>&1) = *"java.runtime.version = 1.8"* ]]; then
	useJava8="yes"
else
	useJava8="no"
fi


echo Location of java executable=$java_path
echo

#======================== Start IBC ===============================

if [[ -n $fix_user_id || -n $fix_password ]]; then got_fix_credentials=1; fi
if [[ -n $ib_user_id || -n $ib_password ]]; then got_api_credentials=1; fi

if [[ -n $got_fix_credentials && -n $got_api_credentials ]]; then
	hidden_credentials="*** *** *** ***"
elif  [[ -n $got_fix_credentials ]]; then
		hidden_credentials="*** ***"
elif [[ -n $got_api_credentials ]]; then
		hidden_credentials="*** ***"
fi

# prevent other Java tools interfering with IBC
JAVA_TOOL_OPTIONS=

pushd "$tws_settings_path" > /dev/null

echo "Renaming IB's TWS or Gateway start script to prevent restart without IBC"
if [[ "$os" = "$OS_LINUX" ]]; then
	if [[ -e "${program_path}/tws" ]]; then mv "${program_path}/tws" "${program_path}/tws1"; fi
	if [[ -e "${program_path}/ibgateway" ]]; then mv "${program_path}/ibgateway" "${program_path}/ibgateway1"; fi
elif [[ "$os" = "$OS_OSX" ]]; then
	if [[ -e "${program_path}/Trader Workstation ${tws_version}.app" ]]; then mv "${program_path}/Trader Workstation ${tws_version}.app" "${program_path}/Trader Workstation ${tws_version}-1.app"; fi
	if [[ -e "${program_path}/IB Gateway ${tws_version}.app" ]]; then mv "${program_path}/IB Gateway ${tws_version}.app" "${program_path}/IB Gateway ${tws_version}-1.app"; fi
fi
echo

if [[ $useJava8 != "yes" ]]; then
moduleAccess="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-exports=java.base/sun.util=ALL-UNNAMED --add-exports=java.desktop/com.sun.java.swing.plaf.motif=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.dnd=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.desktop/javax.swing.event=ALL-UNNAMED --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED --add-opens=java.desktop/javax.swing.table=ALL-UNNAMED --add-opens=java.desktop/sun.awt=ALL-UNNAMED --add-exports=java.desktop/sun.awt.X11=ALL-UNNAMED --add-exports=java.desktop/sun.swing=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports=javafx.media/com.sun.media.jfxmedia=ALL-UNNAMED --add-exports=javafx.media/com.sun.media.jfxmedia.events=ALL-UNNAMED --add-exports=javafx.media/com.sun.media.jfxmedia.locator=ALL-UNNAMED --add-exports=javafx.media/com.sun.media.jfxmediaimpl=ALL-UNNAMED --add-exports=javafx.web/com.sun.javafx.webkit=ALL-UNNAMED --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED"
fi

while :
do
	echo "Starting $program with this command:"
	echo -e "\"$java_path/java\" $moduleAccess -cp \"$ibc_classpath\" $java_vm_options$autorestart_option $entry_point \"$ibc_ini\" $hidden_credentials ${mode}"
	echo

	# forward signals (see https://veithen.github.io/2014/11/16/sigterm-propagation.html)
	trap 'kill -TERM $PID' TERM INT

	if [[ -n $got_fix_credentials && -n $got_api_credentials ]]; then
		"$java_path/java" $moduleAccess -cp "$ibc_classpath" $java_vm_options$autorestart_option $entry_point "$ibc_ini" "$fix_user_id" "$fix_password" "$ib_user_id" "$ib_password" ${mode} &
	elif  [[ -n $got_fix_credentials ]]; then
		"$java_path/java" $moduleAccess -cp "$ibc_classpath" $java_vm_options$autorestart_option $entry_point "$ibc_ini" "$fix_user_id" "$fix_password" ${mode} &
	elif [[ -n $got_api_credentials ]]; then
		"$java_path/java" $moduleAccess -cp "$ibc_classpath" $java_vm_options$autorestart_option $entry_point "$ibc_ini" "$ib_user_id" "$ib_password" ${mode} &
	else
		"$java_path/java" $moduleAccess -cp "$ibc_classpath" $java_vm_options$autorestart_option $entry_point "$ibc_ini" ${mode} &
	fi

	PID=$!
	wait $PID
	trap - TERM INT
	wait $PID

	exit_code=$(($? % 256))
	echo "IBC returned exit status $exit_code"

	if [[ $exit_code -eq $E_LOGIN_DIALOG_DISPLAY_TIMEOUT ]]; then 
		:
	elif [[ -e "$tws_settings_path/COLDRESTART$ibc_session_id" ]]; then
		rm "$tws_settings_path/COLDRESTART$ibc_session_id"
		autorestart_option=
		echo "IBC will cold-restart shortly"
	else
		find_auto_restart
		if [[ -n $restarted_needed ]]; then
			restarted_needed=
			# restart using the TWS/Gateway-generated autorestart file
			:
		elif [[ $exit_code -ne $E_2FA_DIALOG_TIMED_OUT  ]]; then 
			break;
		elif [[ ${twofa_to_action_upper} !=  "RESTART" ]]; then 
			break; 
		fi
	fi
	
	# wait a few seconds before restarting
	echo IBC will restart shortly
	echo sleep 2
done

echo "$program finished"
echo

popd > /dev/null

exit $exit_code


