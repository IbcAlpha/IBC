@echo off
setlocal

:: Sends a RECONNECTDATA command to a specified instance of IBC to refresh
:: all its market data connections. This is the same as the user pressing
:: Ctrl-Alt-F. The IBC instance is specified in the SendCommand.bat script file.

set HERE=%~dp0.

%HERE%/SendCommand.bat RECONNECTDATA
