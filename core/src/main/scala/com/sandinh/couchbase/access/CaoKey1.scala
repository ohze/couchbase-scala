package com.sandinh.couchbase.access

import com.couchbase.client.scala.kv.{
  GetOptions,
  GetResult,
  InsertOptions,
  MutationResult,
  RemoveOptions,
  ReplaceOptions,
  UpsertOptions
}
import com.couchbase.client.scala.codec.JsonSerializer.PlayEncode
import com.sandinh.couchbase.CBBucket
import play.api.libs.json.{Format, Json}

import scala.concurrent.Future

/** @inheritdoc */
private[access] trait CaoKeyId[T] extends CaoKey1[T, String] {

  /** @inheritdoc */
  override final protected def key(id: String): String = id
}

/** Base trait for Couchbase Access Object to access json documents
  * that can be decode/encode to/from the `T` type -
  * which is stored in couchbase at the key generated from the T.key(A) method.
  *
  * This trait permit we interact (get/upsert/replace/..) with couchbase server
  * through a typed interface:
  * instead of {{{
  *   bucket.get(id: String): GetResult
  *   bucket.upsert(id: String, content: T)(implicit serializer: JsonSerializer[T]): MutationResult
  * }}}
  * , we can: {{{
  *   case class Acc(..)
  *   object Acc {
  *     implicit val fmt: OFormat[Acc] = Json.format[Acc]
  *     // Used in upsert
  *     implicit val ser: JsonSerializer[Trophy] = t => PlayEncode.serialize(Json.toJson(t))
  *   }
  *   class AccCao(cluster: CBCluster) extends JsCao[Acc](cluster.bucket("acc"))
  *   val cao: AccCao = ???
  *   cao.get(id: String): Future[Acc]
  *   cao.upsert(id: String, content: Acc): Future[MutationResult]
  * }}}
  * , or: {{{
  *   class AccCao(cluster: CBCluster) extends JsCao1[Acc, Int](cluster.bucket("acc")) {
  *     protected def key(uid: Int): String = "a" + uid
  *   }
  *   val cao: AccCao = ???
  *   cao.get(uid: Int): Future[Acc]
  *   cao.upsert(uid: Int, content: Acc): Future[MutationResult]
  * }}}
  * @see [[JsCao]], [[JsCao1]], [[JsCao2]]
  */
private[access] trait CaoKey1[T, A] extends CaoTrait[T, A] {
  val bucket: CBBucket

  protected implicit val fmt: Format[T]

  /** Map param of type A to a CB key
    * @return CB key (id)
    */
  protected def key(a: A): String

  /** @inheritdoc */
  final def getResult(
    a: A,
    options: GetOptions = GetOptions()
  ): Future[GetResult] = bucket.get(key(a), options)

  /** @inheritdoc */
  final def insert(
    a: A,
    content: T,
    options: InsertOptions = InsertOptions()
  ): Future[MutationResult] =
    bucket.insert(key(a), Json.toJson(content), options.expiry(expiry))

  /** @inheritdoc */
  final def upsert(
    a: A,
    content: T,
    options: UpsertOptions = UpsertOptions()
  ): Future[MutationResult] =
    bucket.upsert(key(a), Json.toJson(content), options.expiry(expiry))

  /** @inheritdoc */
  final def replace(
    a: A,
    content: T,
    options: ReplaceOptions
  ): Future[MutationResult] =
    bucket.replace(key(a), Json.toJson(content), options.expiry(expiry))

  /** @inheritdoc */
  final def remove(
    a: A,
    options: RemoveOptions = RemoveOptions()
  ): Future[MutationResult] = bucket.remove(key(a), options)
}
