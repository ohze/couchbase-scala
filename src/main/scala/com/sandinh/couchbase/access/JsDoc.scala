package com.sandinh.couchbase.access

import com.sandinh.couchbase.document.JsDocument
import play.api.libs.json.{Json, JsValue, Format}

abstract class JsDoc[T: Format] extends DocBase[T, JsValue, JsDocument] {
  protected def reads(u: JsValue): T = u.as[T]
  protected def writes(t: T): JsValue = Json.toJson(t)

  protected def createDoc(id: String, expiry: Int, content: JsValue): JsDocument = new JsDocument(id, content, expiry)
}

abstract class JsDoc1[T: Format, A] extends JsDoc[T] with Key1[T, A, JsValue, JsDocument]

abstract class JsDoc2[T: Format, A, B] extends JsDoc[T] with Key2[T, A, B, JsValue, JsDocument]
