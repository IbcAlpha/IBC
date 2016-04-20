@echo off
::
:: Sets the  environment variable DAYOFWEEK to the English name
:: for the current day of the week
::

for /f %%a in ('wmic path win32_localtime get DAYOFWEEK /format:list ^| findstr "="') do (set %%a)

if "%DAYOFWEEK%" == "1" set DAYOFWEEK=MONDAY
if "%DAYOFWEEK%" == "2" set DAYOFWEEK=TUESDAY
if "%DAYOFWEEK%" == "3" set DAYOFWEEK=WEDNESDAY
if "%DAYOFWEEK%" == "4" set DAYOFWEEK=THURSDAY
if "%DAYOFWEEK%" == "5" set DAYOFWEEK=FRIDAY
if "%DAYOFWEEK%" == "6" set DAYOFWEEK=SATURDAY
if "%DAYOFWEEK%" == "7" set DAYOFWEEK=SUNDAY
