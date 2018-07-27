# Lab 4: Setting container resource constraints

As we know, to make use of all the features we used in [Lab 2](./Lab_2.md) and [Lab 3](./Lab_3.md), we would need a Java version greater than 9. However the reality is most people still use Java 8 in practice and with [Open J9](https://www.eclipse.org/openj9/) which boasts significant performance enhancements to Java 8, it may make a lot more sense to stay on Java for the time being. This lab however, will explain why until recently, running Java 8 within a container was still problematic when considering resource constraints.

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
CMD ["java", "$JVM_OPTS", "-Xshare:off", "-jar", "java-containers101-1.0-SNAPSHOT.jar", "--memory-test"]
```

Now change directory back to `java-containers101/docker` and run:

```
docker build -t java-container:openjdk-8-jre-alpine-mem -f openjdk-8-jre-alpine-mem/Dockerfile .
```

Let's run our application now and see the output:

```
docker run -p 8080:8080 --rm java-container:openjdk-8-jre-alpine-mem
```

Congratulations, you have completed the workshop! Feel free to go [back to the menu](../README.md) to revist any of the labs.
