# IBController User Guide
-----------

> IMPORTANT
>
Please note that there are significant changes to the setup procedure for this 
release of IBController, compared with release 2.14.n.
>
Make sure you read the information in the **Scope of this User Guide** section.
>
IBController can be used to start TWS running the demo account. However 
there are many ways in which the demo account differs from a live or a paper
trading account, which may occasionally cause some inconvenience. In 
particular when you login to the demo account you are actually allocated 
a random account number, and when you log out this account may then be
allocated to another user. Next time you log in to the demo account, you are
unlikely to be allocated the same account as before, and even if you are given
the same one, any settings you made last time round will have changed.
IBController makes no attempt to avoid these situations: they are simply
an inevitable by-product of using the demo system, which is not intended
for any serious usage.
>
Note that in the remainder of this document, 'Unix' is used to refer to all
Unix-derived operating systems, including Linux and OS X.


## Introduction

### Overview of IBController
----------------------------

IBController enables Interactive Brokers' Trader Workstation (TWS) and Gateway to
be run in 'hands-free' mode, so that a user need not be present.  This makes
possible the deployment of unattended automated trading systems.  

IBController loads TWS or the Gateway and then 'listens' for various events (such 
as the display of dialogs) that would normally require user intervention. It can then
automatically take appropriate action on the user's behalf. For example, as well
as automating the TWS and Gateway login by filling the login dialog with your 
credentials, it can also deal with TWS's autologoff dialog so that it can keep TWS 
running continuously.

Here are some of the things IBController does for you:

- starts TWS or the Gateway

- logs you into TWS or Gateway

- clicks the YES button if the "Accept incoming connection?" dialog is displayed

- clicks the Close button if the Tip of the Day dialog appears

- dismisses the dialog that warns of a new TWS version upon startup

- responds to TWS's 'Exit Session Setting' to prevent autologoff, enabling
  TWS to be kept running indefinitely

- shuts down TWS tidily at a specified day of the week and time.

IBController also responds to certain commands sent to it by another program,
for example to tell TWS/Gateway to shut itself down cleanly.
	
### Scope of this User Guide
---------------------------

This User Guide is intended to help you get started with IBController. It applies
to IBController releases 3.2.0 and later. Note that these releases only apply to
versions of TWS from TWS 952 onwards: for earlier versions of TWS you should
use IBController release 2.14.0.

IBController release 3.2.0 and later has new script files (ie `.bat` and `.sh` files). 
These files are **very** different from those in release 2.14.0 and earlier, and also
substantially different from those in releases 3.0.0 and 3.0.1. So if you are 
updating an existing IBController installation, make sure that you extract the
new ones from the .zip file (including the ones in the Scripts subfolder) rather 
than trying to modify the old ones.

Note that the script files provided in IBController release 3.2.0 and later will 
**not** work with TWS 950 and earlier. 

Also, while it is technically possible to amend the script files from earlier 
versions of IBController to work with TWS 952 onwards, you are strongly 
advised not to do this, because the new scripts do more than the previous 
versions. Note that **priority support will not be provided for 'home-grown'** 
**scripts**: having said that, we are a friendly lot so we will help if you have 
problems with your own scripts, but not with the same sense of urgency.

### Acknowledgement
--------------------

This User Guide has been produced using the Markdown editor and PDF 
export tool at https://stackedit.io/editor.

## Getting Started

### Checklist
------------

Here is a summary of the steps you need to perform to get IBController
up and running properly.

1. Install the offline version of Interactive Brokers Trader Workstation 
   (see *Interactive Brokers*   *Trader Workstation* in the *Prerequisites* section), 
  and make sure that it uses the English language setting. **Please note**
  **that you MUST download the OFFLINE version of TWS, not the self-**
  **updating version: IBController DOES NOT WORK with the self-**
  **updating version of TWS.**

2. Download the IBController distribution ZIP file (see the *Where to get* 
   *IBController* section).

3. Install IBController (see the *Installing IBController* section). Please note
  that if you already have an existing IBController installation, it's wise to
  rename its folder before installing the new version in case you need to 
  revert to it later.

