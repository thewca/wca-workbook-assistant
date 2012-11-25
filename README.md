A Java application for preparing Excel workbooks with WCA competition results for uploading them to the WCA database.

# Building the application

There is an Ant build script called `build.xml` in the root directory of the project. The default target generates an executable jar file under the `target` directory. The build script uses the Proguard obfuscator to merge all the third party libraries and the application code in to one jar file. This usually takes quite a bit of memory, so it is advisable to set the `ANT_OPTS` environment variable and give Ant some more heap space, like such:

    export ANT_OPTS=-Xmx256m