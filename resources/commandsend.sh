#!/bin/bash

# Sends a command to the specified instance of IBC, for example to cause it
# to initiate a tidy closedown or restart of TWS or Gateway

# You must supply the command as the first argument when you call this
# script. It is case-insensitive.


# Please read the notes below and make any required changes, then save this
# file before running it.

# You may need to change this line. Set it to the name or IP address of the 
# computer that is running IBC. Note that you can use the local loopback 
# address (127.0.0.1) if IBC is running on the current machine.

server_address=127.0.0.1

# You may need to change this line. Make sure it's set to the value of the 
# CommandServerPort setting in config.ini:

command_server_port=7462


# You shouldn't need to change anything below this line.
#==============================================================================


if [[ -z "$1" ]]; then
	>&2 echo -e "Error: you must supply a valid IBC command as the first argument"
	>&2 exit 1
fi

# send the required command to IBC 
(echo "$1"; sleep 1; echo "EXIT"; echo "quit" ) | /usr/local/bin/telnet "$server_address" $command_server_port


