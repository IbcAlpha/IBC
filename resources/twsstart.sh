#!/bin/bash

#=============================================================================+
#                                                                             +
#   This command file starts the Interactive Brokers' Trader Workstation.     +
#                                                                             +
#   If you run it without any arguments it will display a new window showing  +
#   useful information and then start TWS. If you supply -inline as           +
#   the first argument, the information will be displayed in the current      +
#   terminal window.                                                          +
#                                                                             +
#   The following lines are the only ones you may need to change, and you     +
#   probably only need to change the first one.                               +
#                                                                             +
#   The notes below give further information on why you might need to         +
#   change them.                                                              +
#                                                                             +
#=============================================================================+


TWS_MAJOR_VRSN=1019
IBC_INI=~/ibc/config.ini
TRADING_MODE=
TWOFA_TIMEOUT_ACTION=exit
IBC_PATH=/opt/ibc
TWS_PATH=~/Jts
TWS_SETTINGS_PATH=
LOG_PATH=~/ibc/logs
TWSUSERID=
TWSPASSWORD=
JAVA_PATH=
HIDE=


#              PLEASE DON'T CHANGE ANYTHING BELOW THIS LINE !!
#==============================================================================

#   Notes:
#

#   TWS_MAJOR_VRSN
#
#     Specifies the major version number of TWS to be run. If you are
#     unsure of which version number to use, run TWS manually from the
#     icon on the desktop, then click Help > About Trader Workstation. In the
#     displayed information you'll see a line similar to this:
#
#       Build 10.19.1f, Oct 28, 2022 3:03:08 PM
#
#     The major version number is 1019 (ie ignore the period after the first
#     part of the version number).
#
#     Do not include the rest of the version number in this setting.


#   IBC_INI
#
#     This is the location and filename of the IBC configuration file.
#     This file should be in a folder in your personal filestore, so that
#     other users of your computer can't access it. This folder and its
#     contents should also be encrypted so that even users with administrator
#     privileges can't see the contents.


#   TRADING_MODE
#
#     This indicates whether the live account or the paper trading account 
#     corresponding to the supplied credentials is to be used. The values 
#     allowed here are 'live' and 'paper' (not case-sensitive). If no value
#     is specified here, the value is taken from the TradingMode setting in
#     the configuration file. If no value is specified there either, the
#     value 'live' is assumed. 
#
#     If this is set to 'live', then the credentials for the live account
#     must be supplied. If it is set to 'paper', then either the live or
#     the paper-trading credentials may be supplied.


#   TWOFA_TIMEOUT_ACTION
#
#     If you use the IBKR Mobile app for second factor authentication, and
#     after you acknowledge the alert login fails to proceed, this
#     setting determines what action will occur. If you set it to 'restart',
#     IBC will be automatically restarted and the authentication sequence
#     will be repeated, giving you another opportunity to complete the login.
#     If you set it to 'exit', IBC will simply terminate.
#
#     Note that if you have another automated mechanism (such as crontab)
#     to periodically restart IBC, you should set this to 'exit'.
#
#     Note also that if you set this to 'restart', you must also set 
#     ReloginAfterSecondFactorAuthenticationTimeout=yes in your config.ini file.


#   IBC_PATH
#
#     The folder containing the IBC files.


#   TWS_PATH
#
#     The folder where TWS is installed. The TWS installer always installs to
#     ~/Jts. Note that even if you have installed from a Gateway download
#     rather than a TWS download, you should still use this default setting.
#     It is possible to move the TWS installation to a different folder, but
#     there are virtually no good reasons for doing so.


#   TWS_SETTINGS_PATH
#
#     The folder where TWS is to store its settings. By default it uses the
#     folder specified in TWS_PATH.
#
#     Is is also possible to specify this folder via the IbDir setting in
#     the configuration file. If TWS is set to auto-restart each day
#     (ie without having to log in again each time), then you must specify
#     the settings folder here rather than via IbDir: this means that these
#     two settings must either be identical, or the IbDir setting must be
#     left unset. If they are different, auto-restart will fail.
#
#     The recommended approach is to NOT use the IbDir setting in the
#     configuration file. 
#
#     Note that if multiple IB accounts are used such as live and paper
#     accounts for the same user, or accounts for different users), then
#     they should either each have a unique settings folder, or autorestart
#     must be configured to occur at a different time for each account:
#     concurrent auto-restarts may interferec and not succeed. You could
#     achieve this, for example, by having different versions of this file
#     for different users.


#   LOG_PATH
#
#     Specifies the folder where diagnostic information is to be logged while
#     this command file is running. This information is very valuable when
#     troubleshooting problems, so it is advisable to always have this set to
#     a valid location, especially when setting up IBC. You must
#     have write access to the specified folder.
#
#     If no value is set, log information is sent to the terminal window.
#
#     If the setting is removed entirely (or commented out), no log information
#     is captured at all (but this is not recommended).


#   TWSUSERID
#   TWSPASSWORD
#
#     If your IBKR user id and password are not included in your IBC
#     configuration file, you can set them here (do not encrypt the password).
#     However you are strongly advised not to set them here because this file
#     is not normally in a protected location.


#   JAVA_PATH
#
#     IB's installer for TWS/Gateway includes a hidden version of Java which
#     IB have used to develop and test that particular version. This means that
#     it is not necessary to separately install Java. If there is a separate
#     Java installation, that does not matter: it won't be used by IBC or
#     TWS/Gateway unless you set the path to it here. You should not do this
#     without a very good reason.


#   HIDE
#
#     If set to YES or TRUE, the diagnostic window that contains information
#     about the running TWS, and where to find the log file, will be iconified.
#     If not set, or set to any other value, the window will be displayed.
#     Values are not case-sensitive so for example yEs and yes are interpeted
#     as YES. (Note that when the -inline argument is supplied, this setting
#     has no effect.)


#   End of Notes:
#==============================================================================

if [[ -x "${IBC_PATH}/scripts/displaybannerandlaunch.sh" ]]; then
	:
elif [[ -x "${IBC_PATH}/scripts/ibcstart.sh" ]]; then
	:
else
	>&2 echo -e "Error: no execute permission for scripts in ${IBC_PATH}/scripts"
	>&2 exit 1
fi

if [[ -n $(/usr/bin/pgrep -f "java.*${IBC_INI}") ]]; then
	>&2 echo -e "Error: process is already running"
	>&2 exit 1
fi

APP=TWS

export TWS_MAJOR_VRSN
export IBC_INI
export TRADING_MODE
export TWOFA_TIMEOUT_ACTION
export IBC_PATH
export TWS_PATH
export TWS_SETTINGS_PATH
export LOG_PATH
export TWSUSERID
export TWSPASSWORD
export JAVA_PATH
export APP

hide="$(echo ${HIDE} | tr '[:lower:]' '[:upper:]')"
if [[ "$hide" = "YES" || "$hide" = "TRUE" ]]; then
	iconic=-iconic
fi

if [[ "$1" == "-inline" ]]; then
    exec "${IBC_PATH}/scripts/displaybannerandlaunch.sh"
else
    title="IBC ($APP $TWS_MAJOR_VRSN)"
    xterm $iconic -T "$title" -e "${IBC_PATH}/scripts/displaybannerandlaunch.sh" &
fi
