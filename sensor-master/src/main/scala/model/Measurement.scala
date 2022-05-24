package model

/** Measurement, which corresponds to a line in a CSV file. */
case class Measurement(id: String, humidity: Option[Int])