# Lab 2: Fine-tuning Java applications

[Lab 1](./Lab_1.md) goes into detail about how we can choose a base image to reduce our image sizes. This lab will go into more detail into how we can make further optimizations to our image to make them it more efficient.

## Class Data Sharing

As [Oracle](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/class-data-sharing.html) explain, *Class data sharing (CDS) helps reduce the startup time for Java programming language applications, in particular smaller applications, as well as reduce footprint. When the JRE is installed using the installer, the installer loads a set of classes from the system jar file into a private internal representation, and dumps that representation to a file, called a "shared archive"... the shared archive is memory-mapped in, saving the cost of loading those classes and allowing much of the JVM's metadata for these classes to be shared among multiple JVM processes.*

This feature has been available in Java since JDK 5 and is quite simple to take advantage of. Continue with this lab to see how to take advantage of CDS with our Java application. 

### Using the shared archive 

The shared archive can be created simply by running the command:

```java
java -Xshare:dump
```

**NOTE: If this command fail on Linux/Mac you may need to run this command with sudo**

This creates the archive in a file called: `classes.jsa`

By default, Java will use CDS as long as the shared archive exists. If not, it will run as normal. To be explicit, we will simply add the argument `-Xshare:on ` to our java command to run the fat jar.

Great, we can now add this to our Dockerfile so our Java containers start faster! Well, let's just start off with seeing how much of a difference CDS has made to our Java application with some simple benchmarking: 

### Benchmarking our application

First of all open up a new terminal/console (PowerShell if on Windows).

Now create a new environment variable within the shell that our java application will pick up so we don't get bombarded with logs. (*NOTE: This environment variable will be wiped when you close the shell*): 

`export LOG_LEVEL=OFF`

OR 

`$env:LOG_LEVEL="OFF"` 

Next, make sure that you are in the `java-containers101/docker` directory. To have an estimate of how long it takes to start-up our application in consecutive runs, we can run our application without CDS 10 times:

On Linux/Mac using zsh:

```bash
time (repeat 10 { \
      java -Xshare:off \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit \
})
```

Or on Linux/Mac using bash:

```bash
time for i in {1..10}; do \
      java -Xshare:off \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit; \
done
```

Or on Windows using PowerShell:

```bash
Measure-Command { foreach ($j in 1..10) {
      java -Xshare:off `
           -jar java-containers101-1.0-SNAPSHOT.jar --exit 
}}
```

The `--exit` flag is used to shutdown the application as soon as it has finished start-ing up so we don't hang in an interactive shell. This allows us to repeatedly call the application

For Linux/Mac, we are interested in the `user` output as this gives the CPU time in seconds taken to execute the process (If you were to increase/decrease the number of cores the application could use, the value would be similar in consecutive runs). It's a better value than `total` or `real` as it does not count other processes or time spent blocked. For Windows, we just have to rely on the `TotalSeconds` output which will just give us the elapsed time. Be sure to record these stats somewhere!

### Benchmarking our application with CDS

Now we can benchmark our application with CDS. In the same shell we can benchmark our application while making use of the shared archive:

On Linux/Mac using zsh:

```bash
time (repeat 10 { \
      java -Xshare:on \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit \
})
```

Or on Linux/Mac using bash:

```bash
time for i in {1..10}; do \
      java -Xshare:on \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit; \