4. Create an encrypted folder called `IBController` in your personal
   filestore (see *Protecting the Password* in the *Password Security* section).

5. Copy the configuration file (called `IBController.ini`) from the    
   IBController installation folder to the encrypted folder created in
   step 5.

6. Check that the correct major version number for TWS is set in the shell 
   script files  in the IBController installation folder: these files are 
   `IBControllerStart.bat` and `IBControllerGatewayStart.bat`
   on Windows, `IBControllerStart.sh` and 
   `IBControllerGatewayStart.sh` on Unix. To find the TWS major 
   version number, first run TWS or the Gateway manually using the 
   IB-provided icon, then click `Help > About Trader Workstation` 
   or `Help > About IB Gateway`. In the displayed information you'll 
   see a  line similar to this:

    Build 954.2a, Oct 30, 2015 4:07:54 PM

   Here the major version number is 954. Ignore the rest of the version 
   number. Now open the script files with a text editor and ensure that
   the TWS_MAJOR_VRSN variable is set correctly.

7. At this stage, everything is set up to run IBController with its default
   settings, which will start TWS and log it into the IB demo user. It is
   worthwhile doing this just to check that everything works before 
   customising it to suit your needs. To do this, run the relevant
   shell script (`IBControllerStart.bat` on Windows, 
   `IBControllerStart.sh` on Unix) from the IBController 
   installation folder. If everything is satisfactory, shut down 
   IBController by closing TWS in the usual way. 
  
  Note that when you start IBController, information about the startup 
  process is logged to a file to aid diagnosing any faults that may 
  occurr. You will be notified of the log file name during the startup
  sequence.

8. Edit the configuration file (`IBController.ini`) in the encrypted 
   `IBController` folder using a text editor such as Notepad. See 
   *Configuring IBController* for further information.

9. If you did not install TWS and IBController in their default locations, 
   and store the configuration file in the recommended location, you will
   have to edit the shell scripts in the IBController installation folder
   accordingly. They contain comments that will help you do this correctly.

10. If you intend to run API programs to connect with TWS, you will need
    to manually edit the API settings in TWS's Global Configuration Dialog.

### Prerequisites
----------------

This section details the other software that is needed to run IBController.

