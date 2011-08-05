0.	CONTENTS
----------------

1.  Overview
2.  Download Contents
3.  Instructions
4.  Useful Tips
5.  Change History


1.	OVERVIEW
================

IBController is a Java application that was written to enable Interactive
Brokers Trader Workstation (TWS) to be run in "hands-free" mode.  This 
makes writing unattended automated trading systems possible.  IBController
automates the TWS login by filling the login dialog with your login 
credentials. It also handles the dialog boxes that TWS presents
during programmatic trading activies.

IBController also provides similar functions to automate the use of the 
IB Gateway (but note that the FIX mode of the IB Gateway is not supported).

The original author was Steven Kearns (skearns23@yahoo.com), and it has since
been significantly enhanced and restructured by Richard King
(rlking@aultan.com).  It is based on original code by ken_geis@telocity.com.

IBController is licensed under the GNU General Public License Version 3. You
can see the terms of this license in the COPYING.txt file included in the
download.

Running the "main" of IBController provides the following services:

1.1	It starts up TWS or the IB Gateway.

1.2	It automatically logs into TWS/Gateway, using the username and 
	password specified in the IBController .ini file.  The password is 
	lightly encrypted, which prevents it from easily being used by 
	humans who might see it in the .ini file.

1.3	It automatically clicks the YES button if the "Accept incoming 
	connection?" dialog is displayed.

1.4	It automatically clicks the Close button if the Tip of the Day 
	dialog appears.

1.5	It automatically dismisses the dialog that warns of a new TWS 
	version upon startup.

1.6	IBController provides a server that accepts commands for 
	controlling TWS/Gateway.  Currently, the following commands are
	implemented:
	
	(a) STOP - causes TWS/Gateway to shut itself down cleanly.
	
	(b) ENABLEAPI - ensures that the Enable ActiveX and Socket Clients 
	option is checked in the TWS Configure/API menu.

1.7	It automatically responds to the "Exit Session Setting" 
	dialog by setting the autologoff five minutes in the past. 
	This enables TWS to be kept running indefinitely.

1.8	It can be configured to automatically shut down TWS at a
	specified day of the week and time.

1.9	The code can be easily modified to add new features. If you
	add any new features that you feel may be useful for other 	
	users, please email details to Richard King at
	rlking@aultan.com who will coordinate their incorporation
	into a new release.


2.	DOWNLOAD CONTENTS
=========================

The download file is a zip file containing the following files:

	build.cmd - a Windows command file for building IBController
		after modifying the source code. You will not need 
		this file unless you intend to make any modifications
		to IBController.

        COPYING.txt - the GNU General Public License Version 3

	IBController.ini - a specimen configuration file. You will 
		need to edit it to ensure it contains the settings 
		you need.

	IBController.jar - a Java Archive containing the compiled
		code for IBController.

	IBControllerEncrypt.bat - a specimen Windows command file
		that you must use to encrypt your IB account
		password prior to entering it in the configuration
		file. You will first need to edit it to replace
		the string aaaaa with your password.

	README.txt - this file.

	sampleIBControllerStart.bat - a specimen Windows command file
		for running IBController to start TWS. You may need to edit
		this file to take account of where your TWS is
		installed and your version of TWS.

	sampleIBControllerGatewayStart.bat - a specimen Windows command 
		file for running IBController, starting the IB Gateway
		rather than TWS. You may need to edit this file to 
		take account of where your TWS is installed and your 
		version of TWS.

	src\ibcontroller\*.*  - the source code files for IBController. 
		You will not need these files unless you intend to make 
		modifications to IBController.



3.	INSTRUCTIONS
====================

These instructions should help you to get started with IBController. Where
relevant, things which are different between Windows and Unix are pointed out.

For Windows it is assumed that TWS is installed in C:\Jts and that you will
install IBController in C:\IBController.

For Unix it is assumed that TWS is installed in /opt/IBJts and that you 
will install IBController in /opt.  

Obviously you may need to change some of these instructions to take account of
where TWS and IBController are actually installed.

