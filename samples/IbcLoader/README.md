IbcLoader Sample Application
----------------------------

The IbcLoader sample application shows how you can use IBC to start
TWS/Gateway from your own Java code, so that IBC and TWS actually run
within your application.

Note that running TWS/Gateway within your application in this way needs
careful consideration, in particular because if the user closes
TWS/Gateway, that will also terminate your application.

To run this application on Windows, use the RunIbcLoader.bat command
file, supplying the TWS major version number as the first argument,
for example:

`RunIbcLoader.bat 947`

Note that you must already have TWS/Gateway installed. A compiled jar
for the program and for IBC are included, so you don't need to build it
to try it out. A bash script to run the program on Linux will follow
later.

IbcLoader uses the dependency injection mechanisms within IBC to replace
some of the default functionality in IBC:

* the `MySettings` class provides its own values for the various
settings used by IBC, thus removing the need for a `config.ini` file as
used in the normal IBC setup

* the `MyLoginManager` class supplies the username and password

* the `MyTradingModeManager` class supplies the trading mode (live or
paper)

* the `MyMainWindowManager` class augments the main window handling
in IBC by setting the main window transparency to 80% and overlaying
a simple clock on the main window (this is not to suggest that such
things are particularly useful, just that they illustrate what can be
done).

It's worth pointing out that the current version of IBC (3.7.3) doesn't
have a way of injecting a logging mechanism, so all the logging that IBC
does just goes to System.out. Given that most sensible applications have
their own logging mechanism, it would be far preferable for the launcher
application to be able to inject a logger that IBC would use so that its
logging is integrated with the launching application's. This will be
addressed in a (near?) future IBC release.

Finally, in case anyone notices, there is a peculiar phenomenon that
this program exhibits when running in a Hyper-V virtual machine on
Windows 10. When the program is running and there is no RDP connection
to the virtual machine, the program's CPU usage runs very high. You can
see this by leaving Task Manager running with the Performance tab
visible, then closing the RDP session or switching to another program
on the host, then returning to the RDP session after a few seconds: the
CPU usage graphs will be high, and will return to normal almost
immediately after returning to the session. Also if you show the CPU
time in the Details tab, you'll see that this increases by roughly the
same number of seconds as you leave the session for. This behaviour,
as far as I can determine, is a bug in the handling of the Swing
glass pane (which this program uses to draw the clock).

