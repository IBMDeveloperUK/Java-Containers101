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

### Configuring Java 10:

Once you have installed the binary file, you can extract the contents to a sensible directory (probably where you have other Java versions installed):

On Mac:

```
/Library/Java/JavaVirtualMachines/jdk-10.0.2.jdk
```

or on Linux:

```
/usr/lib/jvm/jdk-10.0.2
```

or on Windows:

```
C:\Program Files\Java\jdk-10.0.2
```

On Mac or Linux, you can then update your profile (which can be any of the following files depending on your shell: `~/.bashrc`, `~/.bash_profile`, `~/.zshrc`). This will ensure that every time you open a new shell, you will be ready to use this version of Java. Alternatively, you can run these commands directly in the shell. Following the examples above:

On Mac you would add:

```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-10.0.2.jdk/Contents/Home
export PATH=$JAVA_HOME/bin/:$PATH
```

On Linux you would add:

```
export JAVA_HOME=/usr/lib/jvm/jdk-10.0.2
export PATH=$JAVA_HOME/bin/:$PATH
```

On Windows, similarly, you will also have to configure `JAVA_HOME` and `PATH` to point to openJDK 10. The best way to do this is to set them up as environment variables where:

```
JAVA_HOME=C:\Program Files\Java\jdk-10.0.2
PATH=C:\Program Files\Java\jdk-10.0.2\bin;%PATH%
```

If `JAVA_HOME` exists, update it with the new full path to openJDK 10 as shown above. As `PATH` is likely to already exist, simply prepend the full path to `JAVA_HOME\bin` to whatever `PATH` is currently as shown above.

In all cases, you should verify that running `java --version` outputs:

```bash
$ java --version

openjdk 10.0.2 2018-07-17
OpenJDK Runtime Environment 18.3 (build 10.0.2+13)
OpenJDK 64-Bit Server VM 18.3 (build 10.0.2+13, mixed mode)
```

Now you have all the prerequisites you can go back to the [menu](../README.md) and start a lab!
