IBController is a Java application that was written to enable Interactive
Brokers Trader Workstation (TWS) to be run in "hands-free" mode.  This 
makes writing unattended automated trading systems possible. IBController
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

1. It starts up TWS or the IB Gateway.

2. It automatically logs into TWS/Gateway, using the username and
   password specified in the IBController ``.ini`` file.  The password is 
   lightly encrypted, which prevents it from easily being used by 
   humans who might see it in the ``.ini`` file.

3. It automatically clicks the YES button if the "Accept incoming 
   connection?" dialog is displayed.

4. It automatically clicks the Close button if the Tip of the Day 
   dialog appears.

5. It automatically dismisses the dialog that warns of a new TWS 
   version upon startup.

6. IBController provides a server that accepts commands for 
   controlling TWS/Gateway.  Currently, the following commands are
   implemented:
   * ``STOP``: causes TWS/Gateway to shut itself down cleanly.
   * ``ENABLEAPI``: ensures that the Enable ActiveX and Socket Clients 
     option is checked in the TWS Configure/API menu.

7. It automatically responds to the "Exit Session Setting" 
   dialog by setting the autologoff five minutes in the past.
   This enables TWS to be kept running indefinitely.

8. It can be configured to automatically shut down TWS at a
   specified day of the week and time.

9. The code can be easily modified to add new features. If you
   add any new features that you feel may be useful for other 	
   users, please email details to Richard King at
   rlking@aultan.com who will coordinate their incorporation
   into a new release.

Download Contents
=================