3.1	Installation
--------------------

	Installing IBController is just a matter of unzipping the download 
	file to wherever you want to install it.

	Example for Unix users:

	$ cd /opt
	$ unzip /some/download/directory/IBControllerV2-6.zip

3.2	Encrypt your password
-----------------------------

	(You don't need to do this step if you pass the login id
	and password to IBController via the command line, or if you set
	the PasswordEncrypted option to 'no'.)

	To logon to TWS, IBController needs to know your password. The 
	password can either be given to IBController on the command line 
	(see 3.4.1), or it can be stored in IBController's configuration 
	file (see 3.3). If it's stored in the configuration file, it can
	be either 'in clear' or in an encrypted form that makes it more 
	difficult for other users of your computer to discover it.

	To encrypt your password run the following command from the 
	IBController directory (applies to both Unix and Windows):
 
 	    java -cp IBController.jar ibcontroller.IBController encrypt <password>

	where <password> is your IB account password.

	The program output will include your encrypted password, which 
	can then be included in your configuration file (see 3.3).

3.3	Make a Configuration File
---------------------------------

	IBController must be supplied with a configuration file. A 
	specimen file called IBController.ini is supplied. 

	There are two ways that IBController can locate the filename. The
	simplest way is to tell it where to find the file, as described in 
	3.4.1: if you do this, you can give the configuration file any
	name you like. Otherwise IBController will expect to find a file
	called IBController.<username>.ini where <username> is your 
	username on your computer (NOT your IB account username).

	It is recommended that you use the explicit approach unless
	there are multiple users of your computer.

	Rename the specimen configuration file if appropriate.

	Edit the configuration file and change the IbLoginID,  
	IbPassword and PasswordEncrypted settings as required. If 
	PasswordEncrypted is set to 'yes' or is omitted, the IbPassword 
	setting must be the encrypted password as described in 3.2. 
	Note that you do not need these settings if you intend to pass 
	the login id and password to IBController on the command line: 
	in this case it is best to comment out these settings by prefixing 
	them with a #.

	Adjust the other settings as required, and save the file.	
	
3.4	Running IBController
----------------------------

	The normal way to start IBController is by use of a command file. On
	Windows, this is a text file with either .bat or .cmd as the filename
	extension.

	You can use such a command file in a number of ways, such as:

	- double click on it in Windows Explorer

	- create a shortcut to it on your start menu

	- create a scheduled task to run it automatically at whatever times
	  you require

	IMPORTANT!!
	-----------

	The IBController download zipfile contains sample command files for
	Windows called:

		sampleIBControllerStart.bat 		(which starts TWS) and 

		sampleIBControllerGatewayStart.bat 	(which starts the IB Gateway)

	You can use these samples as the basis for your own command file(s). 
	However, they may not be entirely up to date so you should read through 
	them carefully and make any necessary changes to ensure that they work 
	properly. They contain detailed comments to help you specify the correct 
	information.

	I suggest you copy the samplecommand files first rather than edit them
	directly so you still have the originals if you make a mistake.

	Linux users should have no difficulty adapting the sample command
	files to the appropriate command syntax.

	The following information is for reference. If you use the 
	sample command files as the basis for your command files you may
	not need to read or understand it!
	

