#!/bin/bash

# get the IBC version
read IBC_VRSN < "${IBC_PATH}/version"

if [[ -n "$LOG_PATH" ]]; then
	if [[ ! -e  "$LOG_PATH" ]]; then
		mkdir -p "$LOG_PATH"
	fi

	readme=${LOG_PATH}/README.txt
	if [[ ! -e  "$readme" ]]; then
		echo You can delete the files in this folder at any time > "$readme"
		echo >> "$readme"
		echo "You'll be informed if a file is currently in use." >> "$readme"
	fi

	log_file=${LOG_PATH}/ibc-${IBC_VRSN}_${APP}-${TWS_MAJOR_VRSN}_$(date +%A).txt
	if [[ -e "$log_file" ]]; then
		if [[ $(uname) = [dD]arwin* ]]; then
			if [[ $(stat -f "%Sm" -t %D "$log_file") != $(date +%D) ]]; then rm "$log_file"; fi
		else
			if [[ $(date -r "$log_file" +%D) != $(date +%D) ]]; then rm "$log_file"; fi
		fi
	fi
else
	log_file=/dev/null
fi

#   now launch IBC

normal='\033[0m'
light_red='\033[1;31m'
light_green='\033[1;32m'
echo -e "${light_green}+=============================================================================="
echo "+"
echo -e "+ IBC version ${IBC_VRSN}"
echo "+"
echo -e "+ Running ${APP} ${TWS_MAJOR_VRSN}"
echo "+"
if [[ -n "$LOG_PATH" ]]; then
	echo "+ Diagnostic information is logged in:"
	echo "+"
	echo -e "+ ${log_file}"
	echo "+"
fi
echo -e "+${normal}"

if [[ "$(echo ${APP} | tr '[:lower:]' '[:upper:]')" = "GATEWAY" ]]; then
	gw_flag=-g
fi

export IBC_VRSN

# forward signals (see https://veithen.github.io/2014/11/16/sigterm-propagation.html)
trap 'kill -TERM $PID' TERM INT

"${IBC_PATH}/scripts/ibcstart.sh" "${TWS_MAJOR_VRSN}" ${gw_flag} \
     "--tws-path=${TWS_PATH}" "--tws-settings-path=${TWS_SETTINGS_PATH}" \
     "--ibc-path=${IBC_PATH}" "--ibc-ini=${IBC_INI}" \
     "--user=${TWSUSERID}" "--pw=${TWSPASSWORD}" "--fix-user=${FIXUSERID}" "--fix-pw=${FIXPASSWORD}" \
     "--java-path=${JAVA_PATH}" "--mode=${TRADING_MODE}" \
     >> "${log_file}" 2>&1 &

PID=$!
wait $PID
trap - TERM INT
wait $PID

exit_code=$?
if [ "$exit_code" == "0" ]; then
	echo -e "${light_green}+ ${APP} ${TWS_MAJOR_VRSN} has finished"
	echo "+"
	echo -e "+==============================================================================${normal}"
elif [ "$exit_code" == "143" ]; then
	# exit code 143 caused by default signal handler for SIGTERM
	exit_code=0
	echo -e "${light_green}+"
	echo "+ IBC terminated by SIGTERM"
	echo "+"
	echo -e "${light_green}+ ${APP} ${TWS_MAJOR_VRSN} has finished"
	echo "+"
	echo -e "+==============================================================================${normal}"
else
	echo -e "${light_red}+=============================================================================="
	echo "+"
	echo -e "+                       **** An error has occurred ****"
	if [[ -n LOG_PATH ]]; then
		echo "+"
		echo "+                     Please look in the diagnostics file "
		echo "+                   mentioned above for further information"
	fi
	echo "+"
  echo "+                           Press enter to continue."
	echo "+"
	echo -e "+==============================================================================${normal}"
	read
fi

exit $exit_code
