# Lab 2: Fine-tuning Java images

## Class Data Sharing

### Using the shared archive 

The shared archive can be created simply by running the command:
`java -Xshare:dump`

**NOTE: If this command fail on Linux/Mac you may need to run this command with sudo**

This creates the archive in a file called: `classes.jsa`

By default, Java will use CDS as long as the shared archive exists. If not, it will run as normal. To be explicit, we will simply add the following argument to are `java` command:

`-Xshare:on`

Great, we can now add this to our Dockerfile so our Java containers start faster! Well, let's just start off with seeing how much of a difference CDS has made to our Java application with some simple benchmarking: 

### Benchmarking our application

First of all open up a new terminal/console (PowerShell if on Windows).

Now create a new environment variable within the shell that our java application will pick up so we don't get bombarded with logs. (*NOTE: This environment variable will be wiped when you close the shell*): 

`export LOG_LEVEL = OFF`

OR 

`$env:LOG_LEVEL = "OFF"` 

Next, make sure that you are in the `java-containers101/docker` directory. To have an estimate of how long it takes to start-up our application in consecutive runs, we can run our application without CDS 10 times:

On Linux/Mac using zsh:

```
time (repeat 10 {java -Xshare:off -jar java-containers101-1.0-SNAPSHOT.jar --exit})
```

Or on Linux/Mac using bash:

```
time for i in {1..10}; do java -Xshare:off -jar java-containers101-1.0-SNAPSHOT.jar --exit; done
```

Or on Windows using PowerShell:

```
Measure-Command { foreach ($j in 1..10) {java -Xshare:off -jar java-containers101-1.0-SNAPSHOT.jar --exit}}
```

The `--exit` flag is used to shutdown the application as soon as it has finished start-ing up so we don't hang in an interactive shell. This allows us to repeatedly call the application

For Linux/Mac, we are interested in the `user` output as this gives the CPU time in seconds taken to execute the process (If you were to increase/decrease the number of cores the application could use, the value would be similar in consecutive runs). It's a better value than `total` or `real` as it does not count other processes or time spent blocked. For Windows, we just have to rely on the `TotalSeconds` output which will just give us the elapsed time. Be sure to record these stats somewhere!

### Benchmarking our application with CDS

Now we can benchmark our application with CDS. In the same shell we can benchmark our application while making use of the shared archive:

On Linux/Mac using zsh:

```
time (repeat 10 {java -Xshare:on -jar java-containers101-1.0-SNAPSHOT.jar --exit})
```

Or on Linux/Mac using bash:

```
time for i in {1..10}; do java -Xshare:on -jar java-containers101-1.0-SNAPSHOT.jar --exit; done
```

Or on Windows using PowerShell:

```
Measure-Command { foreach ($j in 1..10) {java -Xshare:on -jar java-containers101-1.0-SNAPSHOT.jar --exit}}
```

Of course the number of processes running and the specification of your laptop will affect the benchmark. To make your benchmarking a bit more reliable, make sure you don't open new applications between the two use cases (close uneeded Browser tabs and applications)! You can also run this a few times or increase the value from 10 to get a better idea of the range for each use cases. 

Looking at the results, you may have found that it is not conclusive that CDS is better than not using CDS. As [Oracle](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/class-data-sharing.html) state, *CDS produces better results for smaller applications because it eliminates a fixed cost: that of loading certain core classes. The smaller the application relative to the number of core classes it uses, the larger the saved fraction of startup time.* As our application uses quite a number of external libraries to run Spring Boot, the application size is larger and seems to make using CDS negligible. If only we could have CDS for all these extra external libraries... 

## Application CDS 


Congratulations, you have completed Lab 2! Feel free to go [back to the menu](../README.md) to choose another lab.
