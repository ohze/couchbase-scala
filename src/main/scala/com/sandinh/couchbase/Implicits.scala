package com.sandinh.couchbase

import com.couchbase.client.java.document._
import com.couchbase.client.java.view.{AsyncViewRow, AsyncViewResult}
import com.sandinh.couchbase.document.{CompatStringDocument, JsDocument}
import play.api.libs.json.{JsArray, JsValue}
import rx.Observable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import com.sandinh.rx.Implicits._
import scala.reflect.ClassTag

object Implicits {
  implicit lazy val BinDocCls = classOf[BinaryDocument]
  implicit lazy val JsDocCls = classOf[JsDocument]
  implicit lazy val CompatStringDocCls = classOf[CompatStringDocument]
  //  implicit lazy val JsonDocCls = classOf[JsonDocument]
  //  implicit lazy val JsonArrayDocCls = classOf[JsonArrayDocument]
  //  implicit lazy val JsonBooleanDocCls = classOf[JsonArrayDocument]
  //  implicit lazy val JsonDoubleDocCls = classOf[JsonDoubleDocument]
  //  implicit lazy val JsonLongDocCls = classOf[JsonLongDocument]
  ////  implicit lazy val JsonStringDocCls = classOf[JsonStringDocument]

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
}
