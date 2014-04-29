Scope
-----
For releases 2.11.0 and earlier, please refer to the ``readme.txt`` file included
in the release ZIP file. You can also view these readme files online. For example,
the [version 2.11.0 readme.txt](https://github.com/ib-controller/ib-controller/blob/2.11.0/readme.txt)
provides detailed instructions on configuring that version.

For more recent versions, the following instructions apply.

Distribution Contents
---------------------
IB TWS and IB Controller are both
[Java](https://www.java.com)
applications. The distribution ZIP files contain:

* [License](LICENSE.txt) text
* Compiled JAR (named similar to ``IBController.jar``)
* Sample configuration file (named similar to ``IBController.ini``)
* Sample TWS launch script (named similar to ``sampleIBControllerStart.bat|sh``)
* Sample IB Gateway launch script (named similar to ``sampleIBControllerGatewayStart.bat|sh``)

Source code and build scripts are not included in the distribution ZIPs, as they
are freely available from the
[GitHub project page](https://github.com/ib-controller/ib-controller).

Aside from IB Controller, you will need to download and install IB
[Trader Workstation](http://www.interactivebrokers.com/en/pagemap/pagemap_APISolutions.php).
You do not need an IB account to try out IB Controller, as you can use the IB
demo account (username ``edemo``, password ``demouser``).

Default Paths
-------------
Several sample files ship in each IB Controller release. Your main installation
task is to edit these sample files to suit your particular needs. The sample
files (and these instructions) assume the following default paths:

| Platform | Product       | Path                                                |
| -------- | ------------- | --------------------------------------------------- |
| Windows  | IB TWS        | ``C:\Jts``                                          |
| Windows  | IB Controller | ``C:\IBController``                                 |
| Unix     | IB TWS        | ``/opt/IBJts``                                      |
| Unix     | IB Controller | ``/opt``                                            |

Note that installing IB Controller and/or TWS from a Linux package manager will
not use these paths. Consult your Linux package instructions for file locations.

Installation
------------
Installing IB Controller is just a matter of unzipping the download file to
wherever you want to install it. A Linux user might use something like:

```
cd /opt
sudo unzip ~/Downloads/IBControllerV1.2.3.zip
```

Password Security
-----------------
To logon to TWS or IB Gateway, IB Controller needs to know your Interactive
Brokers username and password. You should very carefully secure your IB account
username and password. Your main options are:

1. Store the password in the IB Controller configuration ``.ini`` file and
   rely on operating system security to protect it. This is strongly
   recommended, although complex for many users to implement. Linux users should
   use any operating system packages available for their distribution, as these
   have probably implemented the recommended security option (although check
   the package documentation for details).
2. Store the password in the IB Controller configuration ``.ini`` file and
   use the encrypted encoding option. Please note that encrypted encoding is
   easily reversed. It should not be relied upon on its own.
3. Pass the password as a command line parameter. This is strongly discouraged
   as command line information is available in many places on your computer
   (eg shell histories, process lists etc).

To encrypt your password (ie for option 2 above), run the following command from
the IB Controller directory:
 
``java -cp IBController.jar ibcontroller.IBController encrypt <your-password>``

The program output will include your encrypted password, which can then be
included in your IB Controller configuration ``.ini`` file. You should also
clear your shell history if you entered the above command. In most cases this is
achieved by pressing ALT+F7 (Windows users) or typing ``history -c`` (Linux),
although you should check it worked by pressing the up arrow afterwards.

Configuration
-------------
The sample ``.ini`` file will require editing. This file observes the
[Java property file syntax](http://en.wikipedia.org/wiki/.properties), including
using ``#`` for comments. Please refer to the sample ``.ini`` file for detailed
comments on the meaning of each configuration property.

Generally you will rename the sample configuration file to a new name and
edit the ``IbLoginID`` and ``IbPassword`` options at minimum. The
``PasswordEncrypted`` setting should be set to ``yes`` if you used the
aforementioned encrypted password encoding option.

There are two ways that IB Controller can locate your edited ``.ini`` file. The
simplest way is to tell it where to find the file. If you do this, you can give
the configuration file any name you like. This is the recommended approach,
but will require you to edit the execution shell script (details below).

If you do not specify a configuration file name, IB Controller will expect to
find a file named ``IBController.<username>.ini`` in the current working
directory. In this case, ``<username>`` is your username on your computer (not
your IB account username).

Launching
---------
The normal way to start IB Controller is by use of a shell script. These can
be identified by the ``.bat`` (Windows) or ``.sh`` (Unix) extensions. The
purpose of each shell script was mentioned in the "Distribution Contents"
section above.

Windows users can execute a shell script in a number of ways, including:

* Double-click the filename in Windows Explorer
* Create a shortcut to it on your Start menu
* Create a scheduled task to run it automatically at the required times

Most users will not need to edit the launch shell scripts in any significant
manner. The most likely edit is to specify the path to your configuration
``.ini`` file. The command for running IB Controller has the following form:

``java -cp <classpath> <otherOptions> <entryPoint> [<config file>|NULL] [<loginId> <password>]``

Again it is recommended you do **not** specify your login credentials on the
command line. Place it in the configuration ``.ini`` file instead, and specify a
fully-qualified path to the configuration ``.ini`` file (with that file ideally
protected by suitable operating system file permissions).

Headless Execution (Unix)
-------------------------
IB Controller can operate without a display via a virtual framebuffer. Refer
to the Arch Linux [ib-controller](https://aur.archlinux.org/packages/ib-controller/)
AUR package for a good example of headless operation (as well as other
considerations you'd typically need for a server, such as systemd configuration,
``/etc`` directory file permissions etc).

Scheduled Tasks (Windows)
-------------------------
On Windows you can start IB Controller automatically using a Scheduled Task. 

If you do this, you must make sure that the machine is already logged on before
the scheduled task runs. Otherwise the task will still run, but you won't be
able to see and interact with TWS, even if you subsequently log on. 

Remember also to change the task settings to prevent Windows automatically
ending it after a certain time.

Also you can use the ``IbAutoClosedown=no`` setting in the IB Controller
configuration file to disable TWS's autologoff feature,  and the
``ClosedownAt=``setting to specify when IB Controller will shut down TWS.

Questions?
----------
Lots of people use IB Controller and are happy to assist you! Please see the
[support information](https://github.com/ib-controller/ib-controller#support)
for details.
