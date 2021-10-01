package com.sandinh.couchbase.access

import com.sandinh.couchbase.document.CompatStringDocument
import com.sandinh.couchbase.ScalaBucket

trait StrCaoTrait[T] {
  private[access] def bucket: ScalaBucket

  /** Implement [[com.sandinh.couchbase.access.CaoBase.createDoc]] */
  protected def createDoc(
    id: String,
    expiry: Int,
    content: String,
    cas: Long = 0L
  ) = new CompatStringDocument(id, content, expiry, cas)
}

/** Base class for Couchbase Access Object to access StringDocument that can be decode/encode to/from the `T` type */
abstract class StrCao[T](
  private[access] val bucket: ScalaBucket
) extends CaoBase[T, String, CompatStringDocument](bucket)
    with StrCaoTrait[T]

/** Base class for Couchbase Access Object to access StringDocument that can be decode/encode to/from the `T` type - which is
  * store in couchbase at key generated from the T.key(A) method
  */
abstract class StrCao1[T, A](
  private[access] val bucket: ScalaBucket
) extends StrCaoTrait[T]
    with WithCaoKey1[T, A, String, CompatStringDocument] {
  final override val self = new StrCao[T](bucket) {
    protected def reads(u: String): T = StrCao1.this.reads(u)
    protected def writes(t: T): String = StrCao1.this.writes(t)
  }
}

/** Base class for Couchbase Access Object to access StringDocument that can be decode/encode to/from the `T` type - which is
  * store in couchbase at key generated from the T.key(A, B) method
  */
abstract class StrCao2[T, A, B](bucket: ScalaBucket)
    extends StrCao[T](bucket)
    with WithCaoKey2[T, A, B, String, CompatStringDocument]
