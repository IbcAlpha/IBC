#!/bin/bash

# Sends an ENABLEAPI command to a specified instance of IBC to ensure that the
# ‘Enable ActiveX and Socket Clients’ checkbox in the API configuration is
# set. The IBC instance is specified in the commandsend.sh script file.

DIR="$(dirname "$(realpath "$0")")"
$DIR/commandsend.sh ENABLEAPI
