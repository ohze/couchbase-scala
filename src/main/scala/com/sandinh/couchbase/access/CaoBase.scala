package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import com.sandinh.couchbase.ScalaBucket
import rx.lang.scala.Observable
import scala.reflect.ClassTag

/** Base class for Couchbase Access Object */
abstract class CaoBase[T, U, D <: Document[U]](implicit tag: ClassTag[D]) {
  protected def bucket: Observable[ScalaBucket]
  protected def expiry(): Int = 0

  protected def reads(u: U): T
  protected def writes(t: T): U

  protected def createDoc(id: String, expiry: Int, content: U): D

  final def get(id: String): Observable[T] = bucket.flatMap(_.get[D](id)).map(d => reads(d.content))

  final def set(id: String, t: T): Observable[T] = bucket.flatMap(_.upsert(createDoc(id, expiry(), writes(t)))).map(_ => t)

  final def remove(id: String): Observable[D] = bucket.flatMap(_.remove[D](id))
}
