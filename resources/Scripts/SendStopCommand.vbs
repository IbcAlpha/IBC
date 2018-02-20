' allow time for telnet session to start and connect
WScript.sleep 200 

set OBJECT=WScript.CreateObject("WScript.Shell")

' STOP IBC
OBJECT.SendKeys "STOP{ENTER}" 

WScript.sleep 50 

' Disconnect from IBC
OBJECT.SendKeys "EXIT{ENTER}" 

WScript.sleep 50 

' close telnet window
OBJECT.SendKeys "q{ENTER}" 

