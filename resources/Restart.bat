@echo off
setlocal

:: Sends a RESTART command to a specified instance of IBC to cause it to
:: restart TWS or Gateway without requiring re-authentication. The IBC
:: instance is specified in the SendCommand.bat script file.

set HERE=%~dp0.

%HERE%/SendCommand.bat RESTART
