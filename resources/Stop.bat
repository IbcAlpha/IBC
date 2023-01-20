@echo off
setlocal

:: Sends a STOP command to a specified instance of IBC to cause it to
:: initiate a tidy closedown of TWS or Gateway. The IBC instance is
:: specified in the SendCommand.bat script file.

set HERE=%~dp0.

%HERE%/SendCommand.bat STOP
