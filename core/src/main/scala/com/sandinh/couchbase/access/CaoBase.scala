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

  final def getWithId(id: String): Future[T] = bucket.get[D](id).map(d => reads(d.content))

  def getOrElseWithId(id: String)(default: => T): Future[T] = getWithId(id).recover {
    case _: DocumentDoesNotExistException => default
  }

  def getOrUpdateWithId(id: String)(default: => T): Future[T] = getWithId(id).recoverWith {
    case _: DocumentDoesNotExistException => setWithIdT(id, default)
  }

  final def setWithId(id: String, t: T): Future[D] = bucket.upsert(createDoc(id, expiry(), writes(t)))

  /** convenient method. = set(..).map(_ => t) */
  final def setWithIdT(id: String, t: T): Future[T] = setWithId(id, t).map(_ => t)

  final def removeWithId(id: String): Future[D] = bucket.remove[D](id)
}
