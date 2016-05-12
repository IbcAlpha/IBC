We warmly welcome pull requests. The following guidelines reflect how we try
to maintain the project and in turn the pull requests we're likely to merge:

1. **Discuss it first**: Before starting on a change, it's wise to raise an
   issue on GitHub explaining what you propose to do. There may be reasons
   why your proposed change is unlikely to be accepted, or someone else may
   already be working on something similar, or it may even have been proposed
   and rejected before. You can also discuss suggestions for improvement on 
   the [IBController User Group](https://groups.io/g/ibcontroller).
   
2. **Cross-platform**: Changes must work on both Linux/Unix and Windows.
   Please make sure to test thoroughly on both platforms, and for both TWS
   and the Gateway, before submitting your pull request. If you can't do this,
   then please make clear in your pull request what testing you have done.
   
3. **Broad scope**: Improvements should be potentially useful to a significant
   proportion of the user base.
   
4. **Non-developer users**: A lot of our users aren't developers. It must be
   easy for them to use any improvements. A practical approach is to ensure the
   [configuration file](resources/IBController.ini) and/or
   [user guide](userguide.md) reflects changes.
   
5. **Backward compatibility**: New versions should not force users of previous
   versions to change their configuration or command files (ie no surprises),
   unless this is absolutely essential. Obviously it may be necessary to 
   introduce new settings into the [configuration file](resources/IBController.ini)
   or enhance the script files, but this should be done in such a way that 
   if a user moves to the new version of IBController without making any changes
   their TWS/Gateway will continue to operate exactly as before.
   
6. **Respect IB T&C**: Changes must not be an infringement of the intent or
   provisions of IB's Terms and Conditions. For example, there should be no
   attempt to disable/automate logon for users enrolled in IB's security program.
   
7. **Style**: Please maintain the same style as the existing code unless you've
   discussed a refactor with the current maintainers via email or a GitHub
   [Issue](https://github.com/ib-controller/ib-controller/issues).

Development Tips
================
[Apache Ant](http://ant.apache.org/) is required to build IB Controller. After
installation of Ant and TWS onto your machine, run Ant with the `TWS`
environment variable set to your local TWS directory. This is required so that
the compiler can locate `jts.jar`. For example:

```
TWS=/usr/share/java/ib-tws ant clean dist
```

This will freshly compile the project, create a JAR and a distribution ZIP. New
versions require editing the `ver` property in the [build.xml](build.xml), as
that version number is included in the resulting ZIP.
