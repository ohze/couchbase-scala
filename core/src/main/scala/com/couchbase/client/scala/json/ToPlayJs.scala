package com.couchbase.client.scala.json

import play.api.libs.json.{
  JsArray,
  JsBoolean,
  JsNull,
  JsNumber,
  JsObject,
  JsString,
  JsValue
}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

object ToPlayJs {

  /** @see [[com.couchbase.client.scala.json.JsonObject.toMap]] */
  def apply(o: JsonObject): JsObject = {
    val m = mutable.Map.empty[String, JsValue]
    for (entry <- o.content.entrySet.asScala)
      m.put(
        entry.getKey,
        entry.getValue match {
          case null              => JsNull
          case x: Boolean        => JsBoolean(x)
          case x: String         => JsString(x)
          case x: Int            => JsNumber(BigDecimal(x))
          case x: Long           => JsNumber(BigDecimal(x))
          case x: Double         => JsNumber(BigDecimal(x))
          case x: Float          => JsNumber(BigDecimal(x.toDouble))
          case x: Short          => JsNumber(BigDecimal(x))
          case x: JsonObject     => apply(x)
          case x: JsonObjectSafe => apply(x.o)
          case x: JsonArray      => apply(x)
          case x: JsonArraySafe  => apply(x.a)
          case _                 => ??? // can NOT go here
        }
      )
    JsObject(m)
  }

  /** @see [[com.couchbase.client.scala.json.JsonArray.toSeq]] */
  def apply(a: JsonArray): JsArray = {
    val l = ListBuffer.empty[JsValue]
    for (x <- a.iterator)
      l += (x match {
        case null              => JsNull
        case x: Boolean        => JsBoolean(x)
        case x: String         => JsString(x)
        case x: Int            => JsNumber(BigDecimal(x))
        case x: Long           => JsNumber(BigDecimal(x))
        case x: Double         => JsNumber(BigDecimal(x))
        case x: Float          => JsNumber(BigDecimal(x.toDouble))
        case x: Short          => JsNumber(BigDecimal(x))
        case x: JsonObject     => apply(x)
        case x: JsonObjectSafe => apply(x.o)
        case x: JsonArray      => apply(x)
        case x: JsonArraySafe  => apply(x.a)
        case _                 => ??? // can NOT go here
      })
    JsArray(l)
  }
}