Download the ZIP file at the bottom of the
[latest release](https://github.com/ib-controller/ib-controller/releases/latest)
page. You can also access
[prior releases](https://github.com/ib-controller/ib-controller/releases) if
desired.

Select [GitHub Releases](https://github.com/ib-controller/ib-controller/releases)
to view the released versions. Select the newest displayed release and then

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

README.md - this file.

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

Instructions
============

These instructions should help you to get started with IBController. Where
relevant, things which are different between Windows and Unix are pointed out.

For Windows it is assumed that TWS is installed in C:\Jts and that you will
install IBController in C:\IBController.

For Unix it is assumed that TWS is installed in /opt/IBJts and that you 
will install IBController in /opt.  

Obviously you may need to change some of these instructions to take account of
where TWS and IBController are actually installed.

Installation
------------

Installing IBController is just a matter of unzipping the download 
file to wherever you want to install it.

Example for Unix users:

```
cd /opt
unzip /some/download/directory/IBControllerV2-6.zip
```

Encrypt your password
---------------------

(You don't need to do this step if you pass the login id
and password to IBController via the command line, or if you set
the ``PasswordEncrypted`` option to ``no``.)

To logon to TWS, IBController needs to know your password. The 
password can either be given to IBController on the command line,
or it can be stored in IBController's configuration file.
If it's stored in the configuration file, it can
be either 'in clear' or in an encrypted form that makes it more 
difficult for other users of your computer to discover it.

To encrypt your password run the following command from the 
IBController directory (applies to both Unix and Windows):
 
``java -cp IBController.jar ibcontroller.IBController encrypt <password>``

where ``<password>`` is your IB account password.

The program output will include your encrypted password, which 
can then be included in your configuration file.

Make a Configuration File
-------------------------

IBController must be supplied with a configuration file. A 
specimen file called IBController.ini is supplied. 

There are two ways that IBController can locate the filename. The
simplest way is to tell it where to find the file. If you do this,
you can give the configuration file any name you like. Otherwise
IBController will expect to find a file
called ``IBController.<username>.ini`` where ``<username>`` is your 
username on your computer (NOT your IB account username).

It is recommended that you use the explicit approach unless
there are multiple users of your computer.

Rename the specimen configuration file if appropriate.

Edit the configuration file and change the ``IbLoginID``, ``IbPassword`` and
``PasswordEncrypted`` settings as required. If ``PasswordEncrypted`` is set to
``yes`` or is omitted, the ``IbPassword`` setting must be the encrypted password
as described below.

Note that you do not need these settings if you intend to pass 
the login id and password to IBController on the command line: 
in this case it is best to comment out these settings by prefixing 
them with a ``#``.

Adjust the other settings as required, and save the file.	
	
Running IBController
--------------------

The normal way to start IBController is by use of a command file. On
Windows, this is a text file with either ``.bat`` or ``.cmd`` as the filename
extension.

You can use such a command file in a number of ways, such as:

* double click on it in Windows Explorer
* create a shortcut to it on your start menu
* create a scheduled task to run it automatically at whatever times
  you require

IMPORTANT!!
-----------

The IBController download zipfile contains sample command files for
Windows called:

``sampleIBControllerStart.bat``: Starts IB TWS
``sampleIBControllerGatewayStart.bat`` 	Starts IB Gateway

There are also corresponding sample files for Linux users, called:

``sampleIBControllerStart.sh``
``sampleIBControllerGatewayStart.sh``

You can use these samples as the basis for your own command file(s). 
However, they may not be entirely up to date so you should read through 
them carefully and make any necessary changes to ensure that they work 
properly. They contain detailed comments to help you specify the correct 
information.

I suggest you copy the sample command files first rather than edit them
directly so you still have the originals if you make a mistake.

The following information is for reference. If you use the 
sample command files as the basis for your command files you may
not need to read or understand it!
	
How to start IBController
-------------------------

IBController is a Java program. Once it starts running, it loads
TWS within its own process, and uses some low-level Java 'hooks' to
listen for various events relating to the windows created by TWS.

TWS and the IB Gateway use a number of third party jar files, and the 
Java VM needs to be told where to look for them. This is done using 
the classpath (``-cp``) argument to the java command. The jar files needed 
by TWS and the IB Gateway can be determined from the shortcuts created 
when TWS was installed (note that TWS needs more jar files than the
IB Gateway to support its richer functionality).

NB: in later TWS versions there is only a single jar file that replaces
the several files for earlier versions.

The command for running IBController has the following basic form:

``java -cp <classpath> <otherOptions> <entryPoint> [<config file>|NULL] [<loginId> <password>]``

where:
	
``<classpath>``
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

NB: for Windows 8 onwards, to locate the relevant shortcut,
right click on the tile for TWS/gateway on the Start screen and
select 'Open file location' on the menu bar.

Note that the classpath in the TWS shortcut assumes that the 
TWS files are in the same folder as the shortcut. If your
command file runs in a different folder (for example the folder
where the IBController files are stored) you will need to:
 
EITHER make sure each classpath item is prefixed with the path 
to the TWS files folder

OR your command file can change directory to the TWS files 
folder before issuing the java command (this is what 
the sampleIBControllerStart.bat file does)

``<otherOptions>``
these are items which modify the behaviour of the 
Java VM. Examples that are used by TWS include:
		
``-Dsun.java2d.noddraw=true -Xmx512M``

Any such options that appear in the command to start TWS should
also be included in the command to start IBController.
		
``<entryPoint>``
indicates whether to start TWS or the IB Gateway. Must be one
of the following values (note that these are case-sensitive):

``ibcontroller.IBController``: To start TWS
``ibcontroller.IBGatewayController``: To start IB Gateway

``<config file>``
is the path and name of the configuration file.
If this is not supplied or has the value NULL, then 
IBController will look for a file called 
IBController.<username>.ini in the working directory, 
where <username> is your username on your computer 
(NOT your IB account username).

``<loginId>``
is your TWS login id.

``<password>``
is your TWS password.

Note that if you don't supply ``<loginId>`` and ``<password>`` in the command
then they must be in the configuration file as described above.

For Windows users, please see the included specimen 
``sampleIBControllerStart.bat`` and ``sampleIBControllerGatewayStart.bat``
command files.

For Linux/MacOsX users, please see the included specimen 
``sampleIBControllerStart.sh`` and ``sampleIBControllerGatewayStart.sh``
shell scripts.

Building IBController
---------------------

This section is for those who wish to make modifications to 
BController. Other users can ignore it.

t is recommended that you use a proper Java IDE for any 
ignificant changes. If you don't already have one, NetBeans
see www.netbeans.org) and Eclipse (see www.eclipse.org) are
ully featured open source IDEs that have the advantage of
eing free. The details of creating a project and including 
the supplied IBController source files vary from IDE to IDE,
so you'll have to discover how to do this yourself or ask for
help on the forum. Bear in mind that you will need to include
a reference to IB's jts.jar file (which you'll find in your
TWS installation directory): otherwise your Java IDE will be
unable to compile the project.

For users who prefer to do their Java editing in a text
editing program, and build it from the command prompt, the 
following example commands are provided.

or Unix/Linux/Macos users:

```
$ cd /opt/IBController/src/ibcontroller
$ javac -d opt/IBController/classes -cp ~/IBJts/jts.jar *.java
```

To create the jar file:

```
cd /opt/IBController/classes	
jar cvf /opt/IBController.jar ibcontroller/*
```

For Windows users:

```
cd C:\IBController\src\ibcontroller
javac -d C:\IBController\classes -cp C:\Jts\jts.jar *.java    
```

To create the jar file:

```
cd C:\IBController\classes
jar cvf ..\IBController.jar ibcontroller\*
```

Note that the build.cmd file included in the download is 
a rather more sophisticated command file for building 
IBController on	Windows machines. You will need to edit 
it to meet your	requirements.

Useful Tips
===========

* When running java programs such as IBController and TWS on 32-bit
  systems, you may want to use the -server switch in the command line.
  This runs the server version of the Java VM, which has additional
  optimisations that make it run faster than the client version,
  though it takes longer to load and uses more memory. Note that on
  64-bit systems, the server version is the default.

* Running TWS Headless - *nix

[Not really an IBController tip, but someone may find it useful. The 
information presented here has not been tested and may need
to be corrected.]

Use VNC Server.  

Run your VNC server sending its display to :1

```
vncserver :1
export DISPLAY=:1
```

then use the normal command to start IBController.

You can then view the TWS UI from a remote machine using VNC Viewer.

* On Windows you can start IBController automatically using a Scheduled 
  Task. 

If you do this, you must make sure that the machine is already 
logged on before the scheduled task runs. Otherwise the task
will still run, but you won't be able to see and interact with TWS, 
even if you subsequently log on. 

Remember also to change the task settings to prevent Windows 
automatically ending it after a certain time.

Also you can use the ``IbAutoClosedown=no`` setting in the IBController
configuration file to disable TWS's autologoff feature,  and the 
``ClosedownAt=`` setting to specify when IBController will shut down
TWS.
