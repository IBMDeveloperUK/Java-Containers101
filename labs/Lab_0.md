# Lab 0: Prerequisites

To run the labs, there are several requirements:
1. [Docker](#install-docker)
2. [Java 10](#install-java-10)

## Install Docker

Before you start running any of the exercises, make sure you have downloaded Docker CE from the [Docker store](https://store.docker.com/search?type=edition&offering=community). Docker is the container engine that we will use to deploy our Java containers providing a comprehensive ecosystem around manipulating containers (run, commit, start, stop, delete, pull, push). 

*Note:* you will be prompted to create an account on the Docker store if you don't already have one.

### Docker CE for Windowas

If you are using Windows, once you have installed Docker, you will need to ensure that you [switch to Linux Containers](https://docs.docker.com/docker-for-windows/#switch-between-windows-and-linux-containers).

### Docker Toolbox for Windows 7/8 users

Docker CE is only compatible with Microsoft Windows 10 Professional or Enterprise 64-bit. For people running other versions of Windows, you will need to install [Docker Toolbox](https://docs.docker.com/toolbox/toolbox_install_windows/).

## Install Java 10

As we will be playing around with some of the newer features only available in the more recent versions of Java, we will be using openJDK 10. If you don't already have this installed, you can install the [openJDK 10](http://jdk.java.net/10/) binaries  from the Oracle Website.

### Configuring Java 10

## Mac

Once you have installed the binary file, you can extract the contents in the JVM directory, e.g.

```
/Library/Java/JavaVirtualMachines/jdk-10.0.2.jdk
```

Then you can use the `java_home` tool to let your machine use this version of Java instead of your current version. (You can use this same tool to switch back):

```
/usr/libexec/java_home -v 10.0.2
```

The output should confirm that you are now using this new version of Java.


