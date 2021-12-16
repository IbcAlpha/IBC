# **IBC USER GUIDE**

>IMPORTANT NOTES REGARDING TWS 974 and Gateway 975 and later versions
>
>In TWS 974, IBKR have changed the way the autologoff function works within
TWS. Starting with that version, when the time approaches the configured
autologoff time, logoff can be deferred once by changing the autologoff time in
the 'Exit Session Setting' dialog as in earlier versions, but when the new
autologoff time arrives, TWS will logoff even if the user (or IBC) changes the
autologoff time again.
>
>This defeats the mechanism that IBC used with earlier TWS versions to prevent
autologoff, by changing the configured autologoff each time the 'Exit Session
Setting' dialog was displayed. Because of this, you should no longer use the
`IbAutoClosedown=no` setting because it won't work properly.
>
>Furthermore, in Gateway 975 IBKR have introduced the same autologoff
functionality as TWS: ie the Gateway will no longer run continuously, and will
insist on shutting down every day.
>
>Instead you have two options:
>
>1. Restart IBC afresh each day you want to run TWS or Gateway. This option is
useful if you want to automate login to TWS. You can use Task Scheduler (on
Windows) or crontab (on Linux) to automatically start IBC at the appropriate
time. You'll find sections on using Task Scheduler or crontab to start IBC
towards the end of this document.
>
>2. Abandon the use of IBC and instead use the autorestart mechanism provided
by TWS 974, Gateway 975 and later versions. To use this, you have to start TWS
or Gateway with the .exe files or scripts provided by IBKR, because IBC cannot
work with this mechanism. On Windows, these files are `C:\Jts\nnn\tws.exe` for
TWS, and `C:\Jts\ibgateway\nnn\ibgateway.exe` for Gateway (note that desktop
icons are also provided).  On Linux, the files are `~/Jts/nnn/tws` for TWS, and
`~/Jts/ibgateway/nnn/ibgateway` for Gateway. nnn is the TWS version number.
>
>   Note that as mentioned above, IBC cannot currently work with this
auto-restart mechanism. The setting for it may appear in the relevant
TWS/Gateway configuration dialogs, but if you use it then TWS/Gateway will
actually attempt to relogin at the appropriate time but may not succeed - and
in any case the restarted process will not include IBC so no IBC functionality
will work.

>IMPORTANT
>
>If you have previously been using IBController and are switching over to IBC,
there are some differences that you need to be aware of: there is more
information about this in the **Changes from IBController** section at the end
of this document.
>
>Make sure you read the information in the **Scope of this User Guide** section.
>
>Note that in the remainder of this document, 'Unix' is used to refer to all
Unix-derived operating systems, including Linux and macOS.


## Introduction

### Overview of IBC

IBC enables Interactive Brokers' Trader Workstation (TWS) and Gateway to
be run in 'hands-free' mode, so that a user need not be present.  This makes
possible the deployment of unattended automated trading systems.

IBC loads TWS or the Gateway and then 'listens' for various events (such
as the display of dialogs) that would normally require user intervention. It
can then automatically take appropriate action on the user's behalf. For
example, it automates the TWS and Gateway login by filling the login
dialog with your credentials and 'clicking' the login button.

Here are some of the things IBC does for you:

- starts TWS or the Gateway

- logs you into TWS or Gateway

- clicks the YES button if the "Accept incoming connection?" dialog is
  displayed

- responds to TWS's 'Exit Session Setting' to prevent autologoff, enabling
  TWS to be kept running indefinitely. **IMPORTANT** This setting no longer
  works properly with TWS 974 or later

- shuts down TWS or Gateway tidily at a specified day of the week and time, or
  at a specified time every day.

IBC also responds to certain commands sent to it by another program,
for example to tell TWS/Gateway to shut itself down cleanly.
	
### Scope of this User Guide

This User Guide is intended to help you get started with IBC. It does not cover
every feature in depth.

Note that the configuration file `config.ini` that governs IBC's behaviour
contains extensive notes that provide more information on the various settings.

### Acknowledgement

This User Guide has been produced using the Pandoc document conversion
system to produce the PDF from the markdown source.


