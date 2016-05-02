@echo off
:: Grateful thanks to Rob van der Woude for his wonderful scripting pages website at:
::
:: http://www.robvanderwoude.com/
::
:: It made the excruciating task of batch file authoring considerably less painful!
::

:: Note that this command file is a 'service file' intended to be called from 
:: higher level command files. There should be no reason for the end user to modify 
:: it in any way. So PLEASE DON'T CHANGE IT UNLESS YOU KNOW WHAT YOU'RE DOING!

setlocal enableextensions enabledelayedexpansion

if "%1"=="" goto :showUsage
if "%1"=="/?" goto :showUsage
if "%1"=="-?" goto :showUsage
if /I "%1"=="/HELP" goto :showUsage
goto :doIt

:showUsage
::===0=========1=========2=========3=========4=========5=========6=========7=========8
echo.
echo Runs IBController, thus loading TWS or the IB Gateway
echo.
echo Usage:
echo.
echo IBController twsVersion [/G ^| /Gateway] [/TwsPath:twsPath] [/IbcPath:ibcPath]
echo              [/IbcIni:ibcIni] [/JavaPath:javaPath]
echo              [/User:userId] [/PW:password]
echo              [/FIXUser:fixuserId] [/FIXPW:fixpassword]
echo              [/Mode:tradingMode]
echo.
echo   twsVersion              The major version number for TWS
echo.
echo   /G or /Gateway          Indicates that the IB Gateway is to be loaded rather
echo                           than TWS
echo.
echo   twsPath                 Path to the TWS installation folder. Defaults to
echo                           C:\Jts
echo.
echo   ibcPath                 Path to the IBController installation folder.
echo                           Defaults to C:\IBController
echo.
echo   ibcIni                  The location and filename of the IBController 
echo                           configuration file. Defaults to 
echo                           ^%%HOMEPATH^%%\Documents\IBController\IBController.ini
echo.
echo   javaPath                Path to the folder containing the java.exe to be used
echo                           to run IBController. Defaults to the java.exe included
echo                           in the TWS installation; failing that, to the Oracle
echo                           Java installation
echo.
echo   userId                  IB account user id
echo.
echo   password                IB account password
echo.
echo   fixuserId               FIX account user id (only if /G or /Gateway) 
echo.
echo   fixpassword             FIX account password (only if /G or /Gateway) 
echo.
echo   tradingMode             Indicates whether the live account or the paper 
echo                           trading account will be used. Allowed values are:
echo.
echo                               live
echo                               paper
echo.
echo                           These values are not case-sensitive.
echo.
exit /B
::===0=========1=========2=========3=========4=========5=========6=========7=========8

:doIt

:: Some constants

set E_NO_JAVA=1
set E_NO_TWS_VERSION=2
set E_INVALID_ARG=3
set E_TWS_VERSION_NOT_INSTALLED=4
set E_IBC_PATH_NOT_EXIST=5
set E_IBC_INI_NOT_EXIST=6
set E_TWS_VMOPTIONS_NOT_FOUND=7

set ENTRY_POINT_TWS=ibcontroller.IBController
set ENTRY_POINT_GATEWAY=ibcontroller.IBGatewayController

:: Variables to be derived from arguments
set TWS_VERSION=
set ENTRY_POINT=%ENTRY_POINT_TWS%
set TWS_PATH=
set IBC_PATH=
set IBC_INI=
set JAVA_PATH=
set IB_USER_ID=
set IB_PASSWORD=
set FIX_USER_ID=
set FIX_PASSWORD=
set IBC_CLASSPATH=
set ERROR_MESSAGE=

::======================== Parse command line arguments =====================
:parse

if "%~1" == "" goto :parsingComplete

