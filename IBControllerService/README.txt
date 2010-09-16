0.	CONTENTS
----------------

1.  Overview
2.  Download Contents
3.  Instructions
4.  Useful Tips
5.  Version History


1.	OVERVIEW
=====================
IBControllerService is a Windows Service that starts/stops IBController. 
It is a simple implementation, it simply launches the IBController batch
file. When the service is stopped it sends IBController a STOP then EXIT
command via TCPIP.

The service runs as a background process. IBController and TWS/Gateway
are not displayed on the screen, nor do they require a logged on user.
TWS can be accessed from the API client via the IP and Port configured
in TWS.

This service requires IBController to be previously installed and configured.
Make sure you can run IBController from a batch file before using this
service.

Licensed under the GPL v3. Contact: Shane Cusson, shane.cusson@vaultic.com


2.	DOWNLOAD CONTENTS
===================== 

The download file is a zip file containing the following files:

bin/
	IBControllerService.exe        - The Windows Service executable
	IBControllerService.exe.config - The config file for the service,
	                                 must be in the same folder as the
									 service.
src/
	*.*	- The source files for the project. The .sln file is a Visual
	      Studio 2010 project file that will build the service

COPYING.txt - Full text of the GPL License

README.txt - This file.

install.cmd - Batch file to install the service.

3.	INSTRUCTIONS
===================== 

1. Edit the config file
		Open "IBControllerService.exe.config" and set your IP address, Port,
		and the path to the IBController batch file. Make sure this batch
		file works before using the service.

2. Install the service
		Run the "install.cmd" file. Edit the path in this file if you've
		moved IBControllerService to a folder other than c:\IBControllerService

4.	USEFUL TIPS
===================== 
The service logs events to the Windows Event log. Check the event log for
error and status messages.

The service can be found in the "Services" snapin. It is named 
"IBController Service". It is set to log in a LocalSystem and start
automatically by default. These settings can be changed in the "Services"
snapin.


4.	VERSION HISTORY
===================== 

20100905 - Shane Cusson - Initial release.


eof