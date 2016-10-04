# AMQP Spark Streaming demo

This demo shows how it's possible to integrate AMQP based products with Apache Spark Streaming. It uses the AMQP Spark Streaming connector, which is able to get messages from an AMQP source and pushing them to the Spark engine as micro batches for real time analytics.
The other main point is that the used Apache Spark deployment isn't in standalone mode but running on OpenShift.
Finally, an Apache Artemis instance is used as AMQP source/destintation for exchanged messages.

The demo consists of a publisher application which simulates temperature values from a reading sensor and sending them to the `temperature` queue available on the broker via AMQP.
At same time, a Spark driver application uses the AMQP connector for reading the messages from the above queue and pushing them to the Spark engine. The driver application shows the max temperature value in the last 5 seconds.

## Requirements to build/run:

Tha main requirements for running the demo are :

* [Maven](https://maven.apache.org/)

## Building the demo source code

The first component needed for running the demo is the AMQP Spark Streaming connector, which provides an AMQP protocol head for Spark Streaming in order to ingest messages from AMQP based sources (i.e. broker, direct publisher through a router network, ...). The related project is available [here](https://github.com/redhatanalytics/dstream-amqp) and it needs to be compiled and installed into the local Maven repository in order to be available for the demo application.

``` shell
$ git clone https://github.com/redhatanalytics/dstream-amqp.git
$ cd dstream-amqp
$ mvn clean install
```

Now that the AMQP Spark Streaming connector is available locally, the current demo repository needs to be cloned in order to compile and package the demo application. The demo source code has two main parts :

* the Spark driver application which connects to an AMQP source for getting messages and uses the AMQP connector for pushing them into the Spark Streaming engine, showing on the console the output.
* the publisher application which uses the Vert.x Proton library for connecting to an AMQP address (i.e. a queue) and sending messages.

First step cloning the repo :

``` shell
$ git clone https://github.com/redhat-iot/amqp-spark-demo.git
```

Then compiling and packaging the publisher application :

``` shell
cd demo/amqp-publisher/
mvn package
```

Finally the Spark driver application :

``` shell
cd demo/amqp-spark-driver/
mvn package
```

In the related `target` subdirectory, the above Maven commands create fat jars for both applications with all dependencies needed.
