package com.sandinh.couchbase.access

import com.couchbase.client.scala.kv.MutationResult
import scala.concurrent.{ExecutionContext, Future}

/** Base trait for Couchbase Access Object to access json documents
  * that can be decode/encode to/from the `T` type -
  * which is store in couchbase at key generated from the T.key(A, B) method
  */
private[access] trait CaoKey2[T, A, B] extends CaoKeyId[T] {

  /** Map 2 param of type A, B to a CB key
    * @return CB key
    */
  protected def key(a: A, b: B): String

  final def get(a: A, b: B)(implicit ec: ExecutionContext): Future[T] =
    get(key(a, b))

  final def getOrElse(a: A, b: B)(default: => T)(
    implicit ec: ExecutionContext
  ): Future[T] =
    getOrElse(key(a, b))(default)

  final def getBulk(aa: Seq[A], b: B)(
    implicit ec: ExecutionContext
  ): Future[Seq[T]] =
    Future.traverse(aa)(get(_, b))

  final def getWithCAS(a: A, b: B)(
    implicit ec: ExecutionContext
  ): Future[(T, Long)] =
    getWithCAS(key(a, b))

  final def getOrElseWithCAS(a: A, b: B)(default: => T)(
    implicit ec: ExecutionContext
  ): Future[(T, Long)] =
    getOrElseWithCAS(key(a, b))(default)

  @deprecated("Use upsert or replace. This `set` method use `upsert`", "10.0.0")
  final def set(a: A, b: B, content: T): Future[MutationResult] =
    upsert(key(a, b), content)

  def upsert(a: A, b: B, content: T): Future[MutationResult] =
    upsert(key(a, b), content)

  def replace(a: A, b: B, content: T): Future[MutationResult] =
    replace(key(a, b), content)

  /** convenient method. = upsert(..).map(_ => t) */
  final def setT(a: A, b: B, content: T)(
    implicit ec: ExecutionContext
  ): Future[T] =
    upsert(key(a, b), content).map(_ => content)

  final def setBulk(aa: Seq[A], b: B, contents: Seq[T])(
    implicit ec: ExecutionContext
  ): Future[Seq[MutationResult]] =
    setBulk(aa.map(key(_, b)), contents)

  final def change(a: A, b: B)(f: Option[T] => T)(
    implicit ec: ExecutionContext
  ): Future[MutationResult] =
    change(key(a, b))(f)

  final def flatChange(a: A, b: B)(f: Option[T] => Future[T])(
    implicit ec: ExecutionContext
  ): Future[MutationResult] =
    flatChange(key(a, b))(f)

  final def changeBulk(aa: Seq[A], b: B)(f: Option[T] => T)(
    implicit ec: ExecutionContext
  ): Future[Seq[MutationResult]] =
    Future.traverse(aa)(change(_, b)(f))

  final def flatChangeBulk(aa: Seq[A], b: B)(
    f: Option[T] => Future[T]
  )(implicit ec: ExecutionContext): Future[Seq[MutationResult]] =
    Future.traverse(aa)(flatChange(_, b)(f))

  final def remove(a: A, b: B): Future[MutationResult] = remove(key(a, b))
}