set ARG=%~1
if /I "%ARG%" == "/G" (
	set ENTRY_POINT=%ENTRY_POINT_GATEWAY%
) else if /I "%ARG%" == "/GATEWAY" (
	set ENTRY_POINT=%ENTRY_POINT_GATEWAY%
) else if /I "%ARG:~0,9%" == "/TWSPATH:" (
	set TWS_PATH=%ARG:~9%
) else if /I "%ARG:~0,9%" == "/IBCPATH:" (
	set IBC_PATH=%ARG:~9%
) else if /I "%ARG:~0,8%" == "/IBCINI:" (
	set IBC_INI=%ARG:~8%
) else if /I "%ARG:~0,10%" == "/JAVAPATH:" (
	set JAVA_PATH=%ARG:~10%
	if "%JAVA_PATH%" == """" set JAVA_PATH=
) else if /I "%ARG:~0,6%" == "/USER:" (
	set IB_USER_ID=%ARG:~6%
) else if /I "%ARG:~0,4%" == "/PW:" (
	set IB_PASSWORD=%ARG:~4%
) else if /I "%ARG:~0,9%" == "/FIXUSER:" (
	set FIX_USER_ID=%ARG:~9%
) else if /I "%ARG:~0,7%" == "/FIXPW:" (
	set FIX_PASSWORD=%ARG:~7%
) else if /I "%ARG:~0,6%" == "/MODE:" (
	set MODE=%ARG:~6%
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

if defined IB_USER_ID set GOT_API_CREDENTIALS=1
if defined IB_PASSWORD set GOT_API_CREDENTIALS=1
if defined FIX_USER_ID set GOT_FIX_CREDENTIALS=1
if defined FIX_PASSWORD set GOT_FIX_CREDENTIALS=1

if defined GOT_FIX_CREDENTIALS (
	if not "%ENTRY_POINT%" == "%ENTRY_POINT_GATEWAY%" (
		set ERROR_MESSAGE=FIX user id and FIX password are only valid for the Gateway
		set ERROR=%E_INVALID_ARG%
	)
)

if defined MODE (
	if /I "%MODE%" == "LIVE" (
		echo. > NUL
	) else if /I "%MODE%" == "PAPER" (
		echo. > NUL
	) else (
		set ERROR_MESSAGE=Trading mode set to %MODE% but must be either 'live' or 'paper'
		set ERROR=%E_INVALID_ARG%
	)
)

if defined ERROR goto :err

echo.
echo ================================================================================
echo.
echo Starting IBController version %IBC_VRSN% on %DATE% at %TIME%
echo.
for /f "usebackq tokens=* skip=1" %%a in (`wmic OS get Caption^,Version^,OSArchitecture ^| findstr "." `) do echo Operating system:  %%a
echo.

:: log the arguments

echo Arguments:
echo.
echo TWS version = %TWS_VERSION%
echo Entry point = %ENTRY_POINT%
echo /TwsPath = %TWS_PATH%
echo /IbcPath = %IBC_PATH%
echo /IbcIni = %IBC_INI%
echo /Mode = %MODE%
echo /JavaPath = %JAVA_PATH%

if defined GOT_API_CREDENTIALS (
	echo /User = ***
	echo /PW = ***
) else (
	echo /User =
	echo /PW =
)
if defined GOT_FIX_CREDENTIALS (
	echo /FIXUser = ***
	echo /FIXPW = ***
) else (
	echo /FIXUser =
	echo /FIXPW =
)
echo.

::======================== Check everything ready to proceed ================

if not defined TWS_VERSION (
	set ERROR_MESSAGE=TWS major version number has not been supplied
	set ERROR=%E_NO_TWS_VERSION%
	goto :err
)

if not defined TWS_PATH set TWS_PATH=C:\Jts
if not defined IBC_PATH set IBC_PATH=C:\IBController
if not defined IBC_INI set IBC_INI=%HOMEPATH%\Documents\IBController\IBController.ini

:: In the following we try to use the correct .vmoptions file for the chosen entrypoint
:: Note that uninstalling TWS or Gateway leaves the relevant .vmoption file in place, so
:: we can still use the correct one.
if /I "%ENTRY_POINT%" == "%ENTRY_POINT_TWS%" (
	if exist "%TWS_PATH%\%TWS_VERSION%\tws.vmoptions" (
		set TWS_VMOPTS=%TWS_PATH%\%TWS_VERSION%\tws.vmoptions 
	) else if exist "%TWS_PATH%\ibgateway\%TWS_VERSION%\ibgateway.vmoptions" (
		set TWS_VMOPTS=%TWS_PATH%\ibgateway\%TWS_VERSION%\ibgateway.vmoptions 
	) 

	if exist "%TWS_PATH%\%TWS_VERSION%\jars" (
		set TWS_JARS=%TWS_PATH%\%TWS_VERSION%\jars
		set INSTALL4J=%TWS_PATH%\%TWS_VERSION%\.install4j
	) else (
		set TWS_JARS=%TWS_PATH%\ibgateway\%TWS_VERSION%\jars
		set INSTALL4J=%TWS_PATH%\ibgateway\%TWS_VERSION%\.install4j
	)
)
if /I "%ENTRY_POINT%" == "%ENTRY_POINT_GATEWAY%" (
	if exist "%TWS_PATH%\ibgateway\%TWS_VERSION%\ibgateway.vmoptions" (
		set TWS_VMOPTS=%TWS_PATH%\ibgateway\%TWS_VERSION%\ibgateway.vmoptions 
	) else if exist "%TWS_PATH%\%TWS_VERSION%\tws.vmoptions" (
		set TWS_VMOPTS=%TWS_PATH%\%TWS_VERSION%\tws.vmoptions 
	) 

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
	set ERROR=%E_TWS_VERSION_NOT_INSTALLED%
	goto :err
)
if not exist "%IBC_PATH%" (
	set ERROR_MESSAGE=IBController path: %IBC-PATH% does not exist
	set ERROR=%E_IBC_PATH_NOT_EXIST%
	goto :err
)
if not defined IBC_INI (
	set IBC_INI=NULL
) else if not exist "%IBC_INI%" (
	set ERROR_MESSAGE=IBController configuration file: %IBC-INI%  does not exist
	set ERROR=%E_IBC_INI_NOT_EXIST%
	goto :err
)
if not exist "%TWS_VMOPTS%" (  
	set ERROR_MESSAGE=%TWS_VMOPTS% does not exist  
	set ERROR=%E_TWS_VMOPTIONS_NOT_FOUND%
	goto :err
)
if defined JAVA_PATH (
	if not exist "%JAVA_PATH%\java.exe" (  
		set ERROR_MESSAGE=%JAVA_PATH% does not contain the Java runtime executable  
		set ERROR=%E_NO_JAVA%
		goto :err
	)
)


echo =================================
echo.

::======================== Generate the classpath ===========================

echo Generating the classpath

for %%i in (%TWS_JARS%\*.jar) do (
    if not "!IBC_CLASSPATH!"=="" set IBC_CLASSPATH=!IBC_CLASSPATH!;
    set IBC_CLASSPATH=!IBC_CLASSPATH!%%i
)
set IBC_CLASSPATH=%IBC_CLASSPATH%;%IBC_PATH%\IBController.jar
echo Classpath=%IBC_CLASSPATH%
echo.

::======================== Generate the JAVA VM options =====================

echo Generating the JAVA VM options

for /f "tokens=1 delims= " %%i in (%TWS_VMOPTS%) do (
	set TOKEN=%%i
	if not "!TOKEN!"=="" (
		if not "!TOKEN:~0,1!"=="#" set JAVA_VM_OPTIONS=!JAVA_VM_OPTIONS! %%i
	)
)
echo Java VM Options=%JAVA_VM_OPTIONS%
echo.

::======================== Determine the location of java.exe ===============

echo Determining the location of java.exe 

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

echo Location of java.exe=%JAVA_PATH%
echo.

::======================== Start IBController ===============================

if defined GOT_FIX_CREDENTIALS (
	if defined GOT_API_CREDENTIALS (
		set HIDDEN_CREDENTIALS="***" "***" "***" "***"
	) else (
		set HIDDEN_CREDENTIALS="***" "***"
	)
) else if defined GOT_API_CREDENTIALS (
	set HIDDEN_CREDENTIALS="***" "***"
)
	

if "%ENTRY_POINT%"=="%ENTRY_POINT_TWS%" (
	set PROGRAM=IBController
) else (
	set PROGRAM=IBGateway
)
echo Starting %PROGRAM% with this command:
echo "%JAVA_PATH%\java.exe" -cp  "%IBC_CLASSPATH%" %JAVA_VM_OPTIONS% %ENTRY_POINT% "%IBC_INI%" %HIDDEN_CREDENTIALS% %MODE%
echo.

:: prevent other Java tools interfering with IBController
set JAVA_TOOL_OPTIONS=

pushd %TWS_PATH%

if defined GOT_FIX_CREDENTIALS (
	if defined GOT_API_CREDENTIALS (
		"%JAVA_PATH%\java.exe" -cp  "%IBC_CLASSPATH%" %JAVA_VM_OPTIONS% %ENTRY_POINT% "%IBC_INI%" "%FIX_USER_ID%" "%FIX_PASSWORD%" "%IB_USER_ID%" "%IB_PASSWORD%" %MODE%
	) else (
		"%JAVA_PATH%\java.exe" -cp  "%IBC_CLASSPATH%" %JAVA_VM_OPTIONS% %ENTRY_POINT% "%IBC_INI%" "%FIX_USER_ID%" "%FIX_PASSWORD%" %MODE%
	)
) else if defined GOT_API_CREDENTIALS (
		"%JAVA_PATH%\java.exe" -cp  "%IBC_CLASSPATH%" %JAVA_VM_OPTIONS% %ENTRY_POINT% "%IBC_INI%" "%IB_USER_ID%" "%IB_PASSWORD%" %MODE%
) else (
		"%JAVA_PATH%\java.exe" -cp  "%IBC_CLASSPATH%" %JAVA_VM_OPTIONS% %ENTRY_POINT% "%IBC_INI%" %MODE%
)

popd

echo %PROGRAM% finished
echo.

exit /B %ERRORLEVEL%

:err
echo.
echo =========================== An error has occurred =============================
echo.
echo.
echo.
echo Error: %ERROR_MESSAGE% 
exit /B %ERROR%

