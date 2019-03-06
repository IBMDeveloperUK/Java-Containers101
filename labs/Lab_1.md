# Lab 1: Choosing the right base image

When creating a Java container, quite a bit of consideration should go into building the Docker image. We know that Java requires a JVM to run Java applications so whatever Linux distribution we end up using, it must contain the JVM. In Docker we have the concept of the Dockerfile which we use to create our Docker images. Consequently, as we experiment with different base images (that will package our JVM in our image), our Dockerfile will slighlty change. As with all things programming, the answer to what is the _right_ base image is "it depends":

## JDK versions

In terms of Java versions, it seems to be unclear what version of Java to pick with the [6 month release cadence for Java](https://blogs.oracle.com/java-platform-group/update-and-faq-on-the-java-se-release-cadence), Oracle recommend not using the older versions once the newer versions are avialable and will stop supporting these versions once the newer versions GA. This means as of now, there is no incentive for anyone to use Java 9 or 10. Version 8 and 11 are exceptions to this rule as they have been marked for Long Term Support and different vendors will agree to support these versions beyond Oracle's 6 month support period. 

## Free as in beer vs. Free as in speech

There is also the argument about the licensing around Oracle build of Java. Oracle licenses Java under several license. Although free to use for development purposes (free as in beer), you are not free to redistribute the Oracle JDK, which makes even things like storing custom built images in private/public repository. Other vendors however such as AdoptOpenJDK allow you to use their software and do whatever you wish (free as in speech). 

## Full JDK vs. Streamlined variants 

The JDK contains the JRE and development tools to actually develop Java programs. In most cases you don't want to develop Java within a container, you simply want to run the end product (JAR / WAR) file. In this case, why choose a full JDK over a more streamlined version to just run your application? Choosing a full JDK will just mean that you will have a bigger image size, so it will take longer to build, pull and update your images. There has been additional work to make Java run on smaller linux distributions such as Alpine Linux to further decrease the image size and other slim variants (all available AdoptOpenJDK tags for different versions can be found [here](https://hub.docker.com/r/adoptopenjdk/openjdk11))

## Building Docker Containers

Well that's enough chat, this is a workshop after all! Let's have a little play with the awesome Spring Boot web application   set up for this workshop with [a simple route](../src/main/java/com/ibm/code/java/ping/PingController.java): `/ping`. If you haven't already cloned the repo, do so and change directory to:

```
java-containers101/docker
``` 

In this directory, we have the spring boot application jar and a list of directories containing a Dockerfile. Each Dockerfile will build a Docker image with a slightly different Java distribution as the base image to run our application. The directory names are named such that you can identify what variant of Java will be built.

First of all, ensure that Docker is running:

```
docker images
```

If it's running you should either see a list of all images you have installed in Docker (which can be empty if you have no images). Once you have ensured that Docker is running you may proceed to building a Docker image from any of the Dockerfile in the current directory.

To build a Docker image from any of these distributions from this directory run:

```
docker build -t java-container:<adopt-openjdk-distribution> -f <adopt-openjdk-distribution>/Dockerfile .
```

For example, to build the application with `adoptopenjdk-8-alpine-slim`, run:

```
docker build -t java-container:adoptopenjdk-8-alpine-slim -f adoptopenjdk-8-alpine-slim/Dockerfile .
```

Unless you really want to, I'll save you some time and show you what image sizes you should expect for each of the `java-container` images and the `adoptopenjdk` images:

```
$ docker images

REPOSITORY          TAG                    IMAGE ID            CREATED             SIZE
adoptopenjdk/openjdk8-openj9    alpine-slim                          253e3e59f633        42 hours ago        84.8MB
java-container                  adoptopenjdk-11-openj9-alpine-slim   b48ab042240c        44 hours ago        259MB
adoptopenjdk/openjdk11-openj9   alpine-slim                          52ca855326f8        2 days ago          243MB
java-container                  adoptopenjdk-8-slim                  00ebc71a5514        5 days ago          211MB
java-container                  adoptopenjdk-8-alpine-slim           ce0969af8311        5 days ago          87MB
java-container                  adoptopenjdk-8-alpine                7051f904cc54        5 days ago          234MB
java-container                  adoptopenjdk-8-jdk                   b110eb9af0ce        5 days ago          342MB
java-container                  adoptopenjdk-11-jdk                  2bb67cfef0cd        5 days ago          458MB
java-container                  adoptopenjdk-11-slim                 f352ceb124f4        5 days ago          383MB
java-container                  adoptopenjdk-11-alpine-slim          c6189cb8b7b9        5 days ago          259MB
java-container                  adoptopenjdk-11-alpine               48b8442e7df1        5 days ago          350MB
adoptopenjdk/openjdk11          alpine-slim                          3b277a441622        6 days ago          243MB
adoptopenjdk/openjdk11          alpine                               46fe565525a9        6 days ago          334MB
adoptopenjdk/openjdk11          slim                                 027ff41ec7af        6 days ago          367MB
adoptopenjdk/openjdk11          latest                               0c05f58fcc03        6 days ago          442MB
adoptopenjdk/openjdk8           alpine-slim                          48ba92fa4ad0        6 days ago          70.8MB
adoptopenjdk/openjdk8           alpine                               206aaaea0bff        6 days ago          218MB
adoptopenjdk/openjdk8           slim                                 e6851f197559        6 days ago          195MB
adoptopenjdk/openjdk8           latest                               e78f1e7bf68c        6 days ago          326MB
```

For the sake of time, I have only compared the image sizes with JDK variants with Java 8 but for all versions it holds true that for image sizes jdk > slim > alpine > alpine-slim. Also comparing the `java-container` with the image size for its corresponding AdoptOpenJDK version, the overhead of the actual application is constant. In this case, it is always 16MB. 

Let's build the images for `adoptopenjdk-8-alpine-slim` and `adoptopenjdk-11-jdk` being the smallest and largest images:

For `adoptopenjdk-8-alpine-slim` run:

```
docker build -t java-container:adoptopenjdk-8-alpine-slim -f adoptopenjdk-8-alpine-slim/Dockerfile .
```

For `adoptopenjdk-11-jdk ` run:

```
docker build -t java-container:adoptopenjdk-11-jdk  -f adoptopenjdk-11-jdk/Dockerfile .
```

## Running our Java Container 

Now that we have the 2 extremes of our java-container application (adoptopenjdk-8-alpine-slim and adoptopenjdk-11-jdk), we can test if our application runs:

Assuming that port 8080 is free on your machine, run:

```
docker run -p 8080:8080 --rm java-container:adoptopenjdk-11-jdk
```

If the Spring logs print to the terminal, then you should be able to reach the application on `localhost:8080`. 

On your browser go to:

```
localhost:8080/ping
```

Or in a new shell/tab/command prompt:

```
ping localhost:8080/ping
```

Now lets test to see how the other image performs. To exit the container press the keys: 

```
Ctrl + c
```

As we specified `--rm` as a argument to the docker daemon, when we stop the container process, Docker will automatically remove the container so this will clean up after us automatically.

Now let's run the smaller version of the application:

```
docker run -p 8080:8080 --rm java-container:adoptopenjdk-8-alpine-slim
```

Again, on your browser go to:

```
localhost:8080/ping
```

Or in a new shell/tab/command prompt:

```
ping localhost:8080/ping
```

So from the assumption that we only need to have a runtime for our Java applications, there is no need to have such a big distribution.

Congratulations, you have completed Lab 1! Move on to [Lab 2](./Lab_2.md) or go [back to the menu](../README.md).
