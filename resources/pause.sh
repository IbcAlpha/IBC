#!/bin/bash

# Sends a RESTART command to a specified instance of IBC to cause it to
# shut down TWS or Gateway in such a way that when restarted, the session
# will continue without requiring re-authentication. The IBC
# instance is specified in the commandsend.sh script file.

DIR="$(dirname "$(realpath "$0")")"
$DIR/commandsend.sh PAUSE
