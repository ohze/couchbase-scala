package com.sandinh.couchbase

import com.couchbase.client.java.document._
import com.sandinh.couchbase.document.{CompatStringDocument, JsDocument}

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
}
