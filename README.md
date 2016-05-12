**Download the
[latest official release](https://github.com/ib-controller/ib-controller/releases/latest)
here**

IBController provides hands-free operation of
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

If you just want to use IBController without modifying it, you should download 
the latest official release ZIP which you can find [here](https://github.com/ib-controller/ib-controller/releases/latest).

Should you ever need to revert to an earlier IBController release, they are also available 
[here](https://github.com/ib-controller/ib-controller/releases).

If you use Arch Linux you can install the
[ib-controller](https://aur.archlinux.org/packages/ib-controller/) package
from AUR: it includes lots of server-oriented features such as multiple daemons,
systemd units, headless/virtual framebuffer support, Monit monitoring etc.

Developers may wish to embed IBController for integration testing convenience
(eg see projects
[GoIB](https://github.com/gofinance/ib/tree/master/testserver) and
[IB Connect](https://github.com/benalexau/ibconnect/tree/master/testserver))

User Guide
----------

Please see the [IBController User Guide](userguide.md) for installation and
usage instructions. The User Guide is also included as a PDF file in the 
download ZIP.

Support
-------

If you need assistance with running IBController, or have any queries or 
suggestions for improvement, you should join the 
[IBController User Group](https://groups.io/g/ibcontroller).

If you're convinced you've found a bug in IBController, please report it
via either the [IBController User Group](https://groups.io/g/ibcontroller)
or the [GitHub Issue Tracker](https://github.com/ib-controller/ib-controller/issues).

Please provide as much evidence as you can, especially the versions of 
IBController and TWS/Gateway you're using and a full description of the 
incorrect behaviour you're seeing.

Contributing
------------

We welcome your involvement and contributions! Please read the
[contributor guidelines](CONTRIBUTING.md), and send us a 
[pull request](../../pulls).

We also thank past contributors to the original SourceForge project: Richard
King, Steven Kearns and Ken Geis. Plus of course all those on our GitHub
[contributor list](../../graphs/contributors).

License
-------
IBController is licensed under the
[GNU General Public License](http://www.gnu.org/licenses/gpl.html) version 3.