3.4.1	How to start IBController:

	IBController is a Java program. Once it starts running, it loads
	TWS within its own process, and uses some low-level Java 'hooks' to
	listen for various events relating to the windows created by TWS.

	TWS and the IB Gateway use a number of third party jar files, and the 
	Java VM needs to be told where to look for them. This is done using 
	the classpath (-cp) argument to the java command. The jar files needed 
	by TWS and the IB Gateway can be determined from the shortcuts created 
	when TWS was installed (note that TWS needs more jar files than the
	IB Gateway to support its richer functionality).

	The command for running IBController has the following basic form:

	java -cp <classpath> <otherOptions> <entryPoint> [<config file>|NULL] [<loginId> <password>]

	where
	
	<classpath>  
		tells the Java virtual machine where to look to find
		the class files that must be loaded in order to run both 
		IBController and TWS. You must ensure that all the information 
		from the corresponding item in the command to run TWS without 
		IBController is included,  PLUS the path and name of the 
		IBController.jar file. Note that this information may vary 
		in different releases of TWS.
		
		To find this information on Windows, locate the entry to start
		TWS in your start menu (usually Start > All Programs > Trader
		Workstation 4.0 > Trader Workstation 4.0). Right click on this
		entry and select Properties from the pop-up menu. The command
		is in the field labelled 'Target'. 

		Note that the classpath in the TWS shortcut assumes that the 
		TWS files are in the same folder as the shortcut. If your
		command file runs in a different folder (for example the folder
		where the IBController files are stored) you will need to:
 
		EITHER make sure each classpath item is prefixed with the path 
			to the TWS files folder

		OR your command file can change directory to the TWS files 
			folder before issuing the java command (this is what 
			the sampleIBControllerStart.bat file does)

	<otherOptions>  
		these are items which modify the behaviour of the 
		Java VM. Examples that are used by TWS include:
		
		-Dsun.java2d.noddraw=true 
		-Xmx512M

		Any such options that appear in the command to start TWS should
		also be included in the command to start IBController.
		
	<entryPoint> 
		indicates whether to start TWS or the IB Gateway. Must be one
		of the following values (note that these are case-sensitive):

		ibcontroller.IBController 		(to start TWS)
		ibcontroller.IBGatewayController 	(to start IB Gateway)

	<config file>  
		is the path and name of the configuration file.
		If this is not supplied or has the value NULL, then 
		IBController will look for a file called 
		IBController.<username>.ini in the working directory, 
		where <username> is your username on your computer 
		(NOT your IB account username).

	<loginId>  
		is your TWS login id.

	<password> 
		is your TWS password.

		Note that if you don't supply <loginId> and <password> in the command
		then they must be in the configuration file as described in 3.3.

	Unix example for TWS 865, using a configuration file called demo.ini:

	    $ cd opt/IBJts
	    $ java -server -cp jts.jar:hsqldb.jar:jcommon-1.0.12.jar:jhall.jar:\
            > other.jar:rss.jar:/opt/IBController.jar \
	    > ibcontroller.IBController /opt/IBController/demo.ini

	For Windows users, please see the included specimen 
	sampleIBControllerStart.bat and sampleIBControllerGatewayStart.bat 
	command files.


