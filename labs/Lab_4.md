# Lab 4: Setting container resource constraints

As we know, to make use of all the features we used in [Lab 2](./Lab_2.md) and [Lab 3](./Lab_3.md), we would need a Java version greater than 9. However the reality is most people still use Java 8 in practice and with [Open J9](https://www.eclipse.org/openj9/) which boasts significant performance enhancements to Java 8, it may make a lot more sense to stay on Java for the time being. This lab however, will explain why until recently, running Java 8 within a container was still problematic when considering resource constraints:

* [Creating a new Docker Image for memory tests](./#Creating-a-new-docker-image-for-memory-tests)
* [Running with memory constraints](./#running-with-memory-constraints)
* [Java SE support for Docker CPU and memory limits](./#running-with-memory-constraints)

## Creating a new Docker Image for memory tests 

Let's create a new Docker Image using our application JAR with the smallest Java image according to our investigations in [Lab 1](./Lab_1.md). As we're now using Java 8, we can't use our optimisations from [Lab 2](./Lab_2.md) and [Lab 3](./Lab_3.md) so there is no need to add the extra arguments and no need to copy in the shared caches. Let's also pass the environment variable `$JVM_OPTS` to the java command in case we need to pass extra information to the JVM (\*hint\* \*hint\*). Next, let's add the argument `--memory-test` to tell our Java application to use our [memory test configuration](../src/main/java/com/ibm/code/java/App.java#L25) when it starts up. Finally, let's set the environment variable `LOG_LEVEL=OFF` in our Dockerfile to turn off all logs except our application logs. 

To do all this, as before, from `java-containers101/docker` create a new directory called:

```
openjdk-8-jre-alpine-mem
```

Change directory into `openjdk-8-jre-alpine-mem` and create a file called `Dockerfile` with the following contents:

```
FROM openjdk:8-jre-alpine
EXPOSE 8080
ADD java-containers101-1.0-SNAPSHOT.jar /
ENV LOG_LEVEL=OFF
CMD java $JVM_OPTS -Xshare:off -jar java-containers101-1.0-SNAPSHOT.jar --memory-test
```

As we want to be able to resolve `$JVM_OPTS`, we can not use the exec form (string array form) of `CMD`. Instead we use the shell form so we invoke a command shell and then `$JVM_OPTS` will substiute for whatever is passed to it. 

Now change directory back to `java-containers101/docker` and run:

```
docker build -t java-container:openjdk-8-jre-alpine-mem -f openjdk-8-jre-alpine-mem/Dockerfile .
```

## Running with memory constraints

To explain the `--memory-test` configuration in more detail, when we run with this configuration, the inital memory, maximum memory and allocatable memory available to the java process will be printed out. In this mode, the container is set to [only allocate up to 75% of the initial free memory](../src/main/java/com/ibm/code/java/App.java#L47) and will continually eat up 1MB of memory until it reaches the the allocatable memory limit. At this point, the program will print out the free memory within the container (there should always be some as we only use 75% of the free memory available when starting the process).

In Docker, it is possible to [set the maximum memory](https://docs.docker.com/engine/reference/run/#user-memory-constraints) that a container can use with the flag `-m` or `--memory`.

Combining Docker's memory limit and this congiuration, we will see how Java _respects_ Docker's memory limits.

Let's run our Dockerized Spring Boot application limiting the memory to "256MB" and see the output:

```
docker run -it -p 8080:8080 --memory=256MB --name=memory-test java-container:openjdk-8-jre-alpine-mem
```

There are several things wrong with the output we get from this command. Firstly, we can see that the maximum memory available to the container is greater than 256MB and the application seems to think it has way more free memory than it should. Even though the memory that we can allocate to the container is 75% of the initial memory, as the container thinks it has access to more free memory than it should, the allocatable memory is still wrong. In fact, the container thinks it has access to the all the memory available to the host! Also, we didn't `CTRL + C` to detach from the container like before...

Let's run see what the status of the container is:

```
docker ps -a
```

So Docker just seemed to exit out of container... Since we specified the name of the container with `--name=memory-test` and didn't run with `--rm` we can still inspect the properties of the container in it's exited state with the reference `memory-test`:

```
docker inspect --format '{{json .State }}' memory-test
```

We can actually see that the container was `OOMKilled`. As detailed in the [Docker docs](https://docs.docker.com/config/containers/resource_constraints/#memory), when a container runs out of memory specified by `--memory`, the kernel kills processes in a container which would inevitably result in Docker `OOMKilling` containers in an attempt to stop Docker and more importantly your machine from freezing if containers caused all memory on a VM to be used up. So while our Java process is completely unaware of any memory limits, Docker sees that our container sees that our container has exceeded the 256MB limit and kills our container as it thinks it is out of memory. 

This happens because process isolation is based on cgroups while tools in Linux which get information about the available resources on a machine like `free` and `top` and are based on the `/proc` filesystem were implemented way before cgroups. As a result, they are therefore not _cgroups-aware_. The JVM suffers from the exact same issue when you specify memory constraints and it instead sees the all the available memory on the host. 

The only way to get around this is to actually tell the JVM exactly how much memory it can use with the `-Xmx` variable which by default will [constrain the JVM heap](https://stackoverflow.com/questions/4667483/how-is-the-default-java-heap-size-determined). 

Let's remove the dead container:

```
docker rm memory-test
```

Now, let's run the container again but this time set the `-Xmx` JVM argument to the same value as our Docker memory limit:

```
docker run -it -p 8080:8080 -e JVM_OPTS="-Xmx256M" --memory=256MB --name=memory-test java-container:openjdk-8-jre-alpine-mem
```

Now we actually see that the JVM respects the limits and our application can actually complete so we see the free memory available. We can now end the process with the keys `CTRL + C` and remove the container:

```
docker rm memory-test
```

This is annoying though because we have to define the memory limits in 2 places. It would be great if the JVM could just pick up the Docker limits...

## Java SE support for Docker CPU and memory limits

Since [Java 9](https://blogs.oracle.com/java-platform-group/java-se-support-for-docker-cpu-and-memory-limits) there is now native support for CPU and memory limits. The changes were also backported to Java 8 and they are available to use as experimental features. 

Let's see how we can make use of these new arguments:

```
docker run -it -p 8080:8080 -e JVM_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -XX:MaxRAMFraction=1" --memory=256MB --name=memory-test java-container:openjdk-8-jre-alpine-mem
```

Again, we can see that our resource constaints are respected but let's take a look at what arguments we are passing through:
* `-XX:+UnlockExperimentalVMOptions` - Tells the JVM to use the backported features
* `-XX:+UseCGroupMemoryLimitForHeap` - Tells the JVM to use the Docker memory limit
* `-XX:MaxRAMFraction=1` - Tells the JVM to use all of the available memory as the max heap size

Let's clean up after ourselves:

```
docker rm memory-test
```

Before this change in Java, it was very impractical to run Docker within a container. Imagine not having mutiple containers all on one host thinking they have access to all the host's resources! If they did actually use a lot of memory, you would enter a world of pain as your containers would all fight for memory and to survive. 

Regardless, I hope I have showed you some of the considerations and thought that goes into running Java applciations within containers.

Congratulations, you have completed the workshop! Feel free to go [back to the menu](../README.md) to revist any of the labs.
