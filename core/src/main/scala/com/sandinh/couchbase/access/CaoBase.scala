package com.sandinh.couchbase.access

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.codec.JsonSerializer.PlayEncode
import com.couchbase.client.scala.durability.Durability
import com.couchbase.client.scala.durability.Durability.Disabled
import com.couchbase.client.scala.kv._
import com.sandinh.couchbase.CBBucket
import play.api.libs.json.{Format, JsValue, Json}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** Base class for Couchbase Access Object.
  * This class permit we interact (get/upsert/replace/..) with couchbase server
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
  *   class AccCao(cluster: CBCluster) extends JsCao[Acc]
  *   val cao: AccCao = ???
  *   cao.get(id: String): Future[Acc]
  *   cao.upsert(id: String, content: Acc): Future[MutationResult]
  * }}}
  * @see [[JsCao1]], [[JsCao2]]
  */
class JsCao[T](val bucket: CBBucket)(
  protected implicit val fmt: Format[T]
) extends CaoTrait[T, String] {

  /** @inheritdoc */
  final def getResult(
    id: String,
    options: GetOptions = GetOptions()
  ): Future[GetResult] = bucket.get(id, options)

  /** @inheritdoc */
  final def upsert(
    id: String,
    content: T,
    options: UpsertOptions = UpsertOptions()
  ): Future[MutationResult] =
    bucket.upsert(id, Json.toJson(content), options.expiry(expiry))

  /** @inheritdoc */
  final def replace(
    id: String,
    content: T,
    options: ReplaceOptions
  ): Future[MutationResult] =
    bucket.replace(id, Json.toJson(content), options.expiry(expiry))

  /** @inheritdoc */
  final def remove(
    id: String,
    options: RemoveOptions = RemoveOptions()
  ): Future[MutationResult] = bucket.remove(id, options)
}

/** Common interface for [[JsCao]] and [[JsCao1]]
  * @tparam A String for document ID type, as in [[JsCao]]
  *           Or some type that will be used to create the document id, as in JsCao1.key(A)
  */
