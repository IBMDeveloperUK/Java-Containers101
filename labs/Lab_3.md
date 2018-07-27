# Lab 3: Fine-tuning Java applications pt2

In [Lab 2](./Lab_2.md), we covered CDS and Application CDS as two ways to make our Java applications more performant, but we found that we received no benefit from CDS and not a lot of benefit from Application CDS. In this lab we will look at two other ways of increasing performance before finally making these changes back to our container. 

* [AOT Compilation](#aot-compilation)
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

With luck, you will be able to see a drastic increase in start-up time. It was possible for me to get 50-60s seconds of CPU time shaved off or an average of 5-6s of CPU time every time we start the application using both AppCDS and AOT. Having said this, the trade-off is that we now have a cache of about 150MB which we would have to load and store whenever we deploy our application. One important takeaway from this lab is that every application is different and depending on the number and type of dependencies you use, the combination of these different features you decide to use will vary according to how well they perform and the cost of using them. Even though our application starts up a lot faster now, if you're more concerned about image sizes, you will be more inclined to not use AOT to save 150MB within the Docker image.

## jLink

Another optimistion that we could use to solve the issue of a bloated image size would be to use `jlink` (a feature available since Java 9) to strip the JDK of everything that our application will not use. This relies on the application using the modular system that came with Java 9 or to use the `jdeps` tool and convert automatic dependencies that Spring Boot uses to explicit ones. Either way, this is a task that deserves it's very own workshop!

## Updating the Docker Image

As we have made all these optimizations to our Java application, we would need to update the Dockerfile to take advantage of these changes. We now run our application with App CDS and AOT so we would need to add these files to the container. The arguments supplied to run our application would also need to be updated. 

As Windows users couldn't use the AOT features and as the AOT shared cache is platform dependent, only Linux users will be able to rebuild the image and successfully run a container using the cache. If you're a Linux user you can follow the [next sub-section](#rebuilding-the-docker-image) to rebuild the image using these new features, otherwise skip this subsection and the [sub-section after](#pulling-the-docker-image) will give steps to pull the image from Docker Hub.

### Rebuilding the Docker Image

Let's start off by creating a new directory in `java-containers101/docker` called:

```
openjdk-10-jre-slim-opt
```

Change directory into `openjdk-10-jre-slim-opt` and create a file called `Dockerfile` with the following contents:

```
FROM openjdk:10-jre-slim
EXPOSE 8080
ADD app-cds.jsa /
ADD lib.so /
ADD java-containers101-1.0-SNAPSHOT.jar /
CMD ["java", "-Xshare:off", "-XX:+UseAppCDS", "-XX:SharedArchiveFile=app-cds.jsa", "-XX:AOTLibrary=./lib.so", "-jar", "java-containers101-1.0-SNAPSHOT.jar"]
```

Now change directory back to `java-containers101/docker` and run:

```
docker build -t java-container:openjdk-10-jre-slim-opt -f openjdk-10-jre-slim-opt/Dockerfile .
```

As before, we can now run our application with the command:

```
docker run -p 8080:8080 --rm java-container:openjdk-10-jre-slim-opt
```

Using a browser or in the terminal, we should get the response `pong` when we go `ping` the address:

```
localhost:8080/ping
```

### Pulling the Docker Image

If you're unable to build the docker image, you can pull it with the latest changes by running:

```
docker pull mofesal/java-container:openjdk-10-jre-slim-opt
```

So that we can remain consistent with the Linux users, let's retag the image:

```
docker tag mofesal/java-container:openjdk-10-jre-slim-opt java-container:openjdk-10-jre-slim-opt
```

We can now run the application with the command:

```
docker run -p 8080:8080 --rm java-container:openjdk-10-jre-slim-opt
```

Using a browser or in the terminal, we should get the response `pong` when we go `ping` the address:

```
localhost:8080/ping
```

Congratulations, you have completed Lab 3! Move on to [Lab 4](./Lab_4.md) or go [back to the menu](../README.md).
