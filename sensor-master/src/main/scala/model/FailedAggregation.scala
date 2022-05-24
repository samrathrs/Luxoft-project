package model

/** Aggregated data from a sensor in case of all failed measurements */
case object FailedAggregation extends Aggregation {
  /** Add an optional value to an aggregation.
   *
   * @param rhs value to add
   * @return new instance of aggregation, which includes rhs value.
   */
  def +(rhs: Option[Int]): Aggregation = rhs match {
    case None => this
    case Some(v) => ValidAggregation(v, v, v, 1)
  }

  /** Combine two aggregations.
   *
   * @param rhs aggregation to be combined
   * @return combined aggregation.
   */
  def |+|(rhs: Aggregation): Aggregation = rhs

  /** String representation of [[model.FailedAggregation]] */
  override def toString: String = "NaN,NaN,NaN"

  /** Average value */
  override def average: Option[Int] = None
}
