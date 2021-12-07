package com.sandinh.couchbase

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.codec.JsonDeserializer.PlayConvert
import com.couchbase.client.scala.codec.{JsonDeserializer, JsonSerializer}
import com.couchbase.client.scala.codec.JsonSerializer.PlayEncode
import com.couchbase.client.scala.json.{JsonArray, JsonObject, ToPlayJs}
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Implicits {
  implicit class DocNotExistFuture[T](private val underlying: Future[T])
      extends AnyVal {
    def recoverNotExist[U >: T](default: => U)(
      implicit ec: ExecutionContext
    ): Future[U] =
      underlying.recover { case _: DocumentNotFoundException => default }

    def optNotExist(implicit ec: ExecutionContext): Future[Option[T]] =
      underlying.map(Option(_)).recoverNotExist(None)
  }

  implicit final class RichJsonObject(private val o: JsonObject)
      extends AnyVal {
    @inline def toPlayJs: JsObject = ToPlayJs(o)
  }

  implicit final class RichJsonArray(private val a: JsonArray) extends AnyVal {
    @inline def toPlayJs: JsArray = ToPlayJs(a)
  }

  implicit def jsonSerializer[T: Writes]: JsonSerializer[T] = content =>
    PlayEncode.serialize(Json.toJson(content))

  implicit def jsonDeserializer[T: Reads]: JsonDeserializer[T] = bytes =>
    PlayConvert.deserialize(bytes).map(_.as[T])
}