## Getting Started

### Checklist

Here is a summary of the steps you need to perform to get IBC
up and running properly.

1. Install the offline version of Interactive Brokers Trader Workstation
   (see *Interactive Brokers*   *Trader Workstation* in the *Prerequisites*
   section), and make sure that it uses the English language setting.

   **Please note that you MUST download the OFFLINE version of TWS, not the**
   **self-updating version: IBC DOES NOT WORK with the self-updating version**
   **of TWS.**

2. Download the appropriate IBC distribution ZIP file for your operating
   system (see the *Where to get IBC* section).

3. Install IBC (see the *Installing IBC* section). Please note
   that if you already have an existing IBC installation, it's wise to
   rename its folder before installing the new version in case you need to
   revert to it later.

4. Create an encrypted folder called `ibc` in your personal
   filestore (see *Protecting the Password* in the *Password Security* section).

5. Copy the configuration file (called `config.ini`) from the
   IBC installation folder to the encrypted folder created in
   step 4.

6. Edit the `config.ini` file,using a text editor such as Notepad, to set
   your username and password in the `IbLoginId` and `IbPassword` settings.
   It's advisable to use your paper-trading credentials at first to check
   things out, and for this you'll also need to set the `TradingMode` setting.

7. Check that the correct major version number for TWS is set in the shell
   script files in the IBC installation folder: these files are
   `StartTWS.bat` and `StartGateway.bat` on Windows, `twsstart.sh` and
   `gatewaystart.sh` on Unix, `twsstartmacos.sh` and `gatewaystartmacos.sh`
   on macOS. 
   
   To find the TWS major version number, first run TWS or the Gateway manually
   using the IBKR-provided icon, then click `Help > About Trader Workstation`
   or `Help > About IB Gateway`. In the displayed information you'll see a
   line similar to either this:

       `Build 981.3c, Jun 29, 2021 3:57:06 PM`

   or this:

       `Build 10.12.2a, Dec 14, 2021 11:07:54 AM`

   In the first case, the major version number is 981. In the second case,
   it is 1012 (ie ignore the period after the first past of the version
   number).

   Ignore the rest of the version number. Now open the script files with a
   text editor and ensure that the TWS_MAJOR_VRSN variable is set correctly.

8. At this stage, everything is set up to run IBC with its default
   settings, which will start TWS and attempt to log it into your
   paper-trading user. It is worthwhile doing this to check that everything
   works before further customising it to suit your needs. To do this, run the
   relevant shell script (`StartTWS.bat` on Windows, `twsstart.sh` on
   Unix, `twsstartmacos.sh` on macOS) from the IBC installation folder.
   If everything is satisfactory, shut down IBC by closing TWS in the
   usual way.

   Note that when you start IBC, information about the startup
   process is logged to a file to aid diagnosing any faults that may
   occur. You will be notified of the log file name during the startup
   sequence.

9. Now you can edit the configuration file `config.ini` to make any further
   customisations you need. See *Configuring IBC* for further information.

10. If you did not install TWS and IBC in their default locations,
   and store the configuration file in the recommended location, you will
   have to edit the shell scripts in the IBC installation folder
   accordingly. They contain comments that will help you do this correctly.

11. If you intend to run API programs to connect with TWS, you will need
    to manually edit the API settings in TWS's Global Configuration Dialog.

### Prerequisites

This section details the other software that is needed to run IBC.

#### Java Runtime

Both IBC and TWS/Gateway are Java programs, and therefore the Java
Runtime needs to be accessible, but you don't have to do anything to ensure
this.

The TWS and Gateway installers include a hidden version of Java
that Interactive Brokers have used for developing and testing TWS. This
version also runs IBC perfectly, and the IBC scripts
ensure that it is used.

This means that it is not necessary to ensure that Java is installed on your
computer. It doesn't matter if it is already installed, but the IBC
scripts won't use it. However the scripts do make provision for declaring
specifically which Java installation is to be used in exceptional situations
where necessary.

If you had previously installed Java for use with old versions of TWS, but
do not need it for any other programs, then you might want to consider
uninstalling it once you have finished setting up IBC.



