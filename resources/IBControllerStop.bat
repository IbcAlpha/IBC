@echo off

:: Sends a STOP command to the specified instance of IBController to cause it to
:: initiate a tidy closedown of TWS or Gateway.


:: You may need to change this line. Set it to the name or IP address of the 
:: computer that is running IBController. Note that you can use the local loopback 
:: address (127.0.0.1) if IBController is running on the current machine.

set ServerAddress=127.0.0.1


:: You may need to change this line. Make sure its set to the value of the 
:: IbControllerPort setting in IBController.ini:

set IBControllerServerPort=7462


:: IMPORTANT NOTE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
:: ==============
::
:: The following uses the telnet utility, which is not enabled in
:: Windows 'out of the box'. To enable it, run the 'Programs and Features' applet in
:: Control Panel. Click the 'Turn Windows features on or off' link in the left hand
:: panel. Then locate the 'Telnet client' entry in the list of Windows features and
:: ensure the checkbox in the entry is ticked. Click 'OK' and Windows will 
:: enable it. This command file should then run successfully.


:: You shouldn't need to change anything below this line.
::==============================================================================


:: open a telnet window with a session to IBController
start telnet %ServerAddress% %IBControllerServerPort% 

:: send the required command to IBController 
cscript Scripts\SendIBControllerCommands.vbs


:: Acknowledgement: many thanks for the help contained in Mukul Goel's answer to this question at:
::    http://stackoverflow.com/questions/13197376/is-it-possible-to-use-a-batch-file-to-establish-a-telnet-session-send-a-command

