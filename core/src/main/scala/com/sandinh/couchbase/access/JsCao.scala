package com.sandinh.couchbase.access

import com.sandinh.couchbase.CBBucket
import play.api.libs.json.Format

/** @inheritdoc
  * @see [[CaoKey1]], [[JsCao1]], [[JsCao2]]
  */
class JsCao[T](
  val bucket: CBBucket
)(
  protected implicit val fmt: Format[T]
) extends CaoKey0[T]

/** @inheritdoc */
abstract class JsCao1[T, A](
  val bucket: CBBucket
)(
  protected implicit val fmt: Format[T]
) extends CaoKey1[T, A]

/** @inheritdoc */
abstract class JsCao2[T, A, B](
  val bucket: CBBucket
)(
  protected implicit val fmt: Format[T]
) extends CaoKey2[T, A, B]
