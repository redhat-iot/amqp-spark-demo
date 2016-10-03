package com.redhat.iot.spark

import org.apache.qpid.proton.amqp.messaging.{AmqpValue, Section}
import org.apache.qpid.proton.message.Message
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.amqp.{AMQPJsonFunction, AMQPUtils}
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}

/**
  * Sample application for getting insights from published temperature values
  */
object AMQPTemperature {

  private val master: String = "local[2]"
  private val appName: String = getClass().getSimpleName()

  private val batchDuration: Duration = Seconds(1)
  private var checkpointDir: String = "/tmp/spark-streaming-amqp"

  private var host: String = "localhost"
  private var port: Int = 5672
  private val address: String = "temperature"

  private val jsonMessageConverter: AMQPJsonFunction = new AMQPJsonFunction()

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      System.err.println("Usage: AMQPTemperature <hostname> <port> [<checkpointdir>]")
      System.exit(1);
    }

    host = args(0);
    port = args(1).toInt

    if (args.length > 2 && !args(2).isEmpty)
      checkpointDir = args(2)

    // get temperature value directly from AMQP body with custom converter ...
    val ssc = StreamingContext.getOrCreate(checkpointDir, createStreamingContext)

    ssc.start()
    ssc.awaitTermination()
  }

  def messageConverter(message: Message): Option[Int] = {

    val body: Section = message.getBody()
    if (body.isInstanceOf[AmqpValue]) {
      val temp: Int = body.asInstanceOf[AmqpValue].getValue().asInstanceOf[String].toInt
      Some(temp)
    } else {
      None
    }
  }

  def createStreamingContext(): StreamingContext = {

    //val conf = new SparkConf().setMaster(master).setAppName(appName)
    val conf = new SparkConf().setAppName(appName)
    conf.set("spark.streaming.receiver.writeAheadLog.enable", "true")
    //conf.set("spark.streaming.receiver.maxRate", "10000")
    //conf.set("spark.streaming.backpressure.enabled", "true")
    //conf.set("spark.streaming.blockInterval", "1ms")
    val ssc = new StreamingContext(conf, batchDuration)
    ssc.checkpoint(checkpointDir)

    val receiveStream = AMQPUtils.createStream(ssc, host, port, address, messageConverter _, StorageLevel.MEMORY_ONLY)

    // get maximum temperature in a window
    val max = receiveStream.reduceByWindow((a,b) => if (a > b) a else b, Seconds(5), Seconds(5))

    max.print()

    ssc
  }
}