done
```

Or on Windows using PowerShell:

```bash
Measure-Command { foreach ($j in 1..10) {
      java -Xshare:on `
           -jar java-containers101-1.0-SNAPSHOT.jar --exit 
}}
```

Of course the number of processes running and the specification of your laptop will affect the benchmark. To make your benchmarking a bit more reliable, make sure you don't open new applications between the two use cases (close uneeded Browser tabs and applications)! You can also run this a few times or increase the value from 10 to get a better idea of the range for each use cases. 

Looking at the results, you may have found that it is not conclusive that CDS is better than not using CDS. As [Oracle](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/class-data-sharing.html) state, *CDS produces better results for smaller applications because it eliminates a fixed cost: that of loading certain core classes. The smaller the application relative to the number of core classes it uses, the larger the saved fraction of startup time.* As our application uses quite a number of external libraries to run Spring Boot, the application size is larger and seems to make using CDS negligible. If only we could have CDS for all these extra external libraries... 

## Application CDS 

Application CDS is exactly what it suggests: it allows us to have Class Data Sharing for our external libraries. Unlike CDS however, Application CDS was only available commercially up until JDK 10 (which is why we had install openJDK 10 to play with this feature). Similar to before, there are more saving to be won the more application classes are being used and more memory savings as there are more containers sharing the same cache. 

### Creating the shared archive 

As Application CDS is dependent on the applciation, there is an extra step we have to run in order to create the list of application classes to include in the archive:

On Linux/Mac:

```java
java -XX:+UseAppCDS \
      -XX:DumpLoadedClassList=classes.lst \
      -jar java-containers101-1.0-SNAPSHOT.jar --exit 
```

On Windows using PowerShell:

```java
java -XX:+UseAppCDS `
      -XX:DumpLoadedClassList=classes.lst `
      -jar java-containers101-1.0-SNAPSHOT.jar --exit
```


**NOTE: Once again, if this command fail on Linux/Mac you may need to run this command with sudo**

This will run our application and as each class is loaded, if it can be used in the archive, Java will add it to the file we specified `classes.lst` (which will be saved in the current directory). This is just a simple list of the slash separated names of each class. 

Next we can generate the share archive from `classes.lst`:

On Linux/Mac:

```java
java -XX:+UseAppCDS \
      -Xshare:dump \
      -XX:SharedClassListFile=classes.lst \
      -XX:SharedArchiveFile=app-cds.jsa \
      --class-path java-containers101-1.0-SNAPSHOT.jar
```

On Windows using PowerShell:

```java
java -XX:+UseAppCDS `
      -Xshare:dump `
      -XX:SharedClassListFile=classes.lst `
      -XX:SharedArchiveFile=app-cds.jsa `
      --class-path java-containers101-1.0-SNAPSHOT.jar
```

**NOTE: Once again, if this command fail on Linux/Mac you may need to run this command with sudo**

The archive will be produced in the file `app-cds.jsa` in the current directory. As we are creating a cache, we may end up in situations where we update our JAR and use a slightly different combination of external library packages and versions. Using `--class-path` as opposed to `--jar` will help to reduce the risk of strange behaviour due to an expired cache as it verifies that the list of classes in the supplied classpath when running the application is in the same order as it was recorded in the cache. 

### Benchmarking our application with Application CDS

Now that we have created the Application CDS shared cache, as before, we can benchmark our application with 10 consecutive runs while using Application CDS (NOTE: Although it is possible to use both CDS and App CDS, we will explicitly tell Java not to use CDS as it didn't appear to have any real benefit for our application):

On Linux/Mac using zsh:

```bash
time (repeat 10 { \
      java -Xshare:off \
           -XX:+UseAppCDS \
           -XX:SharedArchiveFile=app-cds.jsa \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit \
})
```

Or on Linux/Mac using bash:

```bash
time for i in {1..10}; do \
      java -Xshare:off \
           -XX:+UseAppCDS \
           -XX:SharedArchiveFile=app-cds.jsa \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit; \
done
```

Or on Windows using PowerShell:

```bash
Measure-Command { foreach ($j in 1..10) {
      java -Xshare:off `
           -XX:+UseAppCDS `
           -XX:SharedArchiveFile=app-cds.jsa `
           -jar java-containers101-1.0-SNAPSHOT.jar --exit
}}
```

We should now see that application does perform slightly better. I was able to shave of 5-6 seconds off the user time for 10 consecutive runs which is about 500-600ms of CPU time every time we start-up our application. That's not bad but surely we can do better.

## AOT Compilation

[Ahead of Time compilation](http://openjdk.java.net/jeps/295) allows us to compile a list of Java clases from byte code to native byte code again resulting in quicker start-up times. This works by using a new tool `jaotc` which is available in Java since JDK 9. Similar to App CDS, we will run our application to get a list of classes that our JAR uses during startup. We will then format the list and pass it to the `jaotc` in order to generate a cache that the JVM will use anytime anyone of the classes in the cache is called at startup. 

Unfortunately, this feature is currently limited to Linux x64 systems running 64-bit Java so to all you Windows users, you won't be able to do this on your machines. It is also important to note that this feature is still in the experimental phase so bare this in mind when using it.

### Generate a list of classes (Linux/Mac only)

Before, we generated the list containing classes we were interested using the application JAR, but this will not work with the `jaotc` as Spring boot packages JARs in a way that some classes won't be properly resolved. To work around this, we will need to supply an explicit classpath to find all the classes we're interested in.

First of all, we will need to `install` the application and all it's dependencies. Within the `java-containers101/docker` directory, I have included the Maven binary so you can just run:

```bash
apache-maven-3.5.4/bin/mvn install -f ../pom.xml
```

Next we need to get a list of all the dependencies our Spring Boot application uses. To do this, we can use the [Spring Boot Thin Jar Launcher](https://mvnrepository.com/artifact/org.springframework.boot.experimental/spring-boot-thin-wrapper/1.0.9.RELEASE) which I have also included in the `java-containers101/docker` directory. We will generate the list of our application's dependencies and store it in the variable `CP` with the commmand:

```java
CP=`java -jar spring-boot-thin-wrapper-1.0.9.RELEASE.jar --thin.archive=../ --thin.classpath`
```

Once the dependencies have installed, we can now create the list of classes from all our application's dependencies with the command:

```java
java -XX:DumpLoadedClassList=classes.lst  \
        -cp ../target/classes:$CP com.ibm.code.java.App --exit
```

In this command, we tell java to use the classpath of our application's dependencies along with the `../target/classes` which is where the classes for the code I wrote actually lives. We then specify the main method which is `com.ibm.code.java.App` and the `--exit` flag to stop running immediately after starting-up as we did when we ran with the JAR. As we do this, Java will dump all the classes we use in the file `classes.lst`.

### Generate the AOT cache (Linux/Mac only)

Now that we have the list of classes we want to use, we can pass this list to `jaotc` to generate the AOT shared cache. The `jaotc` tool works as described below:

```java
jaotc <options> <name or list>
```

Where:
* `name` is class name or jar file
* `list` is a : separated list of class names, modules, jar files or directories which contain class files

In our case, run the following command:

```java
jaotc --info \
      --output lib.so \
      -J-cp -Jjava-containers101-1.0-SNAPSHOT.jar:$CP \
      `cat classes.lst | sed -e 's,/,.,g'`
```

We specify the `--info` flag so that we can see what `jaotc` is doing when we run this command. The AOT cache will be produced in a file called `lib.so`. As we want to run `jaotc` with a classpath, we have to send these paramaters directly to the JVM. To do this, we prepend `-J` to the arguments passed to the JVM which is the classpath flag `-cp` and the value supplied to it `java-containers101-1.0-SNAPSHOT.jar:$CP`. Finally we supply the list of class names and we use sed to replace all slashes with periods.

### Benchmarking our application with AOT

Congratulations, you have completed Lab 2! Feel free to go [back to the menu](../README.md) to choose another lab.
