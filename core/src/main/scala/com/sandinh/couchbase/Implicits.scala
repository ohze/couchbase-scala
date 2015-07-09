package com.sandinh.couchbase

import com.couchbase.client.java.document._
import com.couchbase.client.java.document.json.{JsonArray, JsonObject}
import com.couchbase.client.java.error.DocumentDoesNotExistException
import com.couchbase.client.java.view.{AsyncViewRow, AsyncViewResult}
import com.sandinh.couchbase.document.{CompatStringDocument, JsDocument}
import play.api.libs.json._
import rx.Observable
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import com.sandinh.rx.Implicits._
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

object Implicits {
  object DocCls {
    implicit val BinDocCls: Class[BinaryDocument] = classOf[BinaryDocument]
    implicit val JsDocCls: Class[JsDocument] = classOf[JsDocument]
    implicit val CompatStringDocCls: Class[CompatStringDocument] = classOf[CompatStringDocument]
    //  implicit val JsonDocCls = classOf[JsonDocument]
    //  implicit val JsonArrayDocCls = classOf[JsonArrayDocument]
    //  implicit val JsonBooleanDocCls = classOf[JsonArrayDocument]
    //  implicit val JsonDoubleDocCls = classOf[JsonDoubleDocument]
    //  implicit val JsonLongDocCls = classOf[JsonLongDocument]
    ////  implicit val JsonStringDocCls = classOf[JsonStringDocument]
  }

  implicit class RichAsyncViewResult(val underlying: AsyncViewResult) extends AnyVal {
    def foldRows(row2Js: AsyncViewRow => JsValue): Future[JsArray] =
      underlying.rows
        .scMap(row2Js)
        .fold(ListBuffer.empty[JsValue])(_ += _)
        .scMap(JsArray(_))
        .toFuture

    def flatFoldRows(row2Obs: AsyncViewRow => Observable[JsValue]): Future[JsArray] = {
      underlying.rows
        .scConcatMap(row2Obs)
        .fold(ListBuffer.empty[JsValue])(_ += _)
        .scMap(JsArray(_))
        .toFuture
    }
  }

  implicit class RichAsyncViewRow(val underlying: AsyncViewRow) extends AnyVal {
    def doc[D <: Document[_]](implicit tag: ClassTag[D]): Observable[D] =
      underlying.document(tag.runtimeClass.asInstanceOf[Class[D]])
  }

  implicit class DocNotExistFuture[T](val underlying: Future[T]) extends AnyVal {
    def recoverNotExist[U >: T](default: => U): Future[U] =
      underlying.recover { case _: DocumentDoesNotExistException => default }

    def optNotExist: Future[Option[T]] = underlying.map(Option(_)).recoverNotExist(None)
  }

  implicit class RichJsonObject(val o: JsonObject) extends AnyVal {
    def toPlayJs: JsObject = {
      val m = mutable.Map.empty[String, JsValue]
      for (key <- o.getNames.asScala)
        o.get(key) match {
          case null                 => m.put(key, JsNull)
          case x: java.lang.Boolean => m.put(key, JsBoolean(x))
          case x: String            => m.put(key, JsString(x))
          case x: Integer           => m.put(key, JsNumber(BigDecimal(x)))
          case x: java.lang.Long    => m.put(key, JsNumber(BigDecimal(x)))
          case x: java.lang.Double  => m.put(key, JsNumber(BigDecimal(x)))
          case x: JsonObject        => m.put(key, x.toPlayJs)
          case x: JsonArray         => m.put(key, x.toPlayJs)
          case _                    => //can NOT go here. see com.couchbase.client.java.document.json.JsonValue.checkType
        }
      JsObject(m)
    }
  }

  implicit class RichJsonArray(val a: JsonArray) extends AnyVal {
    def toPlayJs: JsArray = {
      val l = ListBuffer.empty[JsValue]
      for (x <- a.asScala)
        x match {
          case null                 => l += JsNull
          case x: java.lang.Boolean => l += JsBoolean(x)
          case x: String            => l += JsString(x)
          case x: Integer           => l += JsNumber(BigDecimal(x))
          case x: java.lang.Long    => l += JsNumber(BigDecimal(x))
          case x: java.lang.Double  => l += JsNumber(BigDecimal(x))
          case x: JsonObject        => l += x.toPlayJs
          case x: JsonArray         => l += x.toPlayJs
          case _                    => //can NOT go here. see com.couchbase.client.java.document.json.JsonValue.checkType
        }
      JsArray(l)
    }
  }
}
