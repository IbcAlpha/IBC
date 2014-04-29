::--------------------------------------------------
:: windows command file that builds the IBController code.
::
:: The actions performed are:
:: 1) define some environment variables used in this bat file
:: 2) gut the class directory; execution will terminate if an error is
:: detected (ideally, this step should abort if any non-class files are
:: present)
:: 3) compile all the Java source files; execution will terminate if
:: an error is detected
:: 4) Create the IBController.jar; execution will terminate if
:: an error is detected
::
:: Side effect: echo will be off when this bat file finishes.
::--------------------------------------------------
::
::
@echo off


::----------step 1): define environment variables
set promptOriginal=%prompt%
set promptNone=$a

set ibDir=<INSERT CORRECT PATH HERE, SUCH AS C:\Jts>
set ibControllerDir=<INSERT CORRECT PATH HERE, SUCH AS C:\IBController>

set javaDir=<INSERT CORRECT PATH HERE, SUCH AS c:\Program Files\Java\jdk1.5.0_03\bin>
set path=%path%;%javaDir%

set classPath=
set classPath=%classPath%;%ibDir%\jts.jar

set classDir=%ibControllerDir%\IBController\classes
set srcDir=%ibControllerDir%\src

set javacOutput=
::set javacOutput=-Xstdout javacOutput.txt

::set checks=-deprecation
::set checks=-deprecation  -Xswitchcheck
set checks=-Xlint:all
::set checks=-Xlint:all -Xlint:-unchecked
:: NOTE: which line to use above depends on your JDK version (first 2
for 1.4 and earlier, last 2 for 1.5+)

set jdkVersion=-source 1.4  -target 1.4
::set jdkVersion=-source 1.5  -target 1.5

set srcFiles=%srcDir%\ibcontroller\*.java 

set prompt=%promptNone%


::----------step 2): gut the class directory
echo.
rmdir /Q /S %classDir%
mkdir %classDir%
if errorlevel 1 goto handleError


::----------step 3): do compile
echo.
echo on
::
javac  -classpath %classPath%  -d %classDir%  %javacOutput%  %checks% %jdkVersion%  -sourcepath %srcDir%  %srcFiles%
@if errorlevel 1 goto handleError
::
@echo off


::----------step 4): make jar file
echo.
echo on
::
jar cvf %ibControllerDir%\IBController.jar %classDir%\* 
@if errorlevel 1 goto handleError
::
@echo off


::----------error handling and final actions:

:handleError
@echo off
if not errorlevel 1 goto finalActions
echo.
echo ERROR DETECTED: build.cmd will TERMINATE PREMATURELY

:finalActions
@echo off
set prompt=%promptOriginal%

pause