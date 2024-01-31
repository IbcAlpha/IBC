#!/bin/bash

# Sends a RESTART command to a specified instance of IBC to cause it to
# initiate a tidy closedown of TWS or Gateway. The IBC instance is
# specified in the commandsend.sh script file.

DIR="$(dirname "$(realpath "$0")")"
$DIR/commandsend.sh RESTART
