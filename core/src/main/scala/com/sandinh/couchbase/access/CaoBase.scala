package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import com.couchbase.client.java.error.DocumentDoesNotExistException
import com.sandinh.couchbase.ScalaBucket
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global

/** Base class for Couchbase Access Object */
abstract class CaoBase[T, U, D <: Document[U]: ClassTag](bucket: ScalaBucket) {
  protected def expiry(): Int = 0

  protected def reads(u: U): T
  protected def writes(t: T): U

  type DocumentCAS = (T, Long)

  protected def createDoc(
    id: String,
    expiry: Int,
    content: U,
    cas: Long = 0L
  ): D

  final def get(id: String): Future[T] =
    bucket.get[D](id).map(d => reads(d.content))

  final def getWithCAS(id: String): Future[DocumentCAS] =
    bucket.get[D](id).map(d => (reads(d.content), d.cas()))

  def getOrElse(id: String)(default: => T): Future[T] = get(id).recover {
    case _: DocumentDoesNotExistException => default
  }

  def getOrElseWithCAS(id: String)(default: => T): Future[DocumentCAS] =
    getWithCAS(id).recover { case _: DocumentDoesNotExistException =>
      (default, -1)
    }

  def getOrUpdate(id: String)(default: => T): Future[T] = get(id).recoverWith {
    case _: DocumentDoesNotExistException => setT(id, default)
  }

  final def set(id: String, t: T): Future[D] =
    bucket.upsert(createDoc(id, expiry(), writes(t)))

  final def update(id: String, t: T, cas: Long = 0): Future[D] =
    bucket.replace(createDoc(id, expiry(), writes(t), cas))

  /** convenient method. = set(..).map(_ => t) */
  final def setT(id: String, t: T): Future[T] = set(id, t).map(_ => t)

  final def remove(id: String): Future[D] = bucket.remove[D](id)
}
