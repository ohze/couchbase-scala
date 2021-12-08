package com.sandinh.couchbase.access

import com.couchbase.client.scala.kv.MutationResult

import scala.concurrent.{ExecutionContext, Future}

/** Base trait for Couchbase Access Object to access json documents
  * that can be decode/encode to/from the `T` type -
  * which is store in couchbase at a constance key
  */
private[access] trait CaoKey0[T] extends CaoKeyId[T] {
  protected val key: String

  final def get()(implicit ec: ExecutionContext): Future[T] =
    get(key)

  final def getOrElse(default: => T)(
    implicit ec: ExecutionContext
  ): Future[T] =
    getOrElse(key)(default)

  final def getWithCAS()(
    implicit ec: ExecutionContext
  ): Future[(T, Long)] =
    getWithCAS(key)

  final def getOrElseWithCAS(default: => T)(
    implicit ec: ExecutionContext
  ): Future[(T, Long)] =
    getOrElseWithCAS(key)(default)

  def upsert(content: T): Future[MutationResult] =
    upsert(key, content)

  def replace(content: T): Future[MutationResult] =
    replace(key, content)

  /** convenient method. = upsert(..).map(_ => t) */
  final def setT(content: T)(
    implicit ec: ExecutionContext
  ): Future[T] =
    upsert(key, content).map(_ => content)

  final def change()(f: Option[T] => T)(
    implicit ec: ExecutionContext
  ): Future[MutationResult] =
    change(key)(f)

  final def flatChange()(f: Option[T] => Future[T])(
    implicit ec: ExecutionContext
  ): Future[MutationResult] =
    flatChange(key)(f)

  final def remove(): Future[MutationResult] = remove(key)
}
