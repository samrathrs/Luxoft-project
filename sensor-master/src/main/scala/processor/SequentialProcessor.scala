package processor

import cats.effect.IO
import model.OutputData

/** Sequential processing of CSV files.
 *
 * Slow, but minimal memory requirements.
 */
class SequentialProcessor extends CsvProcessor {
  /** Read and aggregate data from a sequence of CSV-files.
   *
   * @param directoryName name of input directory
   * @return processed data from all input files.
   */
  def run(directoryName: String): IO[OutputData] =
    getInputFiles(directoryName).foldLeft(IO(OutputData())) { (either, file) => either.flatMap(processCsvFile(file, _)) }
}
