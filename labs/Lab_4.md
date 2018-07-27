# Lab 4: Setting container resource constraints

As we know, to make use of all the features we used in [Lab 2](./Lab_2.md) and [Lab 3](./Lab_3.md), we would need a Java version greater than 9. However the reality is most people still use Java 8 in practice and with [Open J9](https://www.eclipse.org/openj9/) which boasts significant performance enhancements to Java 8, it may make a lot more sense to stay on Java for the time being. This lab however, will explain why until recently, running Java 8 within a container was still problematic when considering resource constraints.

## Creating a new Docker Image 

Let's create a new Docker Image using our application JAR with the smallest Java image according to our investigations in [Lab 1](./Lab_1.md). As we're now using Java 8, we can't use our optimisations from [Lab 2](./Lab_2.md) and [Lab 3](./Lab_3.md) so there is no need to add the extra arguments and no need to copy in the shared caches. Finally, let's add the argument `--memory-test` to tell our Java application to use our [memory test configuration](../src/main/java/com/ibm/code/java/App.java#L26) when it starts up.

Congratulations, you have completed the workshop! Feel free to go [back to the menu](../README.md) to revist any of the labs.
