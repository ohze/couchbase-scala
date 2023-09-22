package com.sandinh.couchbase.access

import com.couchbase.client.scala.kv.OptionsConvert._
import com.couchbase.client.core.error.{
  CasMismatchException,
  DocumentExistsException,
  DocumentNotFoundException
}
import com.couchbase.client.scala.durability.Durability
import com.couchbase.client.scala.durability.Durability.Disabled
import com.couchbase.client.scala.kv.{
  GetOptions,
  GetResult,
  InsertOptions,
  MutationResult,
  RemoveOptions,
  ReplaceOptions,
  UpsertOptions
}
import play.api.libs.json.{Format, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

/** Common interface for [[JsCao]] and [[JsCao1]]
  *
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

  /** Inserts a full document into this collection, if it does not exist already.
    * @param id document id or the param of JsCao1.key(a: A)
    * @param content the object of your own type `T` ex T=`case class User(...)`
    *          to be insert into cb server
    * @see [[upsert]], [[replace]]
    */
  def insert(
    id: A,
    content: T,
    options: InsertOptions = InsertOptions()
  ): Future[MutationResult]

  /** Upserts the contents of a full document in this collection.
    * @param id document id or the param of JsCao1.key(a: A)
    * @param content the object of your own type `T` ex T=`case class User(...)`
    *          to be upsert into cb server
    * @see [[insert]], [[replace]]
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
    * @see [[insert]], [[upsert]]
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
    * @see [[insert]], [[upsert]]
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

  /** Removes a document from this collection, if it exists.
    * @param id document id or the param of JsCao1.key(a: A)
    */
  def remove(id: A, options: RemoveOptions): Future[MutationResult]

  /** @param id document id or the param of JsCao1.key(a: A)
    * @note This method will retry on CasMismatchException
    */
  final def change(
    id: A,
    getOptions: GetOptions = GetOptions(),
    setOptions: InsertOptions = InsertOptions()
  )(
    f: Option[T] => T
  )(implicit ec: ExecutionContext): Future[MutationResult] =
    getWithCAS(id, getOptions)
      .map(Option(_))
      .recover { case _: DocumentNotFoundException => None }
      .flatMap {
        case None =>
          insert(id, f(None), setOptions)
            .recoverWith { case _: DocumentExistsException =>
              change(id, getOptions, setOptions)(f)
            }
        case Some((t, cas)) =>
          replace(id, f(Some(t)), setOptions.toReplaceOptions(cas))
            .recoverWith { case _: CasMismatchException =>
              change(id, getOptions, setOptions)(f)
            }
      }

  /** @param id document id or the param of JsCao1.key(a: A) */
  final def flatChange(
    id: A,
    getOptions: GetOptions = GetOptions(),
    setOptions: InsertOptions = InsertOptions()
  )(
    f: Option[T] => Future[T]
  )(implicit ec: ExecutionContext): Future[MutationResult] =
    getWithCAS(id, getOptions)
      .map(Option(_))
      .recover { case _: DocumentNotFoundException => None }
      .flatMap {
        case None =>
          f(None).flatMap {
            insert(id, _, setOptions).recoverWith {
              case _: DocumentExistsException =>
                flatChange(id, getOptions, setOptions)(f)
            }
          }
        case Some((t, cas)) =>
          f(Some(t))
            .flatMap(replace(id, _, setOptions.toReplaceOptions(cas)))
            .recoverWith { case _: CasMismatchException =>
              flatChange(id, getOptions, setOptions)(f)
            }
      }

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
