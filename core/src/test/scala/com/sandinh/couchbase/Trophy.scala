package com.sandinh.couchbase

import play.api.libs.json.{Json, OFormat}

case class Trophy(
  /** user name */
  n: String,
  /** [ [trophyType, firstRoundId, winTime],...] */
  d: List[List[Int]]
)

object Trophy {
  implicit val fmt: OFormat[Trophy] = Json.format[Trophy]
  val t1: Trophy = Trophy("giabao", List(List(1, 2, 3), List(4, 5, 6)))
  val t2: Trophy = Trophy("thanhpv", List(List(1, 2, 3), List(4, 5, 6)))
  val t3: Trophy = Trophy("truongnx", List(List(1, 2, 3), List(4, 5, 6)))
}
