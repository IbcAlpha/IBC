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
set IBC_CLASSPATH=

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
) else if /I "%ARG:~0,6%" == "/USER:" (
	set IB_USER_ID=%ARG:~6%
) else if /I "%ARG:~0,4%" == "/PW:" (
	set IB_PASSWORD=%ARG:~4%
) else if /I "%ARG:~0,1%" == "/" (
	echo Invalid parameter '%ARG%'
	set ERROR=%E_INVALID_ARG%
) else if not defined TWS_VERSION (
	set TWS_VERSION=%ARG%
) else (
	echo Invalid parameter '%ARG%'
	set ERROR=%E_INVALID_ARG%
)
		
shift
goto :parse
	
:parsingComplete
if defined ERROR goto :err

::======================== Check everything ready to proceed ================

if not defined TWS_VERSION (
	echo TWS major version number has not been supplied - it must be the first argument
	set ERROR=%E_NO_TWS_VERSION%
	goto :err
)

if not defined TWS_PATH set TWS_PATH=C:\Jts
if not defined IBC_PATH set IBC_PATH=C:\IBController
if not defined IBC_INI set IBC_INI=%HOMEPATH%\Documents\IBController\IBController.ini


set TWS_JARS=%TWS_PATH%\%TWS_VERSION%\jars

if not exist "%TWS_JARS%" (
	echo TWS version %TWS_VERSION% is not installed
	set ERROR=%E_TWS_VERSION_NOT_INSTALLED%
	goto :err
)
if not exist "%IBC_PATH%" (
	echo IBController path: %IBC-PATH% does not exist
	set ERROR=%E_IBC_PATH_NOT_EXIST%
	goto :err
)
if not exist "%IBC_INI%" (
	echo IBController configuration file: %IBC-INI%  does not exist
	set ERROR=%E_IBC_INI_NOT_EXIST%
	goto :err
)
if not exist "%TWS_PATH%\%TWS_VERSION%\tws.vmoptions" (
	echo %TWS_PATH%\%TWS_VERSION%\tws.vmoptions does not exist
	set ERROR=%E_TWS_VMOPTIONS_NOT_FOUND%
	goto :err
)


echo =================================

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

for /f "tokens=1 delims= " %%i in (%TWS_PATH%\%TWS_VERSION%\tws.vmoptions) do (
	set TOKEN=%%i
	if not "!TOKEN:~0,1!"=="#" set JAVA_VM_OPTIONS=!JAVA_VM_OPTIONS! %%i
)
echo Java VM Options=%JAVA_VM_OPTIONS%
echo.

::======================== Determine the location of java.exe ===============

echo Determining the location of java.exe 

set JAVA_HOME=

if exist "%TWS_PATH%\%TWS_VERSION%\.install4j\pref_jre.cfg" (
	for /f "tokens=1 delims=" %%i in (%TWS_PATH%\%TWS_VERSION%\.install4j\pref_jre.cfg) do set JAVA_HOME=%%i\bin
	if not exist "!JAVA_HOME!\java.exe" set JAVA_HOME=
)

if not defined JAVA_HOME (
	if exist "%TWS_PATH%\%TWS_VERSION%\.install4j\inst_jre.cfg" (
		for /f "tokens=1 delims=" %%i in (%TWS_PATH%\%TWS_VERSION%\.install4j\inst_jre.cfg) do set JAVA_HOME=%%i\bin
		if not exist "!JAVA_HOME!\java.exe" set JAVA_HOME=
	)
)

if not defined JAVA_HOME (
	if exist "%PROGRAMDATA%\Oracle\Java\javapath\java.exe" set JAVA_HOME="%PROGRAMDATA%\Oracle\Java\javapath"
)

if not defined JAVA_HOME (
	echo Can't find suitable Java installation
	set ERROR=%E_NO_JAVA%
	goto :err
)

echo Location of java.exe=%JAVA_HOME%
echo.

::======================== Start IBController ===============================

:: prevent other Java tools interfering with IBController
set JAVA_TOOL_OPTIONS=

pushd %TWS_PATH%

if "%ENTRY_POINT%"=="%ENTRY_POINT_TWS%" (
	echo Starting IBController with this command:
) else (
	echo Starting IBGateway with this command:
)
echo %JAVA_HOME%\java.exe -cp  %IBC_CLASSPATH% %JAVA_VM_OPTIONS% %ENTRY_POINT% "%IBC_INI%" "%IB_USER_ID%" "%IB_PASSWORD%"
echo.
%JAVA_HOME%\java.exe -cp  %IBC_CLASSPATH% %JAVA_VM_OPTIONS% %ENTRY_POINT% "%IBC_INI%" %IB_USER_ID% %IB_PASSWORD%

popd

exit /B 0

:err
exit /B %ERROR%