private[access] trait CaoTrait[T, A] {
  protected implicit val fmt: Format[T]

  protected def expiry: Duration = null

  @deprecated("", "10.0.0")
  final type DocumentCAS = (T, Long)

  /** @param id document id or the param of JsCao1.key(a: A) */
  def getResult(
    id: A,
    options: GetOptions = GetOptions()
  ): Future[GetResult]

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def get(
    id: A,
    options: GetOptions = GetOptions()
  )(implicit ec: ExecutionContext): Future[T] =
    getResult(id, options).map(_.contentAs[JsValue].get.as[T])

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def getWithCAS(
    id: A,
    options: GetOptions = GetOptions()
  )(implicit ec: ExecutionContext): Future[(T, Long)] =
    getResult(id, options).map { r =>
      r.contentAs[JsValue].get.as[T] -> r.cas
    }

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def getOrElse(
    id: A,
    options: GetOptions = GetOptions()
  )(default: => T)(implicit ec: ExecutionContext): Future[T] =
    get(id, options).recover { case _: DocumentNotFoundException =>
      default
    }

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def getOrElseWithCAS(
    id: A,
    options: GetOptions = GetOptions()
  )(default: => T)(implicit ec: ExecutionContext): Future[(T, Long)] =
    getWithCAS(id, options).recover { case _: DocumentNotFoundException =>
      (default, -1)
    }

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def getOrUpdate(
    id: A,
    options: GetOptions = GetOptions()
  )(default: => T)(implicit ec: ExecutionContext): Future[T] =
    get(id, options).recoverWith { case _: DocumentNotFoundException =>
      setT(id, default)
    }

  /** @param ids Seq of document id or the param of JsCao1.key(a: A) */
  final def getBulk(ids: Seq[A])(
    implicit ec: ExecutionContext
  ): Future[Seq[T]] =
    Future.traverse(ids)(get(_, GetOptions()))

  /** @param ids Seq of document id or the param of JsCao1.key(a: A) */
  final def getBulkWithCAS(ids: Seq[A])(
    implicit ec: ExecutionContext
  ): Future[Seq[(T, Long)]] =
    Future.traverse(ids)(getWithCAS(_, GetOptions()))

  /** @param ids Seq of document id or the param of JsCao1.key(a: A) */
  final def setBulk(ids: Seq[A], contents: Seq[T])(
    implicit ec: ExecutionContext
  ): Future[Seq[MutationResult]] =
    Future.traverse(ids zip contents) { case (a, t) => upsert(a, t) }

  @deprecated("Use upsert or replace. This `set` method use `upsert`", "10.0.0")
  final def set(
    id: A,
    content: T,
    options: UpsertOptions = UpsertOptions()
  ): Future[MutationResult] = upsert(id, content, options)

  /** @param id document id or the param of JsCao1.key(a: A)
    * @param content the object of your own type `T` ex T=`case class User(...)`
    *          to be replace into cb server
    */
  def upsert(
    id: A,
    content: T,
    options: UpsertOptions = UpsertOptions()
  ): Future[MutationResult]

  /** Replaces the contents of a full document, if it already exists.
    * @param id document id or the param of JsCao1.key(a: A)
    * @param content the object of your own type `T` ex T=`case class User(...)`
    *          to be replace into cb server
    */
  def replace(
    id: A,
    content: T,
    options: ReplaceOptions
  ): Future[MutationResult]

  /** Replaces the contents of a full document, if it already exists.
    * @param id document id or the param of JsCao1.key(a: A)
    * @param content the object of your own type `T` ex T=`case class User(...)`
    *          to be replace into cb server
    */
  final def replace(
    id: A,
    content: T,
    cas: Long = 0,
    durability: Durability = Disabled,
    timeout: Duration = Duration.MinusInf
  ): Future[MutationResult] = replace(
    id,
    content,
    ReplaceOptions().cas(cas).durability(durability).timeout(timeout)
  )

  @deprecated("Use replace", "10.0.0")
  final def update(id: A, content: T, cas: Long = 0): Future[MutationResult] =
    replace(id, content, cas)

  @deprecated("Use replace", "10.0.0")
  final def updateWithCAS(
    id: A,
    content: T,
    cas: Long = 0
  ): Future[MutationResult] =
    replace(id, content, cas)

  /** convenient method. = set(..).map(_ => t) */
  final def setT(
    id: A,
    content: T,
    options: UpsertOptions = UpsertOptions()
  )(implicit ec: ExecutionContext): Future[T] =
    upsert(id, content, options).map(_ => content)

  /** @param id document id or the param of JsCao1.key(a: A) */
  def remove(id: A, options: RemoveOptions): Future[MutationResult]

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def change(
    id: A,
    getOptions: GetOptions = GetOptions(),
    setOptions: UpsertOptions = UpsertOptions()
  )(
    f: Option[T] => T
  )(implicit ec: ExecutionContext): Future[MutationResult] = {
    // TODO https://docs.couchbase.com/scala-sdk/current/howtos/kv-operations.html#retrying-on-cas-failures
    get(id, getOptions)
      .map(Option(_))
      .recover { case _: DocumentNotFoundException => None }
      .flatMap { o => upsert(id, f(o), setOptions) }
  }

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def flatChange(
    id: A,
    getOptions: GetOptions = GetOptions(),
    setOptions: UpsertOptions = UpsertOptions()
  )(
    f: Option[T] => Future[T]
  )(implicit ec: ExecutionContext): Future[MutationResult] =
    get(id, getOptions)
      .map(Option(_))
      .recover { case _: DocumentNotFoundException => None }
      .flatMap(f)
      .flatMap(upsert(id, _, setOptions))

  /** @param ids Seq of document id or the param of JsCao1.key(a: A) */
  final def changeBulk(ids: Seq[A])(
    f: Option[T] => T
  )(implicit ec: ExecutionContext): Future[Seq[MutationResult]] =
    Future.traverse(ids)(change(_)(f))

  /** @param ids Seq of document id or the param of JsCao1.key(a: A) */
  final def flatChangeBulk(ids: Seq[A])(
    f: Option[T] => Future[T]
  )(implicit ec: ExecutionContext): Future[Seq[MutationResult]] =
    Future.traverse(ids)(flatChange(_)(f))
}
