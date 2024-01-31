' allow time for telnet session to start and connect
WScript.sleep 200 

set OBJECT=WScript.CreateObject("WScript.Shell")

OBJECT.AppActivate WScript.Arguments(0)

' Send command
OBJECT.SendKeys UCase(WScript.Arguments(1)) & "{ENTER}" 

' Send an EXIT command if required (note that for STOP and
' RESTART commands IBC automatically closes the telnet
' connection)

if WScript.Arguments(1) = "STOP" then
elseif WScript.Arguments(1) = "RESTART" then
else
	WScript.sleep 200 
	OBJECT.SendKeys "EXIT{ENTER}" 
end if

' terminate the telnet session
WScript.sleep 200 
OBJECT.SendKeys "q" 


