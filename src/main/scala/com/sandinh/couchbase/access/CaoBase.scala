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

  protected def createDoc(id: String, expiry: Int, content: U): D

  final def get(id: String): Future[T] = bucket.get[D](id).map(d => reads(d.content))
  def getOrElse(id: String)(default: => T): Future[T] = get(id).recover {
    case _: DocumentDoesNotExistException => default
  }

  final def set(id: String, t: T): Future[D] = bucket.upsert(createDoc(id, expiry(), writes(t)))

  /** convenient method. = set(..).map(_ => t) */
  final def setT(id: String, t: T): Future[T] = set(id, t).map(_ => t)

  final def remove(id: String): Future[D] = bucket.remove[D](id)
}
