' allow time for telnet session to start and connect
WScript.sleep 200 

set OBJECT=WScript.CreateObject("WScript.Shell")

OBJECT.AppActivate WScript.Arguments(0)

' STOP IBC
OBJECT.SendKeys UCase(WScript.Arguments(1)) & "{ENTER}" 

' Send an EXIT command if required (note that STOP and
' RESTART command automatically close the telnet
' connection)

if WScript.Arguments.Count=3 then
	if UCASE(WScript.Arguments(2)) = "EXIT" then OBJECT.SendKeys "EXIT{ENTER}" 
end if

WScript.sleep 200 

' close telnet window
OBJECT.SendKeys "q{ENTER}" 

