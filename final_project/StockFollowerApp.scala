package org.cscie88c.final_project
import java.time.Duration
import java.util.Properties

import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{ KafkaStreams, StreamsConfig, Topology }
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser._
import Serdes._
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.scala._


// run with: sbt "runMain org.cscie88c.final_project.StockFollowerApp"
object StockFollowerApp {

  object Implicits {
    implicit def serde[A >: Null : Decoder : Encoder]: Serde[A] = {
      val serializer = (a: A) => a.asJson.noSpaces.getBytes
      val deserializer = (aAsBytes: Array[Byte]) => {
        val aAsString = new String(aAsBytes)
        val aOrError = decode[A](aAsString)
        aOrError match {
          case Right(a) => Option(a)
          case Left(error) =>
            println(s"There was an error converting the message $aOrError, $error")
            Option.empty
        }
      }
      Serdes.fromFn[A](serializer, deserializer)
    }
  }

  // Topics
  object Topics {
    final val RawDataByTick = "StockReadingTopic"
    final val AvgDataByTick = "AvgDataByTick"
  }

  object Domain {
    type Symbol = String
    type TimeStamp = String
    type Open = String
    type High = String
    type Low = String
    type Close = String
    type Volume = String

    case class StockTimeValues(openValue: Open, highValue: High, lowValue: Low, closeValue: Close, volumeValue: Volume)
    
    case class TimeStampValues(timedValue: Map[TimeStamp, StockTimeValues])
    
    case class TimeSeries(title: String, values: TimeStampValues)
  }

  val builder = new StreamsBuilder

  import Implicits._
  import Domain._
  import Topics._

  val rawValues: KStream[TimeStamp, StockTimeValues] =
      builder.stream[TimeStamp, StockTimeValues](RawDataByTick)

  def main(args: Array[String]): Unit = {

    val props: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "stockfollower-application")
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      p
    }

    /*val wordCounts: KTable[String, Long] = textLines
      .flatMapValues(textLine => textLine.toLowerCase.split("\\W+"))
      .groupBy((_, word) => word)
      .count()(Materialized.as("counts-store"))

    wordCounts
      .toStream
      // .peek((k,t) => println(s"stream element: $k: $t")) // to print items in stream
      .filter((_, count) => count > 5)
      .map((word, count) => (word, s"$word: $count"))
      .to("WordsWithCountsTopic")
    */
    val streams: KafkaStreams = new KafkaStreams(builder.build(), props)

    streams.start()

    sys.ShutdownHookThread {
      streams.close(Duration.ofSeconds(10))
    }
  }
  
}


