@echo off
setlocal

:: Sends an ENABLEAPI command to a specified instance of IBC to ensure that the
:: ‘Enable ActiveX and Socket Clients’ checkbox in the API configuration is
:: set. The IBC instance is specified in the SendCommand.bat script file.

set HERE=%~dp0.

%HERE%/SendCommand.bat ENABLEAPI 
