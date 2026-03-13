@echo off
setlocal

:: Sends a PAUSE command to a specified instance of IBC to cause it to
:: shut down TWS or Gateway in such a way that when restarted, the session
:: will continue without requiring re-authentication. The IBC
:: instance is specified in the SendCommand.bat script file.

set HERE=%~dp0.

%HERE%/SendCommand.bat PAUSE
