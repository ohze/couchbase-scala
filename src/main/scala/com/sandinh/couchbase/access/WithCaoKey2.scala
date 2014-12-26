package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import com.couchbase.client.java.error.DocumentDoesNotExistException
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** internal */
trait WithCaoKey2[T, A, B, U, D <: Document[U]] { self: CaoBase[T, U, D] =>
  /** Map 2 param of type A, B to a CB key
    * @return CB key */
  protected def key(a: A, b: B): String

  final def get(a: A, b: B): Future[T] = self.get(key(a, b))
  final def getOrElse(a: A, b: B, default: => T): Future[T] = self.getOrElse(key(a, b), default)
  final def getBulk(aa: Seq[A], b: B): Future[Seq[T]] = Future.traverse(aa)(get(_, b))

  final def set(a: A, b: B, t: T): Future[D] = self.set(key(a, b), t)
  /** convenient method. = set(..).map(_ => t) */
  final def setT(a: A, b: B, t: T): Future[T] = self.set(key(a, b), t).map(_ => t)
  final def setBulk(aa: Seq[A], b: B, tt: Seq[T]): Future[Seq[D]] = Future.traverse(aa zip tt) {
    case (a, t) => set(a, b, t)
  }

  final def change(a: A, b: B)(f: Option[T] => T): Future[D] = get(a, b)
    .map(Option(_))
    .recover { case _: DocumentDoesNotExistException => None }
    .flatMap { o => set(a, b, f(o)) }

  final def flatChange(a: A, b: B)(f: Option[T] => Future[T]): Future[D] = get(a, b)
    .map(Option(_))
    .recover { case _: DocumentDoesNotExistException => None }
    .flatMap(f).flatMap(set(a, b, _))

  final def changeBulk(aa: Seq[A], b: B)(f: Option[T] => T): Future[Seq[D]] = Future.traverse(aa)(change(_, b)(f))

  final def flatChangeBulk(aa: Seq[A], b: B)(f: Option[T] => Future[T]): Future[Seq[D]] = Future.traverse(aa)(flatChange(_, b)(f))

  final def remove(a: A, b: B): Future[D] = self.remove(key(a, b))
}
