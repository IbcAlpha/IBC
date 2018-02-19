**Download the
[latest official release](https://github.com/IbcAlpha/IBC/releases/latest)
here**

IBC provides hands-free operation of
[Interactive Brokers](https://www.interactivebrokers.com)
[Trader Workstation](http://www.interactivebrokers.com/en/pagemap/pagemap_APISolutions.php).
It's especially useful for developers of automated trading systems who use
[IB API](http://interactivebrokers.github.io). Features include:

* Automates Trader Workstation (TWS) and IB Gateway (including the FIX mode)
* Completes login dialog with credentials from an ``.ini`` file or command line
* Handles dialog boxes which TWS presents during programmatic trading activities
* Keeps TWS running indefinitely by handling TWS time-based exits
* Allows TWS to be terminated at a specified time on a specified day of the week
* Dismisses various dialog messages (eg version updates, daily tips, IB API
  connections etc)
* Exposes a [telnet](http://en.wikipedia.org/wiki/Telnet) control protocol to
  ``STOP`` the GUI or ``ENABLEAPI`` for IB API client access

Downloads
---------

If you just want to use IBC without modifying it, you should download 
the latest official release ZIP which you can find [here](https://github.com/IbcAlpha/IBC/releases/latest).

User Guide
----------

Please see the [IBC User Guide](userguide.md) for installation and
usage instructions. The User Guide is also included as a PDF file in the 
download ZIP.

Support
-------

> IMPORTANT
> By far the most common problem that users have when setting up IBC
is the result of trying to use it with the self-updating version of TWS.
>
>**IBC DOES NOT WORK with the self-updating version of TWS.**
>
>You must install the offline version of TWS for use with IBC.
>
>Note however that there is no self-updating version of the Gateway, so the
normal Gateway installer will work fine if you only want to use the Gateway.

If you need assistance with running IBC, or have any queries or 
suggestions for improvement, you should join the 
[IBC User Group](https://groups.io/g/IBC).

If you're convinced you've found a bug in IBC, please report it
via either the [IBC User Group](https://groups.io/g/IBC)
or the [GitHub Issue Tracker](https://github.com/IbcAlpha/IBC/issues).

Please provide as much evidence as you can, especially the versions of 
IBC and TWS/Gateway you're using and a full description of the 
incorrect behaviour you're seeing.

Note that IBC creates a log file that records a lot of useful 
information that can be very helpful in diagnosing users' problems. The 
location of this log file is prominently displayed in the window that appears
when you run IBC. It is helpful to attach this log file to any 
problem reports.

Contributing
------------

We welcome your involvement and contributions! Please read the
[contributor guidelines](CONTRIBUTING.md), and send us a 
[pull request](../../pulls).

We also thank past contributors to the IBController project from which IBC
was forked: Richard King, Steven Kearns, Ken Geis, Ben Alex and Shane Castle.

License
-------
IBC is licensed under the
[GNU General Public License](http://www.gnu.org/licenses/gpl.html) version 3.
