package com.sandinh.couchbase

import play.api.libs.json.Json

case class Trophy(
  /** user name */
  n: String,
  /** [ [trophyType, firstRoundId, winTime],...] */
  d: List[List[Int]]
)

object Trophy {
  implicit val fmt = Json.format[Trophy]
  val t1 = Trophy("giabao", List(List(1, 2, 3), List(4, 5, 6)))
}
