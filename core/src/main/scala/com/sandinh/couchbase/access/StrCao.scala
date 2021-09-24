package com.sandinh.couchbase.access

import com.sandinh.couchbase.document.CompatStringDocument
import com.sandinh.couchbase.ScalaBucket

/** Base class for Couchbase Access Object to access StringDocument that can be decode/encode to/from the `T` type */
abstract class StrCao[T](bucket: ScalaBucket)
    extends CaoBase[T, String, CompatStringDocument](bucket) {
  protected def createDoc(
    id: String,
    expiry: Int,
    content: String,
    cas: Long = 0L
  ) = new CompatStringDocument(id, content, expiry, cas)
}

/** Base class for Couchbase Access Object to access StringDocument that can be decode/encode to/from the `T` type - which is
  * store in couchbase at key generated from the T.key(A) method
  */
abstract class StrCao1[T, A](bucket: ScalaBucket)
    extends StrCao[T](bucket)
    with WithCaoKey1[T, A, String, CompatStringDocument]

/** Base class for Couchbase Access Object to access StringDocument that can be decode/encode to/from the `T` type - which is
  * store in couchbase at key generated from the T.key(A, B) method
  */
abstract class StrCao2[T, A, B](bucket: ScalaBucket)
    extends StrCao[T](bucket)
    with WithCaoKey2[T, A, B, String, CompatStringDocument]
