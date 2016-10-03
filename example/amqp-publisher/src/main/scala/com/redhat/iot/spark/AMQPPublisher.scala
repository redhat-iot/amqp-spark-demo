package com.redhat.iot.spark

import java.lang.Long

import io.vertx.core.{AsyncResult, Handler, Vertx}
import io.vertx.proton._
import org.apache.qpid.proton.amqp.messaging.AmqpValue
import org.apache.qpid.proton.message.Message

import scala.util.Random

/**
  * Sample application which publishes temperature values to an AMQP node
  */
object AMQPPublisher {

  private var host: String = "localhost"
  private var port: Int = 5672
  private val address: String = "temperature"

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      System.err.println("Usage: AMQPPublisher <hostname> <port>")
      System.exit(1)
    }

    host = args(0)
    port = args(1).toInt

    val vertx: Vertx = Vertx.vertx()

    val client:ProtonClient = ProtonClient.create(vertx)

    client.connect(host, port, new Handler[AsyncResult[ProtonConnection]] {
      override def handle(ar: AsyncResult[ProtonConnection]): Unit = {
        if (ar.succeeded()) {

          val connection: ProtonConnection = ar.result()
          connection.open()

          val sender: ProtonSender = connection.createSender(address)
          sender.open()

          val random = new Random()

          vertx.setPeriodic(1000, new Handler[Long] {
            override def handle(timer: Long): Unit = {

              val temp: Int = 20 + random.nextInt(5)

              val message: Message = ProtonHelper.message()
              message.setBody(new AmqpValue(temp.toString))

              println("Temperature = " + temp)
              sender.send(message, new Handler[ProtonDelivery] {
                override def handle(delivery: ProtonDelivery): Unit = {

                }
              })
            }
          })

        }
      }
    })

    System.in.read()
  }
}
