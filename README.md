# AMQP Spark Streaming demo

This demo shows how it's possible to integrate AMQP based products with Apache Spark Streaming. It uses the AMQP Spark Streaming connector, which is able to get messages from an AMQP source and pushing them to the Spark engine as micro batches for real time analytics.
The other main point is that the used Apache Spark deployment isn't in standalone mode but running on OpenShift.
Finally, an Apache Artemis instance is used as AMQP source/destintation for exchanged messages.

The demo consists of a publisher application which simulates temperature values from a reading sensor and sending them to the `temperature` queue available on the broker via AMQP.
At same time, a Spark driver application uses the AMQP connector for reading the messages from the above queue and pushing them to the Spark engine. The driver application shows the max temperature value in the last 5 seconds.

## Requirements to build/run:

The main requirements for running the demo are :

* [Maven](https://maven.apache.org/)

## Building the demo source code

The current demo repository needs to be cloned in order to compile and package the demo application. The demo source code has two main parts:

* the Spark driver application which connects to an AMQP source for getting messages and uses the AMQP connector for pushing them into the Spark Streaming engine, showing on the console the output.
* the publisher application which uses the Vert.x Proton library for connecting to an AMQP address (i.e. a queue) and sending messages.

First step cloning the repo :

``` shell
$ git clone https://github.com/redhat-iot/amqp-spark-demo.git
```

Then compiling and packaging the publisher application :

``` shell
$ cd demo/amqp-publisher/
$ mvn package
```

Finally the Spark driver application :

``` shell
$ cd demo/amqp-spark-driver/
$ mvn package
```

In the related `target` subdirectory, the above Maven commands create fat jars for both applications with all dependencies needed.

## OpenShift cluster set up

The demo uses OpenShift as platform for running the Apache Spark cluster. The simple way to set up an OpenShift cluster is using the `oc cluster up` command. At this [link](https://github.com/openshift/origin/blob/master/docs/cluster_up_down.md), there is the official documentation which explains how to get the `oc` command line tools for OpenShift, how to configure the Docker daemon which will run the images and how to run the cluster on the local PC.

Start up an OpenShift Origin cluster locally :

``` shell
$ oc cluster up
```

When the cluster is up and running few other steps are needed for adding the Oshinko application which is in charge to deploy the Apache Spark cluster.

Create an "oshinko" ServiceAccount, used by Oshinko to write to the OpenShift API.

``` shell
echo '{"apiVersion": "v1", "kind": "ServiceAccount", "metadata": {"name": "oshinko"}}' | oc create -f -
```

Authorize the "oshinko" ServiceAccount to write to the OpenShift API.

``` shell
oc policy add-role-to-user admin -z oshinko
```

Create the Oshinko template.

``` shell
curl https://raw.githubusercontent.com/radanalyticsio/oshinko-rest/master/tools/server-ui-template.yaml | oc create -f -
```

Launch Oshinko in the current project.

``` shell
oc new-app oshinko
```

Last step is to run an Apache Artemis instance into the same cluster.

``` shell
curl https://raw.githubusercontent.com/redhat-iot/amqp-spark-demo/master/cluster/artemis-rc.yaml | oc create -f -
```

## Deploying the Apache Spark cluster

Using the OpenShift web console available locally at `https://localhost:8443/console`, it's possible to handle the cluster but in this case the main thing is accessing to the Oshinko web application (through the link available in the overview page of the running project).

The Oshinko web application provides a simple "deploy" button in order to deploy an Apache Spark cluster on OpenShift specifying :

* the cluster name
* the number of nodes

Just for the demo, 2 nodes are enough.

After deploying the cluster, the Oshinko web application shows the addresses for all nodes but the most important is the "master" one that could be something like this :

```
spark://172.17.0.7:7077
```

Finally, from the main OpenShift web console we need to access to the Apache Artemis deployment and check what is the address for this instance; it's needed for the demo applications which connect to it for exchanging AMQP messages.

## Running demo applications

Running the publisher application is quite simple because from the `amqp-publisher` directory project, the launch command is the following :

``` shell
java -cp ./target/amqp-publisher-1.0-SNAPSHOT.jar com.redhat.iot.spark.AMQPPublisher 172.17.0.5 5672
```

where the fat jar and the main class to run `AMQPPublisher` are specified with two arguments :

* IP address of the AMQP source (in this case the Apache Artemis instance)
* the AMQP port to connect on the above address

While running, the publisher application shows the simulated temperature values sent to the AMQP destination every second.

In order to run the Spark driver application locally (on the PC) and not from one of the available nodes in the cluster, an Apache Spark local installations is needed from [download page](http://spark.apache.org/downloads.html) on the official web site.
From the root directory of the downloaded (and unzipped) Spark installation, the command to run the Spark driver is the following :

``` shell
./bin/spark-submit --class com.redhat.iot.spark.AMQPTemperature --master spark://172.17.0.7:7077 <PATH_TO_DEMO>/amqp-spark-demo/demo/amqp-spark-driver/target/amqp-spark-driver-1.0-SNAPSHOT.jar 172.17.0.5 5672
```

The `spark-submit` script needs :

* the main class with driver application to run (in this case `AMQPTemperature`)
* the address of the master node
* the path to the fat jar containing the driver application

On the same command line, two arguments are needed for the driver application :

* IP address of the AMQP source (in this case the Apache Artemis instance)
* the AMQP port to connect on the above address

While running, the driver application shows (other than some log messages from Spark engine) the max temperature value in the last 5 seconds.
