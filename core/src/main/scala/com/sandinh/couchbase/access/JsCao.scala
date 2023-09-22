package com.sandinh.couchbase.access

import com.sandinh.couchbase.CBBucket
import play.api.libs.json.Format

/** @inheritdoc
  * @see [[CaoKey1]], [[JsCao0]], [[JsCao1]], [[JsCao2]]
  */
class JsCao[T](
  val bucket: CBBucket
)(
  protected implicit val fmt: Format[T]
) extends CaoKeyId[T]

/** @inheritdoc
  * @see [[CaoKey1]], [[JsCao]], [[JsCao1]], [[JsCao2]]
  */
class JsCao0[T](
  val bucket: CBBucket,
  protected val key: String
)(
  protected implicit val fmt: Format[T]
) extends CaoKey0[T]

/** @inheritdoc
  * @see [[CaoKey1]], [[JsCao0]], [[JsCao]], [[JsCao2]]
  */
abstract class JsCao1[T, A](
  val bucket: CBBucket
)(
  protected implicit val fmt: Format[T]
) extends CaoKey1[T, A] {
  @deprecated("May be removed in later versions", "10.0.0")
  final lazy val self = new JsCao(bucket)
}

/** @inheritdoc
  * @see [[CaoKey1]], [[JsCao0]], [[JsCao1]], [[JsCao]]
  */
abstract class JsCao2[T, A, B](
  val bucket: CBBucket
)(
  protected implicit val fmt: Format[T]
) extends CaoKey2[T, A, B]