3.5	Building IBController
-----------------------------

	This section is for those who wish to make modifications to 
	IBController. Other users can ignore it.

	It is recommended that you use a proper Java IDE for any 
	significant changes. If you don't already have one, NetBeans
	(see www.netbeans.org) and Eclipse (see www.eclipse.org) are
	fully featured open source IDEs that have the advantage of
	being free. The details of creating a project and including 
	the supplied IBController source files vary from IDE to IDE,
	so you'll have to discover how to do this yourself or ask for
	help on the forum. Bear in mind that you will need to include
	a reference to IB's jts.jar file (which you'll find in your
	TWS installation directory): otherwise your Java IDE will be
	unable to compile the project.

	For users who prefer to do their Java editing in a text
	editing program, and build it from the command prompt, the 
	following example commands are provided.

	For Unix users:

	    $ cd /opt/IBController/src/ibcontroller
	    $ javac -d opt/IBController/classes -cp ~/IBJts/jts.jar *.java

	    --  To create the jar file --

	    $ cd /opt/IBController/classes	
	    $ jar cvf /opt/IBController.jar ibcontroller/*

	For Windows users:

	    cd C:\IBController\src\ibcontroller
	    javac -d C:\IBController\classes -cp C:\Jts\jts.jar *.java    

	    : To create the jar file

	    cd C:\IBController\classes
	    jar cvf ..\IBController.jar ibcontroller\*

	Note that the build.cmd file included in the download is 
	a rather more sophisticated command file for building 
	IBController on	Windows machines. You will need to edit 
	it to meet your	requirements.


4.	USEFUL TIPS
===================

4.1	When running java programs such as IBController and TWS on 32-bit
        systems, you may want to use the -server switch in the command line.
        This runs the server version of the Java VM, which has additional
        optimisations that make it run faster than the client version,
        though it takes longer to load and uses more memory. Note that on
        64-bit systems, the server version is the default.

4.2	Running TWS Headless - *nix

	[Not really an IBController tip, but someone may find it useful.]

	You need vncserver.  

	Run your VNC server sending its display to :1

	    $ vncserver :1
	    $ export DISPLAY=:1
	    $ java -server -cp jcommon-1.0.0.jar:jfreechart-1.0.0.jar:jhall.jar:\
	    > jts.jar:other.jar:rss.jar:/opt/IBController.jar \
	    > ibcontroller.IBController /opt/IBController/demo.ini

4.3	On Windows you can start IBController automatically using a Scheduled 
	Task. 

	If you do this, you must make sure that the machine is already 
	logged on before the scheduled task runs. Otherwise the task
	will still run, but you won't be able to see and interact with TWS, 
	even if you subsequently log on.

	Remember also to change the task settings to prevent Windows 
	automatically ending it after a certain time.

	Also you can use the IbAutoClosedown=no setting in the IBController
	configuration file to disable TWS's autologoff feature,  and the 
	ClosedownAt= setting to specify when IBController will shut down
	TWS.


5.	CHANGE HISTORY
======================

-------------- Version 2.9.0 (Released 23 June 2011 by Richard King) ------------

1. Changed some window handlers to reflect changes in titles etc in TWS 918.6.

2. Enhanced the ENABLEAPI command implementation to cater for the fact that
the Configure top-level menu was removed in TWS 909. API configuration can
now only be done via the Edit > Global Configuration... menu.

3. Added an AutoConfirmOrders option. If set to yes, then when orders are placed
using the BookTrader in TWS, the confirmation dialog is automatically handled,
thereby effectively restoring the one-click trading that was removed in TWS 906
(as a result of IB's dispute with Trading Technologies who claim a patent for
one-click trading off a price ladder). The default is 'no', requiring the user
to manually confirm each trade.

4. Fixed the NewerVersionDialogHandler: the text to be searched for (in current
 TWS versions) is contained in a JOptionPane, not a JLabel.


-------------- Version 2.8.4.1 (Released 13 September 2010 by Shane Cusson) ------------

1. Changed encoding of IBController.ini to ANSI. Unicode was causing issues for Linux
users. No changes to IBController source or .jar.


-------------- Version 2.8.4 (Released 2 September 2010 by Richard King) ------------

1. Modified the main window handler to recognise the new main window title
introduced in TWS 907.


-------------- Version 2.8.3 (Released 31 August 2010 by Richard King) ------------

1. Added handlers for the Password Notice and NSE Compliance dialogs displayed by
Indian versions of TWS. By default the Password Notice is not dismissed, but the
NSE Compliance notice is dismissed. This behaviour may be overridden using the
new DismissPasswordExpiryWarning and DismissNSEComplianceNotice options.


-------------- Version 2.8.2 (Released 28 July 2010 by Richard King) ------------

1. Addressed some threading and synchronization issues. Thanks to Brent Boyer
for pointing them out.

2. Commands to IBControllerServer now produce the following responses:
	OK info
 	ERROR info
	INFO info

	where 'info' is a text string.

OK and ERROR are final responses.

INFO is an intermediate response that may be sent to provide information about the
command's progess. INFO's may be suppressed using the SuppressInfoMessages option
(see item 4).

3. Added a CommandPrompt option. The specified string is output by the server when
the connection to IBControllerServer is first opened and after the completion
of each command. If no string is specified, no prompt is issued.

4. Added a SuppressInfoMessages option. If set to 'yes', only the final response from
a command to IBControllerServer is sent - any intermediate information messages
are suppressed. The default is 'yes'.


-------------- Version 2.8.1 (Released 21 May 2010 by Richard King) ------------

1. Modified the gateway login handler to cope with a problem experienced by some
users where the login attempt was ignored.


-------------- Version 2.8.0 (Released 21 Apr 2010 by Richard King) ------------

1. Support for the IB Gateway added. Only the IB API mode of the gateway is
supported, not the FIX mode.

2. Fixed a bug where the tidy shutdown time was not accurately observed.


-------------- Version 2.7.5 (Released 19 Apr 2010 by Richard King) ------------

1. The 'Newer Version' notification in TWS 903 was not caught by IBController,
due to it now being a JFrame rather than a JDialog as previously. Both versions
are now handled.

2. Improved login dialog handling to keep periodically clicking the Login button after
enabling it, until it becomes disabled. This overcomes the problem that the
necessary delay time in the original approach was unknown, and seemed to vary
from machine to machine and from time to time.


-------------- Version 2.7.4 (Released 31 Mar 2010 by Richard King) ------------

1. Fixed a bug where streams of error messages were written to System.err if an
IBControllerServer client disconnected by resetting the connection (as opposed to
using the Exit command).


-------------- Version 2.7.3 (Released 29 Mar 2010 by Richard King) ------------

1. Fixed a bug where the ENABLEAPI command was not handled correctly if issued
before TWS's main window was loaded.


-------------- Version 2.7.2 (Released 22 Mar 2010 by Richard King) ------------

1. In TWS 903, the title of TWS's 'Tip of the Day' dialog has changed. This new
version is now recognised by IBController.

2. In TWS903, TWS's Login dialog no longer enables the Login button when the
username and password are filled in by IBController. To work around this,
IBController uses a TimerTask with a short delay (currently 1 second) which
enables the Login button and then clicks it.


-------------- Version 2.7.1 (Released 4 Feb 2010 by Richard King) ------------

1. Fixed a bug where the Login dialog was handled every time it became the
active window, rather than just when it first opened.

2. Changed so that if username and/or password are not supplied in either the
commnd line args or the config file, then TWS prompts for them rather than
IBController shutting down with an error.


-------------- Version 2.7 (Released 2 Feb 2010 by Richard King) --------------

1. Added a PasswordEncypted option. If set to 'no', the password in the .ini file
   is treated as not encrypted. The default is 'yes'.

2. Added timestamps to system.out writes.

3. Now recognises the German Exit Session dialogue.

4. The code has been completely refactored to improve readability and
   maintainability. Some redundant code has been removed.

5. Added a MinimizeMainWindow option. Setting this to 'yes' causes the TWS main
   window to be minimised when TWS is started. The default is 'no'.

6. Added an AllowBlindTrading option. Setting this to 'yes' causes the warning
   message output by TWS to be dismissed (by clicking the 'Yes'button) when
   attempting to place an order for a contract for which the user has no market
   data subscription. The default is 'no'.

7. Added a StoreSettingsOnServer option. Setting this to 'yes' sets the
   corresponding option in the TWS login dialog, resulting in TWS settings being
   stored on IB's servers. The default is 'no'.

8. The WAITINIT command to IBController Server has been changed to ENABLEAPI.


-------------- Version 2.6 (Released 5 Mar 2007 by Richard King) --------------

1. Fixed a bug in the implementation of the WAITINIT command.

2. Fixed a bug in ConfigureApiTask.

3. Username and password can now be supplied as args[1] and args[2] on the command
   line rather than via the .ini file.

4. Removed the check for null title in 'newer version' dialogs because Linux TWSs
   sometimes display this dialog with a non-null title.

5. For the Linux versions of the 'newer version' dialog, click 'No' instead of 'Yes'.

6. Added private static final long serialVersionUID to ScriptProperties.java to
   avoid a warning given by some compilers.

7. Included IBControllerLoader program, which shows how to run IBController from
   a separate Java program. This code also works in .Net if compiled with J#.

-------------- Version 2.5 (Released 5 Feb 2007 by Richard King) --------------

1. In the Linux version of TWS 865.7, IB introduced a variant of the 'Newer version'
   dialog that contains 'Yes' and 'No' buttons rather than an 'OK' button.
   IBController has been amended to handle both variants.

2. Fixed a minor bug in the autologoff handling. Thanks to Parimal Patel for
   pointing out this bug.

-------------- Version 2.4 (Released 5 Apr 2006 by Richard King) --------------

1. In Version 8.57 of TWS, IB changed the login dialog title from "New Login" to
   "Login". IBController has been amended to recognise both versions.

2. A new setting has been added to the .ini file to request IBController to shut
   down TWS at a specified day of the week and time. This is useful if you're
   keeping TWS running all week and want to ensure an automatic tidy closedown
   when trading has ceased at the end of the week.
   The new setting is called ClosedownAt, and its value takes the form
   <dayOfWeek hh:mm>. For example:

   ClosedownAt=Friday 22:00

   will cause IBController to shut down TWS tidily on Fridays at 22:00

   If this setting is omitted or no value is supplied, IBController will continue
   running until it is stopped by other means.

   If the value given to this setting is invalid, IBController will treat it as if
   a value had not been supplied.

   Please note that no warning will be given of the impending shutdown. If
   anyone requires such a warning, an additional setting could be added in a
   future release.

3. Fixed an insignificant bug in the autologoff handling.

4. The compiled class files are no longer included in the download. Instead an
   IBController.jar file (jar = Java Archive) is included. Note that this will
   necessitate a change to existing command files for running IBController:
   instead of the class path including the directory containing the classfiles,
   it should contain the path and name of the jar file itself.


-------------- Version 2.3 (Released 3 Feb 2006 by Richard King) --------------

1. When TWS pops-up its dialog advising the user that a newer version of the
   application is available, IBController did not correctly detect the dialog
   because it was checking for the wrong string. This has been corrected.

2. In recent TWS versions (later than TWS 8.48) there has been a difference in the
   way TWS handles the Exit Session Setting dialog which it pops up 5 minutes before
   autologging off. In these newer version, If the user changes the autologoff time
   and closes the dialog, TWS pops it up again a few seconds later (for reasons best
   known to the IB developers!). This caused IB controller to reset the autologoff time
   to its original setting, because it worked by alternating the AM and PM option buttons.
   Then TWS would pop it up again, and IBController would again change the autologoff
   time. This sequence would continue, and the net result was that on some occasions
   TWS would shut down at the original autologoff time. To avoid this problem, the way
   this works has been changed: IBController now sets the autologoff time to 5 minutes
   before the current time.

-------------- Version 2.2 (Released 2 Aug 2004 by Steven Kearns) -------------

1. The ini file should now have the name IBController.<UserName>.ini, where
   <UserName> is replaced by your username on the local machine, with spaces
   replaced by underscores and all lowercase.
   For instance, on my machine the user name is
   "Steven Kearns" and so the ini file should be named IBController.steven_kearns.ini.

   For those of us that don't know our user name, IBController prints out the
   java properties when it starts, and there is a property called "user.name" which
   contains the user name.

   The benefit of this change is that you can maintain different ini files on different
   machines (which is necessary because often the paths are different on different
   machines).

   You can also pass the path to the ini file in as a command line argument when you
   start the application, and in this case you can name it anything.  Here is an
   example:
   "C:\javaLatest\jre\bin\java.exe" -server -cp  C:\\InteractiveBrokers\\JTS\\jts.jar;c:\\InteractiveBrokers\\JTS\\jcommon-0.9.0.jar;C:\InteractiveBrokers\JTS\jfreechart-0.9.15.jar;C:\\myprograms\\mystocksoftware\\IBController\\bin ibcontroller.IBController C:\\myprograms\\mystocksoftware\\IBController.ini

2. Used System.exit() to ensure that the application actually terminated when the TWS is closed.


-------------- Version 2.1 (Released 18 Mar 2004 due to Richard King)----------

1.  The IBController.ini file now has an entry named
    IbAutoClosedown. This governs whether IBController
    keeps TWS running continuously, or allows it to
    auto-closedown at its configured time.

    To allow auto closedown use:

    IbAutoClosedown=yes

    To keep TWS running continuously use:

    IbAutoClosedown=no

    If you omit the IbAutoClosedown setting from
    IBController.ini, the code assumes a value of no.

2.  If you connect an API program to TWS 818.5 (and possibly
    some earlier versions) after it has lost its connection
    to the IB server and recovered it, the "Accept incoming
    connection" dialog has a different title that IBController
    v2 doesn't recognise. IBController v2.1 recognises both
    formats.

3.  IBController.bat, the command file for starting IBController,
    has been amended to work with TWS 819.3 and later. A version
    that works with earlier versions of TWS is included as
    IBControllerStartForTWS818andBefore.bat
