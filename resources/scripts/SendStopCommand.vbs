' allow time for telnet session to start and connect
WScript.sleep 200 

set OBJECT=WScript.CreateObject("WScript.Shell")

OBJECT.AppActivate WScript.Arguments(0)

' STOP IBC
OBJECT.SendKeys "STOP{ENTER}" 

' Do not send an EXIT command because IBC always
' closes the connection after receiving a STOP
' command
'OBJECT.SendKeys "EXIT{ENTER}" 

WScript.sleep 200 

' close telnet window
OBJECT.SendKeys "q{ENTER}" 

