@echo off
setlocal

:: Sends a RECONNECTACCOUNT command to a specified instance of IBC to reconnect
:: to the IB login server. This is the same as the user pressing Ctrl-Alt-R. The
:: IBC instance is specified in the SendComand.bat script file.

set HERE=%~dp0.

%HERE%/SendCommand.bat RECONNECTACCOUNT
