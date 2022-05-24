package model

import cats.Semigroup

/** Aggregated data from a sensor */
trait Aggregation {
  /** Add an optional value to an aggregation.
   *
   * @param rhs value to add
   * @return new instance of aggregation, which includes rhs value.
   */
  def +(rhs: Option[Int]): Aggregation

  /** Combine two aggregations.
   *
   * @param rhs aggregation to be combined
   * @return combined aggregation.
   */
  def |+|(rhs: Aggregation): Aggregation

  /** Average value */
  def average: Option[Int]
}

/** Semigroup for [[model.Aggregation]]  */
trait AggregationSemigroupImpl extends Semigroup[Aggregation] {
  def combine(lhs: Aggregation, rhs: Aggregation): Aggregation = lhs |+| rhs
}

/** Provide implicit semigroup for [[model.Aggregation]]*/
trait AggregationSemigroup {

  implicit object aggregationSemigroupImpl extends AggregationSemigroupImpl

}