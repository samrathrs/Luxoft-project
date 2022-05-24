package processor

import cats.effect.IO
import cats.implicits._
import implicits._
import model.OutputData

import scala.collection.parallel.CollectionConverters._

/** Processing CSV-files in parallel using parallel collections.
 *
 * Fast, but requires more memory for keeping results of each file processing.
 * MapReduce pattern.
 */
class ParallelProcessor extends CsvProcessor {
  /** Read and aggregate data from a sequence of CSV-files.
   *
   * @param directoryName name of input directory
   * @return processed data from all input files.
   */
  def run(directoryName: String): IO[OutputData] = getInputFiles(directoryName).par
    .map(file => processCsvFile(file))
    .fold(IO(OutputData()))(_ |+| _)
}
