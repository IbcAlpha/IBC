#!/bin/bash

# Sends a RECONNECTACCOUNT command to a specified instance of IBC to reconnect
# to the IB login server. This is the same as the user pressing Ctrl-Alt-R. The
# IBC instance is specified in the commandsend.sh script file.

DIR="$(dirname "$(realpath "$0")")"
$DIR/commandsend.sh RECONNECTACCOUNT
