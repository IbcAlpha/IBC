#!/bin/bash

# Note that this command file is a 'service file' intended to be called from 
# higher level command files. There should be no reason for the end user to modify 
# it in any way. So PLEASE DON'T CHANGE IT UNLESS YOU KNOW WHAT YOU'RE DOING!

showUsage () {
echo
echo "Runs IBController, thus loading TWS or the IB Gateway"
echo
echo "Usage:"
echo
echo "IBController twsVersion [-g \| --gateway] [--tws-path=twsPath] [--ibc-path=ibcPath]"
echo "             [--ibc-ini=ibcIni] [--java-path=javaPath]"
echo "             [--user=userid] [--pw=password]"
echo "             [--fix-user=fixuserid] [--fix-pw=fixpassword]"
echo "             [--mode=tradingMode]"
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
}

if [[ "$1" = "" || "$1" = "-?" || "$1" = "-h" || "$1" = "--HELP" ]]; then
	showUsage
	exit 0
fi

error_exit() {
	error_number=$1
	error_message=$2
	>&2 echo
	>&2 echo =========================== An error has occurred =============================
	>&2 echo
	>&2 echo
	>&2 echo
	>&2 echo -e "Error: ${error_message}"
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

ENTRY_POINT_TWS=ibcontroller.IBController
ENTRY_POINT_GATEWAY=ibcontroller.IBGatewayController

entry_point=$ENTRY_POINT_TWS

shopt -s nocasematch

for arg
do
	if [[ "$arg" = "-g" ]]; then
		entry_point=$ENTRY_POINT_GATEWAY
	elif [[ "$arg" = "--gateway" ]]; then
		entry_point=$ENTRY_POINT_GATEWAY
	elif [[ "${arg:0:11}" = "--tws-path=" ]]; then
		tws_path=${arg:11}
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
	elif [[ "${arg:0:1}" = "-" ]]; then
		error_exit $E_INVALID_ARG "Invalid parameter '${arg}'"
	elif [[ "$tws_version" = "" ]]; then
		tws_version=$arg
	else
		error_exit $E_INVALID_ARG "Invalid parameter '${arg}'"
	fi
done

if [[ -n "${fix_user_id}" || -n "${fix_password}" ]]; then
	if [[ ! "${entry_point}" = "${ENTRY_POINT_GATEWAY}" ]]; then
		error_exit ${E_INVALID_ARG} "FIX user id and FIX password are only valid for the Gateway"
	fi
fi

if [[ -n "${mode}" && ! "${mode^^}" = "LIVE" && ! "${mode^^}" = "PAPER" ]]; then
	error_exit	${E_INVALID_ARG} "Trading mode must be either 'live' or 'paper'"
fi

echo
echo -e "================================================================================"
echo
echo -e "Starting IBController version ${IBC_VRSN} on $(date -I) at $(date +%T)"
echo

# log the arguments

echo Arguments:
echo
echo -e "TWS version = ${tws_version}"
echo -e "Entry point = ${entry_point}"
echo -e "--twspath = ${tws_path}"
echo -e "--ibcpath = ${ibc_path}"
echo -e "--ibcini = ${ibc_ini}"
echo -e "--mode = ${mode}"
echo -e "--javapath = ${java_path}"
if [[ -z "${ib_user_id}" && -z "${ib_password}" ]]; then
	echo -e "--user ="
	echo -e "--pw ="
else 
	echo -e "--user = ***"
	echo -e "--pw = ***"
fi
if [[ "${entry_point}" = "${ENTRY_POINT_GATEWAY}" ]]; then
	if [[ -z "${fix_user_id}" || -z "${fix_password}" ]]; then
		echo -e "--fixuser ="
		echo -e "--fixpw ="
	else 
		echo -e "--fixuser = ***"
		echo -e "--fixpw = ***"
	fi
fi
echo

#======================== Check everything ready to proceed ================

if [ "$tws_version" = "" ]; then
	error_exit $E_NO_TWS_VERSION "TWS major version number has not been supplied"
fi

if [ "$tws_path" = "" ]; then tws_path=~/Jts ;fi
if [ "$ibc_path" = "" ]; then ibc_path=/opt/IBController ;fi
if [ "$ibc_ini" = "" ]; then ibc_ini=~/IBController/IBController.ini ;fi

# In the following we try to use the correct .vmoptions file for the chosen entrypoint
# Note that uninstalling TWS or Gateway leaves the relevant .vmoption file in place, so
# we can still use the correct one.
if [[ "${entry_point}" = "${ENTRY_POINT_TWS}" ]]; then
	if [[ -e "${tws_path}/${tws_version}/tws.vmoptions" ]]; then
		tws_vmopts=${tws_path}/${tws_version}/tws.vmoptions 
	elif [[ -e "${tws_path}/ibgateway/${tws_version}/ibgateway.vmoptions" ]]; then
		tws_vmopts=${tws_path}/ibgateway/${tws_version}/ibgateway.vmoptions 
	fi 

	if [[ -e "${tws_path}/${tws_version}/jars" ]]; then
		tws_jars=${tws_path}/${tws_version}/jars
		install4j=${tws_path}/${tws_version}/.install4j
	else 
		tws_jars=${tws_path}/ibgateway/${tws_version}/jars
		install4j=${tws_path}/ibgateway/${tws_version}/.install4j
	fi
fi
if [[ "${entry_point}" = "${ENTRY_POINT_GATEWAY}" ]]; then
	if [[ -e "${tws_path}/ibgateway/${tws_version}/ibgateway.vmoptions" ]]; then
		tws_vmopts=${tws_path}/ibgateway/${tws_version}/ibgateway.vmoptions 
	elif [[ -e "${tws_path}/${tws_version}/tws.vmoptions" ]]; then
		tws_vmopts=${tws_path}/${tws_version}/tws.vmoptions 
	fi

	if [[ -e "${tws_path}/ibgateway/${tws_version}/jars" ]]; then
		tws_jars=${tws_path}/ibgateway/${tws_version}/jars
		install4j=${tws_path}/ibgateway/${tws_version}/.install4j
	else
		tws_jars=${tws_path}/${tws_version}/jars
		install4j=${tws_path}/${tws_version}/.install4j
	fi
fi

if [[ ! -e "$tws_jars" ]]; then
	error_exit $E_TWS_VERSION_NOT_INSTALLED "TWS version $tws_version is not installed"
fi

if [[ ! -e  "$ibc_path" ]]; then
	error_exit $E_IBC_PATH_NOT_EXIST "IBController path: $ibc_path does not exist"
fi

if [[ ! -e "$ibc_ini" ]]; then
	error_exit $E_IBC_INI_NOT_EXIST "IBController configuration file: $ibc_ini  does not exist"
fi

if [[ ! -e "$tws_path/$tws_version/tws.vmoptions" ]]; then
	error_exit $E_TWS_VMOPTIONS_NOT_FOUND "$tws_path/$tws_version/tws.vmoptions does not exist"
fi

if [[ -n "$java_path" ]]; then
	if [[ ! -e "$java_path/java" ]]; then
		error_exit $E_NO_JAVA "$java_path/java does not exist"
	fi
fi


echo =================================

echo Generating the classpath

for jar in "${tws_jars}"/*.jar; do
	if [[ -n "${ibc_classpath}" ]]; then
		ibc_classpath="${ibc_classpath}:"
	fi
	ibc_classpath="${ibc_classpath}${jar}"
done
ibc_classpath="${ibc_classpath}:${ibc_path}/IBController.jar"

echo -e "Classpath=$ibc_classpath"
echo

#======================== Generate the JAVA VM options =====================

echo Generating the JAVA VM options

declare -a vm_options
index=0
while read line; do
	if [[ -n ${line} && ! "${line:0:1}" = "#" && ! "${line:0:2}" = "-D" ]]; then
		vm_options[$index]="$line"
		((index++))
	fi
done < <( cat "$tws_path/$tws_version/tws.vmoptions" )

java_vm_options=${vm_options[*]}
echo -e "Java VM Options=$java_vm_options"
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

tws_installer="$tws_path/$tws_version/.install4j"
if [[ ! -n "$java_path" ]]; then
	java_path=$(read_from_config "$tws_installer/pref_jre.cfg")
fi
if [[ ! -n "$java_path" ]]; then
	java_path=$(read_from_config "$tws_installer/inst_jre.cfg")
fi

# alternatively use installed java, if its from oracle (openJDK causes problems with TWS)
if [[ ! -n "$java_path" ]]; then
	if type -p java > /dev/null; then
		echo Found java executable in PATH
		system_java=java
	elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
		echo Found java executable in JAVA_HOME
		system_java="$JAVA_HOME/bin/java"
	fi

	if [[ "$system_java" ]]; then
		if [[ $($system_java -XshowSettings:properties -version 2>&1) == *"Java(TM) SE Runtime Environment"* ]]; then
			java_path=$(dirname $(which $system_java))
		else
			>&2 echo "System java $system_java is not from Oracle, won't use it"
		fi
	fi
fi

if [[ -z "$java_path" ]]; then
	error_exit $E_NO_JAVA "Can\'t find suitable Java installation"
elif [[ ! -e "$java_path/java" ]]; then
	error_exit $E_NO_JAVA "No java executable found in supplied path $java_path"
fi

echo Location of java executable=$java_path
echo

#======================== Start IBController ===============================

if [[ -n $fix_user_id || -n $fix_password ]]; then got_fix_credentials=1; fi
if [[ -n $ib_user_id || -n $ib_password ]]; then got_api_credentials=1; fi

echo -e "if [[ -n $got_fix_credentials && -n $got_api_credentials ]]; then"
if [[ -n $got_fix_credentials && -n $got_api_credentials ]]; then
	hidden_credentials="*** *** *** ***"
elif  [[ -n $got_fix_credentials ]]; then
		hidden_credentials="*** ***"
elif [[ -n $got_api_credentials ]]; then
		hidden_credentials="*** ***"
fi

if [[ "$entry_point" = "$ENTRY_POINT_TWS" ]]; then
	program=IBController
else
	program=IBGateway
fi
echo "Starting $program with this command:"
echo -e "$java_path/java -cp $ibc_classpath $java_vm_options $entry_point $ibc_ini $hidden_credentials ${mode}" 
echo

# prevent other Java tools interfering with IBController
JAVA_TOOL_OPTIONS=

pushd "$tws_path" > /dev/null

if [[ -n $got_fix_credentials && -n $got_api_credentials ]]; then
	"$java_path/java" -cp "$ibc_classpath" $java_vm_options $entry_point "$ibc_ini" "$fix_user_id" "$fix_password" "$ib_user_id" "$ib_password" ${mode}
elif  [[ -n $got_fix_credentials ]]; then
	"$java_path/java" -cp "$ibc_classpath" $java_vm_options $entry_point "$ibc_ini" "$fix_user_id" "$fix_password" ${mode}
elif [[ -n $got_api_credentials ]]; then
	"$java_path/java" -cp "$ibc_classpath" $java_vm_options $entry_point "$ibc_ini" "$ib_user_id" "$ib_password" ${mode}
else
	"$java_path/java" -cp "$ibc_classpath" $java_vm_options $entry_point "$ibc_ini" ${mode}
fi

exit_code=$?
echo "$program finished"
echo

popd > /dev/null

exit $exit_code


