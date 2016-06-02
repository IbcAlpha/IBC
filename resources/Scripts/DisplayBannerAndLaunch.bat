@echo off
setlocal enableextensions enabledelayedexpansion

:: get the IBController version
for /f "tokens=1 delims=" %%i in (%IBC_PATH%\version) do set IBC_VRSN=%%i

if defined LOG_PATH (
	if not exist "!LOG_PATH!" (
		mkdir "!LOG_PATH!"
	)
	
	set README=!LOG_PATH!\README.txt
	if not exist "!README!" (
		echo You can delete the files in this folder at any time > "!README!"
		echo.>> "!README!"
		echo Windows will inform you if a file is currently in use.>> "!README!"
	)

	call "%IBC_PATH%\Scripts\getDayOfWeek.bat"
	set LOG_FILE=!LOG_PATH!\ibc-%IBC_VRSN%_%APP%-%TWS_MAJOR_VRSN%_!DAYOFWEEK!.txt
	if exist "!LOG_FILE!" (
		for %%? in (!LOG_FILE!) do (
			set LOGFILETIME=%%~t?
		)
		call set "s=%DATE%%%FILETIME:*%DATE%=%%"
		if not "%s%" == "%FILETIME%" del "!LOG_FILE!"
	)
) else (
	set LOG_FILE=NUL
)

::   now launch IBController

color 0A
echo +==============================================================================
echo +
echo + IBController version %IBC_VRSN%
echo +
echo + Running %APP% %TWS_MAJOR_VRSN%
echo +
if defined LOG_PATH (
	echo + Diagnostic information is logged in:
	echo +
	echo + %LOG_FILE%
	echo +
)
echo +
echo + ** Caution: closing this window will close %APP% %TWS_MAJOR_VRSN% **
echo + (window will close automatically when you exit from %APP% %TWS_MAJOR_VRSN%)
echo +

set GW_FLAG=
if /I "%APP%" == "GATEWAY" set GW_FLAG=/G

call "%IBC_PATH%\Scripts\IBController.bat" "%TWS_MAJOR_VRSN%" %GW_FLAG% ^
     "/TwsPath:%TWS_PATH%" "/IbcPath:%IBC_PATH%" "/IbcIni:%IBC_INI%" ^
     "/User:%TWSUSERID%" "/PW:%TWSPASSWORD%" "/FIXUser:%FIXUSERID%" "/FIXPW:%FIXPASSWORD%" ^
     "/JavaPath:%JAVA_PATH%" "/Mode:%TRADING_MODE%" ^
     >> "%LOG_FILE%" 2>&1

if errorlevel 1 (
	color 0C
	echo +==============================================================================
	echo +
	echo +                       **** An error has occurred ****
	if defined LOG_PATH (
		echo +
		echo +                     Please look in the diagnostics file 
		echo +                   mentioned above for further information
	)
	echo +
	echo +==============================================================================
	echo +
	echo + Press any key to close this window
	pause > NUL
	echo +
) else (
	echo + %APP% %TWS_MAJOR_VRSN% has finished
	echo +
)

echo +==============================================================================
echo.

color
exit