Note that some Unix distributions may provide packages that can automatically
install and configure everything needed to run IBController, for example
the Arch Linux [ib-controller](https://aur.archlinux.org/packages/ib-controller/) package. This subject is beyond the scope of this
document, and you should refer to the relevant package documentation for 
guidance.

#### Java Runtime

Both IBController and TWS/Gateway are Java programs, and therefore the Java
Runtime needs to be accessible, but you don't have to do anything to ensure
this.

TWS version 950 and earlier required Java to be explicitly installed on the 
computer. However starting with TWS 952, the TWS installers for Windows and
Linux include a hidden version of Java which Interactive Brokers have used for 
developing and testing TWS. This version also runs IBController perfectly, and 
the IBController scripts ensure that it is used. 

This means that it is not necessary to ensure that Java is installed on your 
computer. It doesn't matter if it is already installed, but the IBController
scripts won't use it. However the scripts do make provision for declaring 
specifically which Java installation is to be used in exceptional situations 
where necessary.

If you had previously installed Java for use with earlier versions of IBController 
and TWS, but do not need it for any other programs, then you might want to
consider uninstalling it once you have finished setting up IBController.

**Note for OS X users**: the installer for the OS X version of TWS does not 
currently include the Java Runtime, so you will have to ensure that Java is 
installed.


#### Interactive Brokers Trader Workstation

Before running IBController, you will need to download and install the **offline** 
version of Trader Workstation from the [Interactive Brokers](http://www.interactivebrokers.com/) website. 

The location of the TWS dowloads page on IB's website varies from time to time,
and from country to country.  At the time of writing, on IB's US website (linked 
above) you need to click the `Trading` menu near the top of the page, then
select `TWS Software`: currently a valid direct link is [Tws Software](https://www.interactivebrokers.com/en/index.php?f=14099#tws-software).

IB provides two modes of operation for TWS:

- an online, or self-updating TWS that automatically receives updates as IB 
enhances it and fixes bugs. IBController **does not work** with the self-
updating TWS, so **do not install the self-updating version for use with**
**IBController**

- an offline or standalone TWS that, after download and installation, never
changes (until you download and install another version): you **must** 
download and install this offline version for use with IBController.

Note that the TWS installation includes the code for both TWS and the 
Gateway: there is no need to do another download for the Gateway. 

However, there are Gateway-specific downloads on IB's website. They contain
the same code as the TWS downloads, but they install in a different
place. You can install one of these, as well as or instead of the TWS installer. 
You can find these via the LOGIN dropdown in the title bar of IB's website.

When you run the script to load TWS, it will use the TWS installation if there
is one, and if not it will use the Gateway installation if there is one. Similarly 
when you run the script to load the Gateway, it will use the Gateway 
installation if there is one, and if not it will use the TWS installation if there 
is one. (Needless to say, if neither a TWS download nor a Gateway download
has been installed, the scripts will fail!)

It is safest to use the 'stable' offline version of TWS rather than the 'latest'
version for live trading: the latter is more likely to have bugs.

IBController needs TWS to operate in English so that it can recognise the 
various dialogues that it interacts with. You can set TWS's language by starting 
it manually (ie without using IBController) and selecting the language on the 
initial login dialog. TWS will remember this language setting when you 
subsequently start it using IBController.

Note that you do not need an IB account to try out IBController, as you can 
use the IB demo account (username `edemo`, password `demouser`).

### Where to get IBController
----------------------------

IBController is officially distributed as a ZIP file containing the compiled
program and some additional files, detailed below.

The ZIP file for the latest version should be downloaded from [Github](https://github.com/ib-controller/ib-controller/releases). 
Earlier versions can also be downloaded from the same place if need be.

The distribution ZIP file contains:

* [License](LICENSE.txt) text
* Compiled JAR (named similar to `IBController.jar`)
* Sample configuration file (named similar to `IBController.ini`)
* Sample TWS launch script for Windows (named similar to 
`IBControllerStart.bat`)
* Sample Gateway launch script for Windows (named similar to 
`IBControllerGatewayStart.bat`)
* Sample TWS launch script for Unix (named similar to 
`IBControllerStart.sh`)
* Sample Gateway launch script for Unix (named similar to 
`IBControllerGatewayStart.sh`)
* Sample TWS launch script for OS X (named similar to 
`IBControllerStart-OSX.sh`)
* Sample Gateway launch script for OS X (named similar to 
`IBControllerGatewayStart-OSX.sh`)
* Sample Windows Task Scheduler file (named similar to 
`Start TWS Live (daily).xml`)
* A Scripts sub-folder containing sub-scripts used by the top-level scripts 
mentioned above
* A text file called `version` containing the IBController version number

Source code and build scripts are not included in the distribution ZIPs, as they
are freely available from the [IBController project page](https://github.com/ib-controller/ib-controller) on Github.

### Installing IBController
---------------------------

Installing IBController is just a matter of extracting the contents of the
downloaded ZIP file to wherever you want to install it. You will make things
easiest for yourself if you use the locations described in 'Default Paths'
below, because that will minimise customising the configuration file and 
the shell scripts.

If you already have a previous IBController installation, it's wise to rename its
folder (eg to `IBController.old`) so that you can easily refer back to any 
customisations you did for that version.

#### On Windows: 

- create the folder where you want to install IBController, if it doesn't already 
  exist. As noted above (see Default Paths) this is normally `C:\IBController` 
  but it can be anywhere you like

- locate the downloaded ZIP file using File Explorer (Windows Explorer on
  Windows 7 and earlier). Windows treats ZIP files like an ordinary folder,
  so you can see its contents the same way as any other folder

- select all the files and folders and drag them into your installation folder

#### On Unix: 

- use a command similar to this:

```
sudo unzip ~/Downloads/IBControllerV3.2.0.zip -d \
/opt/IBController
```

- you'll need to make the script files executable, using a command similar to this:

```
cd /opt/IBController
chmod -R u+x *.sh
```

#### Default Paths

Several sample files ship in each IBController release. The sample files (and these 
instructions) assume the default paths shown in the table below (where 
``<username>`` represents your operating system user name, not your IB login id).

If you store any of these items in other locations, you will need to edit these 
sample files to reflect this. 

| Platform | Item                       | Path                                |
| -------- | -------------------------- | ------------------------------------|
| Windows  | IB TWS program files       | `C:\Jts`                            |
|          | IBController program files | `C:\IBController`                   |
|          | IBController.ini           | `%HOMEDRIVE%%HOMEPATH%\Documents\IBController` |
| Unix     | IB TWS program files       | `/home/<username>/Jts`              |
|          | IBController program files | `/opt/IBController`                 |
|          | IBController.ini           | `/home/<username>/IBController`     |
| MacOSX   | IB TWS program files       | `/home/<username>/Applications`     |
|          | IBController program files | `/opt/IBController`                 |
|          | IBController.ini           | `/home/<username>/IBController`     |


Note that installing IBController and/or TWS from a Unix package manager may
not use these paths. Consult your Linux package instructions for file locations.

### Password Security
--------------------

To login to TWS or IB Gateway, IBController needs to know your Interactive
Brokers username and password. You should very carefully secure your IB account
username and password to prevent unauthorised use by third parties. This section
gives you guidance on how to achieve this.

The username and password are given to IBController in one of two ways:

- via the configuration `.ini` file: this is the preferred method because the 
  configuration file can be protected by the operating system

- via the command line parameters when IBController is started: this method is 
  strongly deprecated because command line information associated with a 
  process is easily available outside the process (for example via Task 
  Manager on Windows)

#### Protecting the Configuration File

To protect this sensitive information, the configuration file needs to be stored
in a location where it will not be accessible to other users of the computer. The 
simplest way to achieve this is to store it within your personal filestore: 

- on Windows this is your `Documents` folder (which is normally actually located
  at `C:\Users\<username>\Documents`). Note that this folder may also be 
  addressed using environment variables like this:
  
  `%HOMEDRIVE%%HOMEPATH%\Documents`

- on Unix it is the `/home/<username>` directory. 

You are advised to place the file in its own `IBController` folder within this location.

You should also consider encrypting the folder containing the configuration 
file. This will prevent another user with administrator privileges gaining 
access to the contents: even if they (ab)use their administrator privileges to
give themselves access to the file, its contents will not be decrypted because
they are not the user that encrypted it.

To encrypt the folder on Windows:

- right click the folder and select `Properties`

- click the `Advanced` button on the `General` tab

- set the checkbox labelled `Encrypt contents to secure data`

- finally, click the `OK` buttons to apply the changes.

Encrypting a folder on Unix is more involved, and you should refer to the 
documentation for your distribution.

#### Encrypting the Password

When the password is included in the configuration file, IBController allows it
to be in an encrypted form so that casual observers cannot see the actual
password, for example while you are editing the configuration file. 

Note however that the encryption is very simple and easily reversed by 
anyone who knows the encryption technique, so it does not remove the need
to store the configuration file securely. It can also cause problems if your 
password has certain characters in certain positions (due to a deficiency in
the encryption algorithm): therefore it is advisable not to use the password 
encryption facility, provided you make sure your IBController configuration 
file is stored in a safe place and you don't leave it open in an editor while the
computer is unattended or overlooked.

To encrypt your password, run the following command from the IBController directory:
 
``java -cp IBController.jar ibcontroller.IBController encrypt <your-password>``

The program output will include your encrypted password, which can then be
included in your IBController configuration ``.ini`` file. 

You should also clear your shell history if you entered the above command. In most
cases this is achieved by pressing ALT+F7 (Windows users) or typing `history -c`
(Linux users), although you should check it worked by pressing the up arrow afterwards.

### Configuring IBController
---------------------------

IBController must be supplied with a configuration file. A specimen file called
IBController.ini is included in the distribution ZIP. You will need to edit this
file to include your IB username and password, and to ensure that IBController 
behaves in the way that best suits your needs. 

You should copy the supplied file from the IBController installation folder 
into the secure location described above before editing it, so that you have 
a clean copy to revert to if need be.

The sample `IBController.ini` file contains detailed comments on the 
meaning of each configuration property. Many of these have sensible defaults, 
or are only needed in special situations, so to help you get started quickly, here 
is a list of the settings that you are most likely to need to change:

| Setting            | Notes                                                         |
| ------------------ | ------------------------------------------------------------- |
| IbLoginID          | You must set this to your IB username                         |
| PasswordEncrypted  | You must set this to `yes` if you have encrypted your password|
| Password           | You must set this to your IB password (possibly encrypted)    |
| TradingMode        | For TWS 955 and later, you must set this to `paper` if you have supplied the username and password for your live account but actually want to use your paper account. Otherwise you can omit the setting entirely or set it to `live`       |
| IbDir              | You only need to set this if you want TWS to store its settings in a different folder from the one it's installed in                                 |
| AcceptIncomingConnectionAction | It is safest to set this to `reject` and to explicitly configure TWS to specify which IP addresses are allowed to connnect to the API                                                                                  |
| IbAutoClosedown    | Set this to `no` to prevent TWS's daily auto closedown        |
| ClosedownAt        | Set this if you want to keep TWS running all week             |
| ------------------ | ------------------------------------------------------------- |

There are two ways that IBController can locate your edited `IBController.ini` file. 

- the simplest way is to tell it where to find the file in the command that starts
  IBController. In this way, you can give the configuration file any name you 
  like. This is the recommended approach, and the supplied scripts follow this 
  approach. If you want change the filename from IBController.ini, or if you store 
  it somewhere other than the default location, you'll have to edit the start script
  to declare it's new name and location.

- if you do not specify a configuration file name, IBController will expect to
  find a file named `IBController.<username>.ini` in the current working
  directory. In this case, `<username>` is your username on your computer (not
  your IB account username). This method is deprecated, because it is likely to
  result in the `.ini` file being in an insecure location.

### Starting IBController
------------------------

The normal way to start IBController is by use of a shell script. These can be 
identified by the `.bat` (Windows) or `.sh` (Unix) extensions. Scripts 
to start TWS and Gateway are included in the distribution ZIP, and due to their  
complexity you are strongly advised to use them, rather than try to create your
own.

Windows users can execute a shell script in a number of ways, including:

* Double-click the filename in Windows Explorer
* Create a shortcut to it on your Start menu, desktop or taskbar
* Create a scheduled task to run it automatically at the required times (see 
  below for more information about using scheduled tasks)

If you used the default locations to install IBController and TWS, and to
store your IBController.ini file, you should not need to edit the shell 
scripts. If you do need to change them, they are commented to help you.

## Other Topics

### Scheduled Tasks (Windows)
----------------------------
On Windows you can start IBController automatically using a Scheduled Task. 

If you do this, you must make sure that the machine is already logged on before
the scheduled task runs. Otherwise the task will still run, but you won't be
able to see and interact with TWS, even if you subsequently log on. 

Remember also to change the task settings to prevent Windows automatically
ending it after a certain time.

Also you can use the `IbAutoClosedown=no` setting in the IBController
configuration file to disable TWS's autologoff feature,  and the `ClosedownAt=`
setting to specify when IBController will shut down TWS.

In this way you can start IBController automatically on Sunday evening or Monday
morning, keep it running all week and then close down tidily on Friday evening 
or Saturday morning.

The Windows Task Scheduler has many powerful features, and some of these can
be used to provide even better control. For example, you can run the task every
5 minutes during the week so that if TWS crashes, it will automatically be
restarted within 5 minutes. If you also set up your computer to log on
automatically when it starts (information about this is easily available on the
internet), this will also restart TWS after a power outage (but make sure you
understand the security implications of autologon to Windows).

A sample scheduled task is included in the IBController distribution ZIP,
called `Start TWS Live (daily).xml`. You can import this into your Task
Scheduler if you are running Windows 7 or later. After importing it, you will
need to enable it and change the user account it runs under. This task starts TWS
daily at 05:55, and assumes that TWS is set to auto-logoff at 05:52, so the
IBController configuration file must include `IbAutoClosedown=yes`.

### Multiple IBController Instances
----------------------------------

You may want to run more than one instance of TWS or the Gateway on the same 
computer, perhaps simultaneously. Here are some reasons you might want to do
this:

- you want to run both your live and paper-trading IB accounts. This is
  especially true if you want to get market data from both accounts, as IB
  will only allow this if both TWS instances are on the same computer 
  (unless you don't try to run them at the same time)

- you have multiple logins for your live IB account, and want to run
  TWS for both, perhaps at the same time

- you trade on behalf of others, perhaps your family, friends or clients,
  who each have their own accounts, but you want to run TWS instances for
  all these accounts on one powerful computer

- you want to trade in different regions at different times

- you want to test a new version of TWS in your paper trading account at
  the same time as using your live account in a previous version

When TWS runs, it stores a large number of settings in a folder structure (these
settings may also be stored in IB's servers, but this is not a useful option if
you want to use multiple TWS instances). By default, TWS stores this settings
folder structure in the TWS installation folder. For each username, it creates a
separate folder structure. Note however that there are some files that TWS
creates while running that are not separated by username in this way, and only
one instance of TWS can access them at a time. So you can run multiple TWS 
instances with no problem provided each instance is logged in to a different
username, AND you don't try to run them at the same time.

However, by using the `IbDir` setting in the IBController configuration file, 
you can tell TWS to store its settings whereever you like. So to have multiple 
IBController instances operating simultaneously, you need to create a separate
configuration file for each instance with a different setting for `IbDir`. Note 
that you do not need to copy the TWS .jar files themselves - you can load TWS 
from the same installation folder for each instance.

Because you now have different configuration files, you also need different
scripts to run each instance (or you could have a single script and pass the
configuration file details as a parameter).

As a concrete example, let's take the first scenario described above: you want to
run both your live and paper trading accounts. So:

- install TWS into the default location (`C:\Jts` on Windows)

- create two new folders `C:\JtsLive` and `C:\JtsPaper` to store the settings

- create two IBController configuration files called `IBControllerLive.ini` and
  `IBControllerPaper.ini`

- set the IbDir option in them to point to the relevant folder, ie
  `IbDir=C:\\JtsLive` and `IbDir=C:\\JtsPaper`, and set the `IbLoginId`
  and ``IbPassword`` to the live or paper account values as appropriate

- create two start scripts (by copying `IBControllerStart.bat`) called
  `IBControllerStartLive.bat` and `IBControllerStartPaper.bat`

- change the `set IBC_INI=...` line in each script file to refer to the 
  relevant configuration file

- now you can run the new scripts, and each will start a separate instance of
  TWS connected to a different account, with its settings stored in separate
  folders.

#### Using different TWS versions simultaneously

To use more than one version of TWS (for example for testing a new version 
with your paper-trading account while also using a previous version for
your live account), you just need to install the required versions in the normal 
way. Version 952 and later of TWS have installers that automatically place the 
relevant files in separate folders named according to the version number.

Then follow the advice in the previous section and ensure that each script
file has the correct value for the `TWS_MAJOR_VRSN` variable.

### Any Questions?
-----------------

If you need assistance with running IBController, or have any queries or 
suggestions for improvement, you should join the IBController User Group
at:

[https://groups.io/g/IBController](https://groups.io/g/ibcontroller).

If you're convinced you've found a bug in IBController, please report it
via either the IBController User Group or the GitHub Issue Tracker at:

[https://github.com/ib-controller/ib-controller/issues](https://github.com/ib-controller/ib-controller/issues).

Please provide as much evidence as you can, especially the versions of 
IBController and TWS/Gateway you're using and a full description of the 
incorrect behaviour you're seeing.

