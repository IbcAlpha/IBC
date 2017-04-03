' allow time for telnet session to start and connect
WScript.sleep 200 

set OBJECT=WScript.CreateObject("WScript.Shell")

' STOP IBController
OBJECT.SendKeys "STOP{ENTER}" 

WScript.sleep 50 

' Disconnect from IBController 
OBJECT.SendKeys "EXIT{ENTER}" 

WScript.sleep 50 

' close telnet window
OBJECT.SendKeys "q{ENTER}" 

