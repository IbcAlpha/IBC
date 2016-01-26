::   This command file starts the Interactive Brokers' Trader Workstation (TWS).

@echo off
setlocal enableextensions enabledelayedexpansion

::   It's likely that the only thing you need to change in this file is the version
::   number setting. 

::   You can find the TWS major version number by running TWS, then clicking 
::   Help > About Trader Workstation. In the displayed information you'll see a
::   line like this:
::
::    Build 954.2a, Oct 30, 2015 4:07:54 PM
::
::   Here the major version number is 954. Do not include the rest of the version number in
::   this setting:

set TWS_MAJOR_VRSN=952


::   If your TWS user id and password are not included in the IBController 
::   configuration file, set them here (do not encrypt the password). However
::   You are strongly advised not to set them here since this file is not 
::   normally in a protected location:

set TWSUSERID=
set TWSPASSWORD=


::   The folder where TWS is installed:

set TWS_PATH=C:\Jts


::   The folder containing the IBController files:

set IBC_PATH=C:\IBController


::   The location and filename of the IBController configuration file. This file should
::   be in a folder in your personal filestore, so that other users of your computer can't
::   access it. This folder and its contents should also be encrypted so that even users
::   with administrator privileges can't see the contents. Note that you can use the HOMEPATH
::   environment variable to address the root of your personal filestore (HOMEPATH is set
::   automatically by Windows):

set IBC_INI=%HOMEPATH%\Documents\IBController\IBController.ini

::   now launch IBController

call Scripts\IBController.bat %TWS_MAJOR_VRSN% "/TwsPath:%TWS_PATH%" "/IbcPath:%IBC_PATH%" "/IbcIni:%IBC_INI%" /User:%TWSUSERID% /PW:%TWSPASSWORD%

