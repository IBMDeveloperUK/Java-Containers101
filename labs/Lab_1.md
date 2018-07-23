# Lab 1: Choosing the right base image

When creating a Java container, quite a bit of consideration should go into building the Docker image. We know that Java requires a JVM to run Java applications so whatever Linux distribution we end up using, it must contain the JVM. In Docker we have the concept of the Dockerfile which we use to create our Docker images. Consequently, as we experiment with different base images (that will package our JVM in our image), our Dockerfile will slighlty change. 

## JDK versions

In terms of Java versions, there are many options. Most Java applications currently use Java 8, however 9, 10 and the early release of 11 are also viable options with native support for containers built into Java from JDK 10. At the same time, with [Java's new 6 month release cadence for Java](https://blogs.oracle.com/java-platform-group/update-and-faq-on-the-java-se-release-cadence), Oracle recommend not using the older versions once the newer versions are avialable and will stop supporting these versions once the newer versions GA. This means as of now, there is no incentive for anyone to use Java 9 as 10 is available and when 11 GAs it will be the same for 10. This obviouly concerns businesses with large legacy systems and may put people off from using anything but Java 8 until they are forced to. 

## OpenJDK vs Oracle

There is also the argument about the licensing around Oracle build of Java. By using an Oracle build of Java means that you accept their licensing agreement to not re-distribute the JDK which includes pushing your built images that contains the Oracle JDK to a private/public repository. Nevertheless, it is possible to redistribute the Dockerfiles which use the Oracle JDK but most customers will want the built end product i.e. the image. In this case, you would choose OpenJDK builds over Oracle builds. 

## Full JDK vs. JRE vs. Streamlined variants 

The JDK contains the JRE and development tools to actually develop Java programs. In most cases you don't want to develop Java within a container, you simply want to run the end product (JAR / WAR) file. In this case, why choose a full JDK over a JRE? Choosing a full JDK will just mean that you will have a bigger image size, so it will take longer to build, pull and update your images. There has been additional work to make Java run on smaller linux distributions such as Alpine Linux to further decrease the image size and other [slim variants](https://hub.docker.com/_/openjdk/)
