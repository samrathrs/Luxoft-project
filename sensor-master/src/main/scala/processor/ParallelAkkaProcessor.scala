package processor

import java.io.File

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import cats.implicits._
import implicits._
import cats.effect._
import model.OutputData

import scala.concurrent.{ExecutionContextExecutor, Future}

/** Processing CSV-files in parallel using akka-streams. */
class ParallelAkkaProcessor(implicit cs: ContextShift[IO]) extends CsvProcessor {
  /** Read and aggregate data from a sequence of CSV-files.
   *
   * @param directoryName name of input directory
   * @return processed data from all input files.
   */
  def run(directoryName: String): IO[OutputData] = IO.fromFuture(IO {
    implicit val system: ActorSystem = ActorSystem("Sensor")
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val parallelism = 8
    val source = Source(getInputFiles(directoryName))
    val future = source
      .via(Flow[File].mapAsync(parallelism)(file => Future(processCsvFile(file))))
      .runWith(Sink.fold(IO(OutputData()))(_ |+| _))

    future.onComplete(_ => system.terminate())
    future
  }).flatMap(identity)
}
