package com.sandinh.couchbase.access

import com.sandinh.couchbase.document.JsDocument
import play.api.libs.json.{Json, JsValue, Format}

/** Base class for Couchbase Access Object to access json documents that can be decode/encode to/from the `T` type */
abstract class JsCao[T: Format] extends CaoBase[T, JsValue, JsDocument] {
  protected def reads(u: JsValue): T = u.as[T]
  protected def writes(t: T): JsValue = Json.toJson(t)

  protected def createDoc(id: String, expiry: Int, content: JsValue): JsDocument = new JsDocument(id, content, expiry)
}

/** Base class for Couchbase Access Object to access json documents that can be decode/encode to/from the `T` type - which is
  * store in couchbase at key generated from the T.key(A) method */
abstract class JsCao1[T: Format, A] extends JsCao[T] with WithCaoKey1[T, A, JsValue, JsDocument]

/** Base class for Couchbase Access Object to access json documents that can be decode/encode to/from the `T` type - which is
  * store in couchbase at key generated from the T.key(A, B) method */
abstract class JsCao2[T: Format, A, B] extends JsCao[T] with WithCaoKey2[T, A, B, JsValue, JsDocument]
