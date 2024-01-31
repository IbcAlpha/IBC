#!/bin/bash

# Sends a RECONNECTDATA command to a specified instance of IBC to refresh
# all its market data connections. This is the same as the user pressing
# Ctrl-Alt-F. The IBC instance is specified in the commandsend.sh script file.

DIR="$(dirname "$(realpath "$0")")"
$DIR/commandsend.sh RECONNECTDATA
