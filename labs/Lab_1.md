# Lab 1: Choosing the right base image

When creating a Java container, quite a bit of consideration should go into building the Docker image. We know that Java requires a JVM to run Java applications so whatever Linux distribution we end up using, it must contain the JVM. In Docker we have the concept of the Dockerfile which we use to create our Docker images. Consequently, as we experiment with different base images (that will package our JVM in our image), our Dockerfile will slighlty change. As with all things programming, the answer to what is the _right_ base image is "it depends":

## JDK versions

In terms of Java versions, there are many options. Most Java applications currently use Java 8, however 9, 10 and the early release of 11 are also viable options with native support for containers built into Java from JDK 10. At the same time, with [Java's new 6 month release cadence for Java](https://blogs.oracle.com/java-platform-group/update-and-faq-on-the-java-se-release-cadence), Oracle recommend not using the older versions once the newer versions are avialable and will stop supporting these versions once the newer versions GA. This means as of now, there is no incentive for anyone to use Java 9 as 10 is available and when 11 GAs it will be the same for 10. This obviouly concerns businesses with large legacy systems and may put people off from using anything but Java 8 until they are forced to. 

## OpenJDK vs Oracle

There is also the argument about the licensing around Oracle build of Java. By using an Oracle build of Java means that you accept their licensing agreement to not re-distribute the JDK which includes pushing your built images that contains the Oracle JDK to a private/public repository. Nevertheless, it is possible to redistribute the Dockerfiles which use the Oracle JDK but most customers will want the built end product i.e. the image. In this case, you would choose OpenJDK builds over Oracle builds. 

## Full JDK vs. JRE vs. Streamlined variants 

The JDK contains the JRE and development tools to actually develop Java programs. In most cases you don't want to develop Java within a container, you simply want to run the end product (JAR / WAR) file. In this case, why choose a full JDK over a JRE? Choosing a full JDK will just mean that you will have a bigger image size, so it will take longer to build, pull and update your images. There has been additional work to make Java run on smaller linux distributions such as Alpine Linux to further decrease the image size and other slim variants (all available OpenJDK tags for different versions can be found [here](https://hub.docker.com/_/openjdk/))

## Building Docker Containers

Well that's enough chat, this is a workshop after all! Let's have a little play with the awesome Spring Boot web application   set up for this workshop with two simple routes: `/ping` and `/ping/{delay_in_ms}`. If you haven't already cloned the repo, do so and change directory to:

```
java-containers101/docker
``` 

In this directory, we have the spring boot application jar and a list of directories containing a Dockerfile. Each Dockerfile will build a Docker image with a slightly different Java distribution as the base image to run our application. The directory names are named such that you can identify what flavour of Java will be built i.e. version, JDK, JRE etc.

First of all, ensure that Docker is running:

```
docker images
```

If it's running you should either see a list of all images you have installed in Docker (which can be empty if you have no images). Once you have ensured that Docker is running you may proceed to building a Docker image from any of the Dockerfile in the current directory.

To build a Docker image from any of these distributions from this directory run:

```
docker build -t java-container:<openjdk-distribution> -f <openjdk-distribution>/Dockerfile .
```

For example, to build the application with `openjdk-8-jre-alpine`, run:

```
docker build -t java-container:openjdk-8-jre-alpine -f openjdk-8-jre-alpine/Dockerfile .
```

Unless you really want to, I'll save you some time and show you what image sizes you should expect for each of the `java-container` images and the `openjdk` images:

```
$ docker images

REPOSITORY          TAG                    IMAGE ID            CREATED             SIZE
java-container      openjdk-8-jdk-slim     e0ccf3a5f937        8 minutes ago       260MB
java-container      openjdk-8-jdk          8cba3e53ba70        9 minutes ago       640MB
java-container      openjdk-8-jre-alpine   58b18734a9f9        13 minutes ago      99.1MB
java-container      openjdk-8-jre-slim     b5ef03ed2047        14 minutes ago      220MB
java-container      openjdk-11-jre-slim    7bf1eb0dd942        15 minutes ago      295MB
java-container      openjdk-10-jre-slim    4cd9988db5f9        15 minutes ago      307MB
openjdk             10-jre-slim            b4e0d5346a45        2 days ago          291MB
openjdk             11-jre-slim            64f969436dfe        2 days ago          279MB
openjdk             8-jre-slim             578940672555        6 days ago          204MB
openjdk             8-jdk-slim             60575d4bfe64        6 days ago          244MB
openjdk             8-jdk                  8c80ddf988c8        6 days ago          624MB
openjdk             8-jre-alpine           ccfb0c83b2fe        12 days ago         83MB
```

For the sake of time, I have only compared the image sizes with JDK variants with Java 8 but for all versions it holds true that for image sizes JDK > JDK Slim > JRE > JRE Slim. Also comparing the `java-container` with the image size for its corresponding openJDK version, the overhead of the actual application is constant. In this case, it is always 16MB. 

Let's build the images for `openjdk-8-jre-alpine` and `openjdk-8-jdk` being the largest and smallest images:

For `openjdk-8-jre-alpine` run:

```
docker build -t java-container:openjdk-8-jre-alpine -f openjdk-8-jre-alpine/Dockerfile .
```

For `openjdk-8-jdk` run:

```
docker build -t java-container:openjdk-8-jdk -f openjdk-8-jdk/Dockerfile .
```

## Running our Java Container 

Now that we have the 2 extremes of our java-container application (openjdk-8-jre-alpine and openjdk-8-jdk), we can test if our application runs:

Assuming that port 8080 is free on your machine, run:

```
docker run -p 8080:8080 --rm java-container:openjdk-8-jdk
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
docker run -p 8080:8080 --rm java-container:openjdk-8-jre-alpine
```

Again, on your browser go to:

```
localhost:8080/ping
```

Or in a new shell/tab/command prompt:

```
ping localhost:8080/ping
```

So from the assumption of just running our Java applications, there is no need to have such a big distribution. In fact in the Serverless world, Java applications are pretty much ignored as a use case because of the large overhead. For now, Java 8 JRE alpine remains a very attractive distribution but we have to remember that this will eventually lose support. 

Congratulations, you have completed Lab 1! Feel free to go [back to the menu](../README.md) to choose another lab.
