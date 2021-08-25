package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import com.couchbase.client.java.error.DocumentDoesNotExistException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** internal */
trait WithCaoKey1[T, A, U, D <: Document[U]] { self: CaoBase[T, U, D] =>
  protected def key(a: A): String

  final def get(a: A): Future[T] = self.get(key(a))
  final def getOrElse(a: A)(default: => T): Future[T] = self.getOrElse(key(a))(default)
  final def getBulk(aa: Seq[A]): Future[Seq[T]] = Future.traverse(aa)(get)

  final def getWithCAS(a: A): Future[(T, Long)] = self.getWithCAS(key(a))
  final def getOrElseWithCAS(a: A)(default: => T): Future[DocumentCAS] = self.getOrElseWithCAS(key(a))(default)
  final def getBulkWithCAS(aa: Seq[A]): Future[Seq[DocumentCAS]] = Future.traverse(aa)(getWithCAS)

  final def set(a: A, t: T): Future[D] = self.set(key(a), t)
  final def updateWithCAS(a: A, t: T, cas: Long = 0): Future[D] = self.update(key(a), t, cas)

  /** convenient method. = set(..).map(_ => t) */
  final def setT(a: A, t: T): Future[T] = self.set(key(a), t).map(_ => t)
  final def setBulk(aa: Seq[A], tt: Seq[T]): Future[Seq[D]] = Future.traverse(aa zip tt) {
    case (a, t) => set(a, t)
  }

  final def change(a: A)(f: Option[T] => T): Future[D] = get(a)
    .map(Option(_))
    .recover { case _: DocumentDoesNotExistException => None }
    .flatMap { o => set(a, f(o)) }

  final def flatChange(a: A)(f: Option[T] => Future[T]): Future[D] = get(a)
    .map(Option(_))
    .recover { case _: DocumentDoesNotExistException => None }
    .flatMap(f).flatMap(set(a, _))

  final def changeBulk(aa: Seq[A])(f: Option[T] => T): Future[Seq[D]] = Future.traverse(aa)(change(_)(f))

  final def flatChangeBulk(aa: Seq[A])(f: Option[T] => Future[T]): Future[Seq[D]] = Future.traverse(aa)(flatChange(_)(f))

  final def remove(a: A): Future[D] = self.remove(key(a))
}
