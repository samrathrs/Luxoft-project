package processor

import java.io.{File, IOException}

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.github.tototoshi.csv.CSVReader
import model.{Measurement, OutputData}

trait CsvProcessor {
  /** Read and aggregate data from a sequence of CSV-files.
   *
   * @param directoryName name of input directory
   * @return processed data from all input files.
   */
  def run(directoryName: String): IO[OutputData]

  /** Retrieve list of CSV-files from specified directory.
   *
   * @param directoryName name of input directory
   * @return list of CSV-files.
   */
  def getInputFiles(directoryName: String): Seq[File] = {
    val inputDirectory = new File(directoryName)
    if (!inputDirectory.isDirectory) throw new IllegalArgumentException("Wrong input directory")
    inputDirectory.listFiles((_, name) => name.endsWith(".csv")).toIndexedSeq
  }

  /** Process a CSV-file.
   *
   * @param file       a file to process
   * @param outputData optional instance of [[model.OutputData]], which contains data from previously processed files.
   * @return a new [[model.OutputData]] instance containing results of current file processing and optionally
   *         from previously processed files.
   */
  protected def processCsvFile(file: File, outputData: OutputData = OutputData()): IO[OutputData] = {
    val inIO = IO(CSVReader.open(file))

    inIO.bracket { reader =>
      IO({
        val fileOutputData = reader.toStream.tail // Skip header
          .map { // Parse a line
            case id :: "NaN" :: Nil => Measurement(id, None)
            case id :: humidity :: Nil => Measurement(id, humidity.toInt.some)
            case _ => throw new IOException(s"Wrong format of file '${file.getName}'")
          }
          .foldLeft(outputData)(_ + _)

        fileOutputData.copy(fileCount = fileOutputData.fileCount + 1)
      })
    } {
      // Releasing resources.
      in => IO(in.close()).handleErrorWith(_ => IO.unit).void
    }
  }
}

object CsvProcessor {
  private val defaultName: String = "ParallelProcessor"

  /** Factory method to create a CSV processor of chosen type.
   *
   * @param name optional type name of the CSV processor
   * @param cs   context shift used in execution
   * @return new instance of CSV processor.
   */
  def apply(name: Option[String] = None)(implicit cs: ContextShift[IO]): CsvProcessor = name.getOrElse(defaultName) match {
    case "SequentialProcessor" => new SequentialProcessor()
    case "ParallelProcessor" => new ParallelProcessor()
    case "SequentialAkkaProcessor" => new SequentialAkkaProcessor()
    case "ParallelAkkaProcessor" => new ParallelAkkaProcessor()
    case _ => throw new IllegalArgumentException(s"Processor name '$name' is not valid")
  }
}