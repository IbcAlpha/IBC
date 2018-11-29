@echo off
setlocal enableextensions enabledelayedexpansion

if "%1"=="" goto :showUsage
if "%1"=="/?" goto :showUsage
if "%1"=="-?" goto :showUsage
if /I "%1"=="/HELP" goto :showUsage
goto :doIt

:showUsage
::===0=========1=========2=========3=========4=========5=========6=========7=========8
echo.
echo Runs the IbcLoader sample program, which loads IBC (and hence TWS or Gateway)
echo without using the usual start scripts.
echo.
echo Usage:
echo.
echo RunIbcLoader twsVersion [/G ^| /Gateway] [/TwsPath:twsPath] [/JavaPath:javaPath]
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

:: Variables to be derived from arguments
set TWS_VERSION=
set ENTRY_POINT=/G
set TWS_PATH=/TWSPATH:C:\Jts
set JPATH=
set ERROR_MESSAGE=

::======================== Parse command line arguments =====================
set PHASE=Parsing command line arguments 

:parse

if "%~1" == "" goto :parsingComplete

set ARG=%~1
if /I "%ARG%" == "/G" (
	set ENTRY_POINT=/G
) else if /I "%ARG%" == "/GATEWAY" (
	set ENTRY_POINT=/G
) else if /I "%ARG:~0,9%" == "/TWSPATH:" (
	set TWS_PATH=%ARG%
) else if /I "%ARG:~0,10%" == "/JAVAPATH:" (
	set JPATH=%ARG%
	if "%JPATH%" == """" set JPATH=
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

set JAVA_PATH=
call LocateJavaAndTwsJars.bat %TWS_VERSION% %TWS_PATH% %JPATH%
if errorlevel 1 goto :err

::pushd C:\Jts
"%JAVA_PATH%\javaw" -cp IbcLoader.jar;IBC.jar;%TWS_JARS%\* ibcloader.IbcLoader
::popd

exit /B 0

:err
echo.
echo =========================== An error has occurred =============================
echo.
echo Error: %ERROR_MESSAGE% 
endlocal
set JAVA_PATH=
exit /B %ERROR%

