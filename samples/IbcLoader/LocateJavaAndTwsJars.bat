@echo off
setlocal ENABLEDELAYEDEXPANSION

:: Note that this command file is a 'service file' intended to be called from 
:: higher level command files. There should be no reason for the end user to modify 
:: it in any way. So PLEASE DON'T CHANGE IT UNLESS YOU KNOW WHAT YOU'RE DOING!

if "%1"=="" goto :showUsage
if "%1"=="/?" goto :showUsage
if "%1"=="-?" goto :showUsage
if /I "%1"=="/HELP" goto :showUsage
goto :doIt

:showUsage
::===0=========1=========2=========3=========4=========5=========6=========7=========8
echo.
echo Determines the path to the Java installation to be used to run IBC, and sets
echo the path in the JAVA_PATH environment variable. It also sets the path to the 
echo TWS/Gateway jar files in the TWS_JARS environment variable.
echo.
echo Usage:
echo.
echo LocateJava twsVersion [/G ^| /Gateway] [/TwsPath:twsPath] [/JavaPath:javaPath]
echo.
echo   twsVersion              The major version number for TWS
echo.
echo   /G or /Gateway          Indicates that the IB Gateway is to be loaded rather
echo                           than TWS
echo.
echo   twsPath                 Path to the TWS installation folder. Defaults to
echo                           C:\Jts
echo.
echo   javaPath                Path to the folder containing the java.exe to be used
echo                           to run IBC. Defaults to the java.exe included
echo                           in the TWS installation; failing that, to the Oracle
echo                           Java installation
echo.
exit /B
::===0=========1=========2=========3=========4=========5=========6=========7=========8

:doIt

:: Some constants

set E_NO_JAVA=1001
set E_NO_TWS_VERSION=1002
set E_INVALID_ARG=1003
set E_TWS_VERSION_NOT_INSTALLED=1004

set ENTRY_POINT_TWS=ibcalpha.ibc.IbcTws
set ENTRY_POINT_GATEWAY=ibcalpha.ibc.IbcGateway

:: Variables to be derived from arguments
set TWS_VERSION=
set ENTRY_POINT=%ENTRY_POINT_TWS%
set TWS_PATH=C:\Jts
set JAVA_PATH=
set ERROR_MESSAGE=

::======================== Parse command line arguments =====================
set PHASE=Parsing command line arguments 

:parse

if "%~1" == "" goto :parsingComplete

set ARG=%~1
if /I "%ARG%" == "/G" (
	set ENTRY_POINT=%ENTRY_POINT_GATEWAY%
) else if /I "%ARG%" == "/GATEWAY" (
	set ENTRY_POINT=%ENTRY_POINT_GATEWAY%
) else if /I "%ARG:~0,9%" == "/TWSPATH:" (
	set TWS_PATH=%ARG:~9%
) else if /I "%ARG:~0,10%" == "/JAVAPATH:" (
	set JAVA_PATH=%ARG:~10%
	if "%JAVA_PATH%" == """" set JAVA_PATH=
) else if /I "%ARG:~0,1%" == "/" (
	set ERROR_MESSAGE=Invalid parameter '%ARG%'
	set ERROR=%E_INVALID_ARG%
) else if not defined TWS_VERSION (
	set TWS_VERSION=%ARG%
) else (
	set ERROR_MESSAGE=Invalid parameter '%ARG%'
	set ERROR=%E_INVALID_ARG%
)
		
shift
goto :parse
	
:parsingComplete

if defined ERROR goto :err

::======================== Determine the location of java.exe ===============

set PHASE=Determining the location of java.exe

if /I "%ENTRY_POINT%" == "%ENTRY_POINT_TWS%" (
	if exist "%TWS_PATH%\%TWS_VERSION%\jars" (
		set TWS_JARS=%TWS_PATH%\%TWS_VERSION%\jars
		set INSTALL4J=%TWS_PATH%\%TWS_VERSION%\.install4j
	) else (
		set TWS_JARS=%TWS_PATH%\ibgateway\%TWS_VERSION%\jars
		set INSTALL4J=%TWS_PATH%\ibgateway\%TWS_VERSION%\.install4j
	)
)
if /I "%ENTRY_POINT%" == "%ENTRY_POINT_GATEWAY%" (
	if exist "%TWS_PATH%\ibgateway\%TWS_VERSION%\jars" (
		set TWS_JARS=%TWS_PATH%\ibgateway\%TWS_VERSION%\jars
		set INSTALL4J=%TWS_PATH%\ibgateway\%TWS_VERSION%\.install4j
	) else (
		set TWS_JARS=%TWS_PATH%\%TWS_VERSION%\jars
		set INSTALL4J=%TWS_PATH%\%TWS_VERSION%\.install4j
	)
)

if not exist "%TWS_JARS%" (
	set ERROR_MESSAGE=TWS version %TWS_VERSION% is not installed
	set ERROR_MESSAGE1=You must install the offline version of TWS/Gateway
	set ERROR_MESSAGE2=IBC does not work with the auto-updating TWS/Gateway
	set ERROR=%E_TWS_VERSION_NOT_INSTALLED%
	goto :err
)

if not defined JAVA_PATH (
	if exist "%INSTALL4J%\pref_jre.cfg" (
		for /f "tokens=1 delims=" %%i in (%INSTALL4J%\pref_jre.cfg) do set JAVA_PATH=%%i\bin
		if not exist "!JAVA_PATH!\java.exe" set JAVA_PATH=
	)
)

if not defined JAVA_PATH (
	if exist "%INSTALL4J%\inst_jre.cfg" (
		for /f "tokens=1 delims=" %%i in (%INSTALL4J%\inst_jre.cfg) do set JAVA_PATH=%%i\bin
		if not exist "!JAVA_PATH!\java.exe" set JAVA_PATH=
	)
)

if not defined JAVA_PATH (
	if exist "%PROGRAMDATA%\Oracle\Java\javapath\java.exe" set JAVA_PATH="%PROGRAMDATA%\Oracle\Java\javapath"
)

if not defined JAVA_PATH (
	set ERROR_MESSAGE=Can't find suitable Java installation
	set ERROR=%E_NO_JAVA%
	goto :err
)

echo.

endlocal & set JAVA_PATH=%JAVA_PATH% & set TWS_JARS=%TWS_JARS%

exit /B 0

:err
echo.
echo =========================== An error has occurred =============================
echo.
echo Error: %ERROR_MESSAGE% 
if not "%ERROR_MESSAGE1%"=="" (
	echo        %ERROR_MESSAGE1%
)
if not "%ERROR_MESSAGE2%"=="" (
	echo        %ERROR_MESSAGE2%
)
endlocal & set ERROR_MESSAGE=%ERROR_MESSAGE% & set ERROR_MESSAGE1=%ERROR_MESSAGE1% & set ERROR_MESSAGE2=%ERROR_MESSAGE2%
set JAVA_PATH=
exit /B %ERROR%

