@echo off
::
:: Sets the  environment variable DAYOFWEEK to the English name
:: for the current day of the week
::

for /f %%a in ('powershell -NoProfile -Command "[int](Get-Date).DayOfWeek"') do set DAYOFWEEK=%%a

if "%DAYOFWEEK%" == "0" set DAYOFWEEK=SUNDAY
if "%DAYOFWEEK%" == "1" set DAYOFWEEK=MONDAY
if "%DAYOFWEEK%" == "2" set DAYOFWEEK=TUESDAY
if "%DAYOFWEEK%" == "3" set DAYOFWEEK=WEDNESDAY
if "%DAYOFWEEK%" == "4" set DAYOFWEEK=THURSDAY
if "%DAYOFWEEK%" == "5" set DAYOFWEEK=FRIDAY
if "%DAYOFWEEK%" == "6" set DAYOFWEEK=SATURDAY
