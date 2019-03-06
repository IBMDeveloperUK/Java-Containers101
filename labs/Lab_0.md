# Lab 0: Prerequisites

To run the labs, there are several requirements:
1. [Docker](#install-docker)
2. [Java 11](#install-java-11)

## Install Docker

Before you start running any of the exercises, make sure you have downloaded Docker CE from the [Docker store](https://store.docker.com/search?type=edition&offering=community). Docker is the container engine that we will use to deploy our Java containers providing a comprehensive ecosystem around manipulating containers (run, commit, start, stop, delete, pull, push). 

*Note:* you will be prompted to create an account on the Docker store if you don't already have one.

### Docker CE for Windowas

If you are using Windows, once you have installed Docker, you will need to ensure that you [switch to Linux Containers](https://docs.docker.com/docker-for-windows/#switch-between-windows-and-linux-containers).

### Docker Toolbox for Windows 7/8 users

Docker CE is only compatible with Microsoft Windows 10 Professional or Enterprise 64-bit. For people running other versions of Windows, you will need to install [Docker Toolbox](https://docs.docker.com/toolbox/toolbox_install_windows/).

## Install Java 11

As we will be playing around with some of the newer features only available in the more recent versions of Java, we will be using AdoptOpenJDK 11. We have also chosen version 11 as it is a version of Java that has been marked for lonf term support so even with the new 6 months cadence for Java releases, it will be supported and recieve updates over older versions (excluding 8). If you don't already have this installed, you can install the [AdtoptOpenJDK 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) from their website.

### Configuring Java 11:

Once installed, ensure that the `JAVA_HOME` environment variable points to this version of Java. On Mac or Linux, you can do this by updating your profile (which can be any of the following files depending on the shell you use: `~/.bashrc`, `~/.bash_profile`, `~/.zshrc`). This will ensure that every time you open a new shell, you will be ready to use this version of Java. Alternatively, you can run these commands directly in the shell. Following the examples above:

On Mac you would add:

```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/JAVA_VERSION/Contents/Home
export PATH=$JAVA_HOME/bin/:$PATH
```

On Linux you would add:

```
export JAVA_HOME=/usr/lib/jvm/JAVA_VERSION
export PATH=$JAVA_HOME/bin/:$PATH
```

On Windows, similarly, you will also have to configure `JAVA_HOME` and `PATH` to point to openJDK 10. The best way to do this is to set them up as environment variables where:

```
JAVA_HOME=C:\Program Files\Java\JAVA_VERSION
PATH=C:\Program Files\Java\JAVA_VERSION\bin;%PATH%
```

If `JAVA_HOME` exists, update it with the new full path to openJDK 10 as shown above. As `PATH` is likely to already exist, simply prepend the full path to `JAVA_HOME\bin` to whatever `PATH` is currently as shown above.

In all cases, you should verify that running `java --version` outputs:

```bash
$ java --version

openjdk 11.0.2 2019-01-15
OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.2+9)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.2+9, mixed mode, sharing)
```

Now you have all the prerequisites you can go back to the [menu](../README.md) and start a lab!
