# Lab 3: Fine-tuning Java applications pt2

In [Lab 2](./Lab_2.md), we covered CDS and Application CDS as two ways to make our Java applications more performant, but we found that we received no benefit from CDS and not a lot of benefit from Application CDS. In this lab we will look at two other ways of increasing performance before finally making these changes back to our container. 

* [AOT Compilation](#aot-compilation)
* [Open J9](#openj9)
* [jLink](#jlink)
* [Updating the Docker Image](#updating-the-docker-image)

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

In this command, we tell java to use the classpath of our application's dependencies along with the directory `../target/classes` which is where the classes for the application code are. We then specify the main method which is `com.ibm.code.java.App` and the `--exit` flag to stop running immediately after starting-up as we did when we ran with the JAR. As we run the application, Java will dump all the classes we use in the file `classes.lst`.

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

### Benchmarking our application with AOT and App CDS (Linux/Mac only)

Now that we have created the AOT shared cache, as before, we can benchmark our application with 10 consecutive runs while using AOT. As Application CDS provided some benefit, we will also use it but we will still turn off CDS:

Using zsh:

```bash
time (repeat 10 { \
      java -Xshare:off \
           -XX:+UseAppCDS \
           -XX:SharedArchiveFile=app-cds.jsa \
           -XX:AOTLibrary=./lib.so \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit \
})
```

Using bash:

```bash
time for i in {1..10}; do \
      java -Xshare:off \
           -XX:+UseAppCDS \
           -XX:SharedArchiveFile=app-cds.jsa \
           -XX:AOTLibrary=./lib.so \
           -jar java-containers101-1.0-SNAPSHOT.jar --exit; \
done
```

With luck, you will be able to see a drastic increase in start-up time. ~~It was possible for me to get 50-60s seconds of CPU time shaved off or an average of 5-6s of CPU time every time we start the application using both AppCDS and AOT~~. Now that Java 11 has been released using AOT only seemed to have around a ~150ms improvement over not using it. Regardless of performance, the trade-off is that we now have a cache of about 150MB which we would have to load and store whenever we deploy our application. One important takeaway from this lab is that every application is different and depending on the number and type of dependencies you use, the combination of these different features you decide to use will vary according to how well they perform and the cost of using them. If you're more concerned about image sizes, you will be more inclined to not use AOT to avoid the 150MB overhead it adds to the image.

## Open J9

[Open J9](https://www.eclipse.org/openj9/) is a project that was contributed by IBM to the Eclipse Foundation in 2017 based of the alternative proprietary JVM V9. This alternative JVM boasts “low memory footprint” & “fast start-up time” with its own CDS and AOT implementations also included. 

AdoptOpenJDK also provide these builds as Docker images for both versions 8 and 11 and with the same variants as we saw before (jdk, slim, alpine, alpine-slim).

To make use of it's CDS and AOT functions, unlike the HotSpot VMs, we don't have to create cache's. The JVM automatically chooses the classes to optimize which is a huge benefit for keeping the image size down. To test how this performs, you will  first need to download and install the [OpenJ9 variant](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=openj9) of AdoptopenJDK.

```
openjdk version "1.8.0_202"
OpenJDK Runtime Environment (build 1.8.0_202-b08)
Eclipse OpenJ9 VM (build openj9-0.12.1, JRE 1.8.0 Mac OS X amd64-64-Bit Compressed References 20190205_147 (JIT enabled, AOT enabled)
OpenJ9   - 90dd8cb40
OMR      - d2f4534b
JCL      - d002501a90 based on jdk8u202-b08)
```

The Java 8 variant of OpenJ9 is esteemed to be more performant than Java 11 so we have chosen this version. You can now run the jar with the following flags:

```
java -Xshareclasses -Xtune:virtualized -jar java-containers101-1.0-SNAPSHOT.jar
```

The `-Xshareclasses` flag allows us to make use of OpenJ9's CDS implementation while `-Xtune:virtualized` allows us to use it's AOT features. Note how with both options we do not have to create or specify a cache which means our resultant images are a lot smaller in size!

When coming to test these features, I was able to shave of an entire second from the applications start-up time!

## jLink

Another optimistion that we could use to solve the issue of a bloated image size would be to use `jlink` (a feature available since Java 9) to strip the JDK of everything that our application will not use. This relies on the application using the modular system that came with Java 9 or to use the `jdeps` tool and convert automatic dependencies that Spring Boot uses to explicit ones. Either way, this is a task that deserves it's very own workshop!

## Updating the Docker Image

As we have made all these optimizations to our Java application, we would need to update the Dockerfile to take advantage of these changes. We now run our application with App CDS and AOT so we would need to add these files to the container. The arguments supplied to run our application would also need to be updated. 

As Windows users couldn't use the AOT features and as the AOT shared cache is platform dependent, only Linux users will be able to rebuild the image and successfully run a container using the cache. If you're a Linux user you can follow the [next sub-section](#rebuilding-the-docker-image) to rebuild the image using these new features, otherwise skip this subsection and the [sub-section after](#pulling-the-docker-image) will give steps to pull the image from Docker Hub.

### Rebuilding the Docker Image

Let's start off by creating a new directory in `java-containers101/docker` called:

```
aadoptopenjdk-8-openj9-alpine-slim-opt
```

Change directory into `adoptopenjdk-8-openj9-alpine-slim-opt` and create a file called `Dockerfile` with the following contents:

```
FROM adoptopenjdk/openjdk8-openj9:alpine-slim
EXPOSE 8080
ADD java-containers101-1.0-SNAPSHOT.jar /
CMD ["java", "-Xshareclasses", "-Xtune:virtualized", "-jar", "java-containers101-1.0-SNAPSHOT.jar"]
```

Now change directory back to `java-containers101/docker` and run:

```
docker build -t java-container:adoptopenjdk-8-alpine-slim-opt -f adoptopenjdk-8-alpine-slim-opt/Dockerfile .
```

As before, we can now run our application with the command:

```
docker run -p 8080:8080 --rm java-container:adoptopenjdk-8-openj9-alpine-slim-opt
```

Using a browser or in the terminal, we should get the response `pong` when we go `ping` the address:

```
localhost:8080/ping
```

Congratulations, you have completed Lab 3! Move on to [Lab 4](./Lab_4.md) or go [back to the menu](../README.md).