#### Interactive Brokers Trader Workstation

Before running IBC, you will need to download and install the **offline**
version of Trader Workstation from the
[Interactive Brokers](http://www.interactivebrokers.com/) website.

The location of the TWS dowloads page on IBKR's website varies from time to
time, and from country to country.  At the time of writing, on IBKR's US website
(linked above) you need to click the `Trading` menu near the top of the page,
then select `Platforms`, and scroll down to the Desktop TWS section which contains
a download button: currently a valid direct link is
[Tws Software](https://www.interactivebrokers.com/en/index.php?f=14099#tws-software).

IBKR provides two modes of operation for TWS:

- an online, or self-updating TWS that automatically receives updates as IBKR
enhances it and fixes bugs. IBC **does not work** with the self-updating
TWS, so **do not install the self-updating version for use with IBC**

- an offline or standalone TWS that, after download and installation, never
changes (until you download and install another version): you **must**
download and install this offline version for use with IBC.

Note that the TWS installation includes the code for both TWS and the
Gateway: there is no need to do another download for the Gateway.

However, there are Gateway-specific downloads on IBKR's website. They contain
the same code as the TWS downloads, but they install in a different
place. You can install one of these, as well as or instead of the TWS installer.
You can find these via the LOGIN dropdown in the title bar of IBKR's website.

When you run the script to load TWS, it will use the TWS installation if there
is one, and if not it will use the Gateway installation if there is one.
Similarly when you run the script to load the Gateway, it will use the Gateway
installation if there is one, and if not it will use the TWS installation if
there is one. (Needless to say, if neither a TWS download nor a Gateway
download has been installed, the scripts will fail!)

It is safest to use the 'stable' offline version of TWS rather than the
'latest' version for live trading: the latter is more likely to have bugs.

IBC needs TWS to operate in English so that it can recognise the
various dialogues that it interacts with. You can set TWS's language by
starting it manually (ie without using IBC) and selecting the language on the
initial login dialog. TWS will remember this language setting when you
subsequently start it using IBC.

Note that you do not need an IBKR account to try out IBC, as you can use IBKR's
Free Trial offer, for which there is a link at the top of the homepage on the
website.

### Where to get IBC

IBC is officially distributed as a ZIP file containing the compiled
program and some additional files, detailed below. There are separate
ZIP files for Windows, Linux and macOS.

The ZIP file for the latest version should be downloaded from
[Github](https://github.com/IbcAlpha/IBC/releases).
Earlier versions can also be downloaded from the same place if need be.

The distribution ZIP file contains:

* [License](LICENSE.txt) text
* A compiled JAR (named similar to `IBC.jar`), containing the compiled
 Java code for the IBC program
* A sample configuration file (named similar to `config.ini`)
* Top-level script files that run IBC to start TWS or the Gateway. These files
 are specific to the platform (ie Windows, Linux or macOS) to which the
 ZIP file relates
* A script file that can be used to tidily shut down TWS or Gateway from the
 same or another computer.
* A sample Windows Task Scheduler file (named similar to
`Start TWS Live (daily).xml`), which can be used to automate starting TWS
 or Gateway on Windows systems (not present in the Linux and macOS ZIPs)
* A Scripts sub-folder containing sub-scripts used by the top-level scripts
mentioned above
* A text file called `version` containing the IBC version number

Source code and build scripts are not included in the distribution ZIPs, as
they are freely available from the
[IBC project page](https://github.com/IbcAlpha/IBC) on Github.

### Installing IBC

Installing IBC is just a matter of extracting the contents of the
downloaded ZIP file to wherever you want to install it. You will make things
easiest for yourself if you use the locations described in 'Default Paths'
below, because that will minimise customising the configuration file and
the shell scripts.

If you already have a previous IBC installation, it's wise to rename its
folder (eg to `IBC.old`) so that you can easily refer back to any
customisations you did for that version.

#### On Windows:

- create the folder where you want to install IBC, if it doesn't already
  exist. As noted above (see Default Paths) this is normally `C:\IBC`
  but it can be anywhere you like

- locate the downloaded ZIP file using File Explorer (Windows Explorer on
  Windows 7 and earlier). Windows treats ZIP files like an ordinary folder,
  so you can see its contents the same way as any other folder

- select all the files and folders and drag them into your installation folder

#### On Unix:

- unpack the ZIP file using a command similar to this:

```
sudo unzip ~/Downloads/IBCLinux-3.6.0.zip -d \
/opt/ibc
```

- now make sure all the script files are executable:

```
cd /opt/ibc
sudo chmod o+x *.sh */*.sh
```


#### Default Paths

Several script files are included in each IBC release. These script files (and
these instructions) assume the default paths shown in the table below (where
``<username>`` represents your operating system user name, not your IBKR login
id).

If you store any of these items in other locations, you will need to edit these
script files to reflect this.

| Platform | Item                      | Path                                  |
| -------- | ------------------------- | --------------------------------------|
| Windows  | IBKR TWS program files    | `C:\Jts`                              |
|          | IBC program files         | `C:\IBC`                              |
|          | config.ini                | `%USERPROFILE%\Documents\IBC`         |
| Unix     | IBKR TWS program files    | `/home/<username>/Jts`                |
|          | IBC program files         | `/opt/ibc`                            |
|          | config.ini                | `/home/<username>/ibc`                |
| macOS    | IBKR TWS program files    | `/home/<username>/Applications`       |
|          | IBC program files         | `/opt/ibc`                            |
|          | config.ini                | `/home/<username>/ibc`                |

Note that you may be able to find third-party Linux packages that allow
IBC and/or TWS to be installed using a Linux package manager such as `apt`:
they may not use these paths. Consult your Linux package instructions for file
locations.


### Password Security

To login to TWS or IB Gateway, IBC needs to know your Interactive
Brokers username and password. You should very carefully secure your IBKR
account username and password to prevent unauthorised use by third parties.
This section gives you guidance on how to achieve this.

The username and password are given to IBC in one of two ways:

- via the configuration `.ini` file: this is the preferred method because the
  configuration file can be protected by the operating system

- via the command line parameters when IBC is started: this method is
  strongly deprecated because command line information associated with a
  process is easily available outside the process (for example via Task
  Manager on Windows)

#### Protecting the Configuration File

To protect this sensitive information, the configuration file needs to be
stored in a location where it will not be accessible to other users of the
computer. The simplest way to achieve this is to store it within your personal
filestore:

- on Windows this is your `Documents` folder (which is normally actually
  located at:

  `C:\Users\<username>\Documents`).

  Note that this folder may also be addressed using environment variables like
  this:

  `%USERPROFILE%\Documents`

- on Unix it is the `/home/<username>` directory.

You are advised to place the file in its own `ibc` folder within this location.

You should also consider encrypting the folder containing the configuration
file. This will prevent another user with administrator privileges gaining
access to the contents: even if they use their administrator privileges to
give themselves access to the file, its contents will not be decrypted because
they are not the user that encrypted it.

To encrypt the folder on Windows (note that this requires a Professional or
higher edition of Windows - the home edition does not provide this
facility):

- right click the folder and select `Properties`

- click the `Advanced` button on the `General` tab

- set the checkbox labelled `Encrypt contents to secure data`

- finally, click the `OK` buttons to apply the changes.

Encrypting a folder on Unix is more involved, and you should refer to the
documentation for your distribution.

### Configuring IBC

IBC must be supplied with a configuration file. A specimen file called
config.ini is included in the distribution ZIPs. You will need to edit this
file to include your IBKR username and password, and to ensure that IBC
behaves in the way that best suits your needs.

You should copy the supplied file from the IBC installation folder
into the secure location described above before editing it, so that you have
a clean copy to revert to if need be.

The sample `config.ini` file contains detailed comments on the
meaning of each configuration property. Many of these have sensible defaults,
or are only needed in special situations, so to help you get started quickly,
here is a list of the settings that you are most likely to need to change:

| Setting                        | Notes                                       |
| ------------------------------ | --------------------------------------------|
| IbLoginID                      | You must set this to your IBKR username     |
| Password                       | You must set this to your IBKR password       |
| TradingMode                    | For TWS 955 and later, you must set this to |
|                                | `paper` if you have supplied the username   |
|                                | and password for your live account but      |
|                                | actually want to use your paper account.    |
|                                | Otherwise you can omit the setting entirely |
|                                | or set it to `live`                         |
| IbDir                          | You only need to set this if you want TWS   |
|                                | to store its settings in a different folder |
|                                | from the one it's installed in              |
| AcceptIncomingConnectionAction | It is safest to set this to `reject` and to |
|                                | explicitly configure TWS to specify which   |
|                                | IP addresses are allowed to connnect to the |
|                                | API                                         |
| IbAutoClosedown                | Set this to `no` to prevent TWS's daily     |
|                                | auto closedown: NB this setting no longer   |
|                                | works with TWS 974 and later                |
| ClosedownAt                    | Set this if you want to keep TWS running    |
|                                | until a specified time of day on a          |
|                                | particular day of the week, or to specify a |
|                                | time when Gateway should be shut down every |
|                                | day (a daily shutdown time for TWS can be   |
|                                | specified through TWS's own configuration   |
                                 | dialog.                                     |


There are two ways that IBC can locate your edited `config.ini` file.

- the simplest way is to tell it where to find the file in the script that
  starts IBC. In this way, you can give the configuration file any name you
  like. This is the recommended approach, and the supplied scripts follow this
  approach. If you want to change the filename from config.ini, or if you store
  it somewhere other than the default location, you'll have to edit the start
  script to declare its new name and location.

- if you do not specify a configuration file name, IBC will expect to find a
  file named `config.ini` in the current computer user's private filestore. For
  Windows users, the location is `%USERPROFILE%\Documents\IBC`. For Unix
  users, it is `~/ibc`.

### Starting IBC

The normal way to start IBC is by use of a shell script. These can be
identified by the `.bat` (Windows) or `.sh` (Unix) extensions. Scripts
to start TWS and Gateway are included in the distribution ZIPs, and due to
their complexity you are strongly advised to use them, rather than try to
create your own.

Windows users can execute a shell script in a number of ways, including:

* Double-click the filename in Windows Explorer
* Create a shortcut to it on your Start menu, desktop or taskbar
* Create a scheduled task to run it automatically at the required times (see
  below for more information about using scheduled tasks)

If you used the default locations to install IBC and TWS, and to store your
config.ini file, you should not need to edit the shell scripts. If you do need
to change them, they are commented to help you.

## Other Topics

### Second Factor Authentication

You can use your mobile phone or tablet running Android or IOS to provide
second factor authentication for your TWS login. To do this you'll need to
install the IBKR Mobile app on your device, which you can download from the
relevant app store. Once you've installed it, you can register it for
second factor authentication via the button that it prominently displays.

Once it's registered, every time you login to TWS or Gateway (including when
IBC does it for you) you'll receive an alert on your device. When you then
enter your registered PIN into the app, your login will complete.

Note that IBC cannot itself assist in the process, so you'll have to actually
perform the necessary actions on your device yourself, but it's fairly
convenient because you don't need to be anywhere near your computer running
TWS, which is helpful if you've used some automated mechanism to start TWS.

However, if you fail to respond to the alert within a fixed period (currently
3 minutes), you will not then be able to complete your login without manual
intervention at TWS, and this is where IBC _can_ help. You can canfigure IBC
to detect such timeouts and to shut down when this happens. And you can set
it so that IBC is automatically restarted, thus restarting the normal login
sequence and thereby giving you another chance to receive the second factor
authentication alert on your device. This timeout/restart mechanism can
repeat any number of times.

To enable this behaviour you need this setting in your `config.ini` file:

`ExitAfterSecondFactorAuthenticationTimeout=yes`

This will ensure that IBC exits when it detects a second factor authentication
timeout. If you also want it to be automatically restarted, you must also
set the `TWOFA_TIMEOUT_ACTION` variable in your start script file to `restart`
(see the notes for this variable in the relevant start script).

If you have another automatic means of restarting IBC after it closes (for
example Task Scheduler on Windows), then you should consider setting the
`TWOFA_TIMEOUT_ACTION` variable in your start script to `exit`, to avoid
the situation where both mechanisms react at the same time.


### Scheduled Tasks (Windows only)

On Windows you can start IBC automatically using the Task Scheduler to run
`StartTWS.bat` or `StartGateway.bat`.

When you define your task, make sure that the option to 'Run only when user
is logged on' is selected. Doing this will ensure that you can see and interact
with TWS.

You will then need to log on before the task runs.

Note that you can set up Windows to log on automatically at startup: this might
be useful, for example, if your system's BIOS allows you to configure the
system to power on at a particular time. Information on how to do this is
freely available on the internet. But bear in mind that doing this can
negatively impact your system's security.

Task Scheduler does actually allow you to specify that your task should run
whether or not the user is logged in. However if you do this, the task is
always started in a separate user session which you cannot see and interact
with, even if you are already logged on when the task starts, or if you
subsequently log on. Therefore you are strongly advised NOT to use the option
for 'Run whether user is logged on or not'.

Remember also to change the task settings to prevent Windows automatically
ending it after a certain time.

Also, for versions of TWS earlier than TWS 974, you can use the
`IbAutoClosedown=no` setting in the IBC configuration file to disable TWS's
autologoff feature, and the `ClosedownAt=` setting to specify when IBC will
shut down TWS. The `IbAutoClosedown=no` setting DOES NOT WORK properly with
TWS 974 and later.

In this way you can start IBC automatically on Sunday evening or Monday
morning, keep it running all week and then close down tidily on Friday evening
or Saturday morning.

The Windows Task Scheduler has many powerful features, and some of these can
be used to provide even better control. For example, you can run the task
periodically (say every 10 minutes) during the week so that if TWS crashes or
is manually shut down, it will automatically be restarted. If you also set up
your computer to log on automatically when it starts, this will ensure TWS is
restarted after a power outage. (Information about how to make your computer
log on automatically is easily available on the internet: but make sure you
understand the security implications of autologon to Windows).

**IMPORTANT** Note that Microsoft have made changes to the Task Scheduler for
Windows 10. Because of this, it is advisable to set up your Scheduled Task
differently on Windows 10: see the next section _Running under Task Scheduler
on Windows 10_.

**IMPORTANT** Make sure you use the `/INLINE` argument to `StartTWS.bat` or
`StartGateway.bat` when starting IBC from Task Scheduler.
Otherwise IBC will start and run correctly, but Task Scheduler will not
be aware of it: in particular Task Scheduler will not show the task as running.
This prevents correct operation of Task Scheduler features such as killing the
task after a specified elapsed time. The reason for this is that as far as
Task Scheduler is concerned, the task is simply the command processor process
that it creates to run the .bat file, and does not include processes created
by it.

A sample scheduled task is included in the IBC distribution ZIP,
called `Start TWS Live (daily).xml`. You can import this into your Task
Scheduler if you are running Windows 7, Windows 8 or Windows 8.1 (see below
for further information about running on Windows 10). After importing it, you
will need to enable it and change the user account it runs under. This task
starts TWS daily at 05:55, and assumes that TWS is set to autologoff at 05:52,
so the IBC configuration file must include `IbAutoClosedown=yes`: you can
adjust these times to suit your needs.

#### Running under Task Scheduler on Windows 10

Microsoft have made significant changes to the Task Scheduler in Windows 10.
Although the management user interface is pretty much the same as in earlier
Windows versions, there are important changes in some of the 'under the hood'
operation.

The net effect of these changes is that it is no longer a good idea to start
IBC under Task Scheduler by running a command file. It will only work
correctly if the command given to Task Scheduler directly runs IBC.

To set this up, first run IBC manually (using `StartTWS.bat`
or `StartGateway.bat`), and open the log file in Notepad or any other
text editor: if using Notepad, make sure that 'Word Wrap' on the Format menu is
not checked). Now create your scheduled task (it's easiest to import the sample
included in the IBC download zip file), and open the start action editor.
Find the line in the log file that reads: 'Starting IBC with this
command:', then select and copy the first part of the following line (up to but
not including `-cp`), and paste it into the `Program/script:` field of the
action editor. Then select and copy the remainder of the line in the log file
(starting at `-cp`), and paste it into the `Add arguments (optional):` field
of the action editor. You can now run this scheduled task in the normal way.

Note that running IBC from Task Scheduler via a direct command in this way
means that there is no permanent IBC log file. Any output from IBC appears in
the window that Java creates to host the Java console output, but there is no
way to capture this to a file (note that normal redirection operators `>` and
`>>` cannot be used in a command in a scheduled task). If you've made sure
that your IBC installation operates correctly before setting up your scheduled
task, this should not be too much of a problem.

### Running with crontab (Linux only)

On Linux you can use `crontab` to run `twsstart.sh` or `gatewaystart.sh`
automatically.

For example, to run `gatewaystart.sh` at 08:00 on Mondays, include a line like
this in your personal crontab:

`* 8 * * 1 export DISPLAY=:10 && /bin/bash /opt/ibc/gatewaystart.sh`

The value you need for the DISPLAY variable will depend on how your system is
configured.

Starting with IBC 3.8.1, the `twsstart.sh` and `gatewaystart.sh` scripts include
a check to see if IBC is already running with the same `config.ini` file: if it
is, a new instance is not started.

This enables a more sophisticated crontab entry that will periodically attempt
to start IBC, but only succeed if it is not already running. For example:

`0,15,30,45 * * * 1-5 export DISPLAY=:10 && /bin/bash /opt/ibc/gatewaystart.sh`

will try to run gatewaystart.sh every 15 minutes from Monday to Friday. This can
be useful to restart TWS/Gateway after an unexpected shutdown, or, in
conjunction with the use of the `ExistingSessionDetectedAction=primaryoverride`
setting in `config.ini`, to automatically restart it if using the IBKR Mobile
app or the Client Portal on the IBKR Account Management page causes your
TWS/Gateway session to be shut down.

### Multiple IBC Instances

You may want to run more than one instance of TWS or the Gateway on the same
computer, perhaps simultaneously. Here are some reasons you might want to do
this:

- you want to run both your live and paper-trading IBKR accounts. This is
  especially true if you want to get market data from both accounts, as IBKR
  will only allow this if both TWS instances are on the same computer
  (unless you don't try to run them at the same time)

- you have multiple logins for your live IBKR account, and want to run
  TWS for both, perhaps at the same time

- you trade on behalf of others, perhaps your family, friends or clients,
  who each have their own accounts, but you want to run TWS instances for
  all these accounts on one powerful computer

- you want to trade in different regions at different times

- you want to test a new version of TWS in your paper trading account at
  the same time as using your live account in a previous version

When TWS runs, it stores a large number of settings in a folder structure
(these settings may also be stored in IBKR's servers, but this may not be a
useful option if you want to use multiple TWS instances). By default, TWS
stores this settings folder structure in the TWS installation folder. For
each username, it creates a separate folder structure. Note however that
there are some files that TWS creates while running that are not separated
by username in this way, and only one instance of TWS can access them at a
time. So you can run multiple TWS instances with no problem provided each
instance is logged in to a different username, AND you don't try to run them
at the same time.

However, by using the `IbDir` setting in the IBC configuration file,
you can tell TWS to store its settings whereever you like. So to have multiple
IBC instances operating simultaneously, you need to create a separate
configuration file for each instance with a different setting for `IbDir`. Note
that you do not need to copy the TWS .jar files themselves - you can load TWS
from the same installation folder for each instance.

Because you now have different configuration files, you also need different
scripts to run each instance (or you could have a single script and pass the
configuration file details as a parameter). And you need to ensure that the
different instances don't try to write their log files to the same folder
(because otherwise they might try to log to the same file, and one instance
would fail).

As a concrete example, let's take the first scenario described above: you want
to run both your live and paper trading accounts without them interfering with
each other in any way. But before describing the steps to achieve this, here's
something to bear in mind: if you have already been running either or both the
live and paper TWSs, you may have already spent quite some time configuring
them, and you won't want to have to repeat this work. TWS provides a means of
saving and subsequently restoring settings (the `Save Settings As...`
and `Settings Recovery...` commands on the `File` menu, and you can use these to
keep your current settings in a temporary location, and then restore them once
you've finished setting up the two instances.

So:

- install TWS into the default location (`C:\Jts` on Windows)

- create two new folders `C:\JtsLive` and `C:\JtsPaper` to store the settings

- create two IBC configuration files called `configLive.ini`
 and `configPaper.ini`

- set the IbDir option in them to point to the relevant folder, ie
  `IbDir=C:\\JtsLive` and `IbDir=C:\\JtsPaper`, and set the `IbLoginId`
  and `IbPassword` to the live or paper account values as appropriate

- create two start scripts (by copying `StartTWS.bat`) called
  `StartTWSLive.bat` and `StartTWSPaper.bat`

- change the `set CONFIG=...` line in each script file to refer to the
  relevant configuration file

- change the `set LOG_PATH=...` line in each script file to refer to different
  folders, for example `set LOG_PATH=%IBC_PATH%\LiveLogs` and
  `set LOG_PATH=%IBC_PATH%\PaperLogs`

- now you can run the new scripts, and each will start a separate instance of
  TWS connected to a different account, with its settings stored in separate
  folders.

#### Using different TWS versions simultaneously

To use more than one version of TWS (for example for testing a new version
with your paper-trading account while also using a previous version for
your live account), you just need to install the required versions in the
normal way. Version 952 and later of TWS have installers that automatically
place the relevant files in separate folders named according to the version
number.

Then follow the advice in the previous section and ensure that each script
file has the correct value for the `TWS_MAJOR_VRSN` variable.

### Any Questions?

If you need assistance with running IBC, or have any queries or
suggestions for improvement, you should join the IBC User Group
at:

[https://groups.io/g/ibcalpha](https://groups.io/g/ibcalpha)

If you're convinced you've found a bug in IBC, please report it
via either the IBC User Group or the GitHub Issue Tracker at:

[https://github.com/IbcAlpha/IBC/issues](https://github.com/IbcAlpha/IBC/issues)

Please provide as much evidence as you can, especially the versions of
IBC and TWS/Gateway you're using and a full description of the
incorrect behaviour you're seeing.

### Changes from IBController

Although IBC has been forked from the original IBController project, it is not
identical and there are several important differences that you'll need to take
account of if you're switching from IBController to IBC.

Here are the main differences between IBC and IBController:

1. The program file is now called IBC.jar.

2. Changes to the settings file:

   - in IBController, the configuration settings were held in a file called
    `IBController.ini` by default, whereas the equivalent file in IBC
	is called `config.ini`

   - the setting previously called `ForceTwsApiPort` has been renamed
	`OverrideTwsApiPort`

   - the `AcceptIncomingConnectionAction` setting previously had a default of
	`accept`. This default has now changed to `manual`, which means that the
	user must now explicitly configure IBC to automatically accept API
	connections from unknown computers.

   - the settings `PasswordEncrypted` and `FIXPasswordEncrypted` have been
    removed, as has the facility to 'encrypt' these passwords.
	
   - the `IbControllerPort` setting has been renamed to `CommandServerPort`,
     and its default value is 0 (zero), which is taken to mean 'do not
	 start the command server'

   - the `IbControlFrom` setting has been renamed to `ControlFrom`

   - the `IbBindAddress` setting has been renamed to `BindAddress`

3. Changes to top-level script names:

   On Windows:

       IBControllerStart.bat 				-> 	StartTWS.bat
	   IBControllerGatewayStart.bat 		-> 	StartGateway.bat
       IBControllerStop.bat 				-> 	Stop.bat
	
   On Linux:

       IBControllerStart.sh 				-> 	twsstart.sh
	   IBControllerGatewayStart.bat 		-> 	gatewaystart.sh

   On macOS:

       IBControllerStart-OSX.sh 			-> 	twsstartmacos.sh
	   IBControllerGatewayStart-OSX.bat 	-> 	gatewaystartmacos.sh

	
