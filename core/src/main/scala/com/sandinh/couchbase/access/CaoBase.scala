package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import com.couchbase.client.java.error.DocumentDoesNotExistException
import com.sandinh.couchbase.ScalaBucket
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global // TODO remove

/** Base class for Couchbase Access Object.
  * This class permit we interact (get/set/update/..) with couchbase server
  * through a typed interface. Ex, given a class `D <: Document`
  * (that means D is a subclass of [[com.couchbase.client.java.document.Document]])
  * instead of {{{
  *   // com.couchbase.client.java.AsyncBucket#get
  *   get(id: String): Observable[D]
  *   // com.couchbase.client.java.AsyncBucket#upsert
  *   upsert(document: D): Observable[D]
  * }}}
  * , we can: {{{
  *   get(id: String): Future[T]
  *   set(id: String, t: T): Future[D]
  * }}}
  * With T is your own type, ex `case class User(name: String, age: Int)`
  *
  * To able to do that, we need implement this class' 2 abstract methods: {{{
  *   def reads(u: U): T
  *   def writes(t: T): U
  * }}}
  * @see [[WithCaoKey1]]
  * @tparam T type that will be encoded before upsert using `writes(t: T): U`,
  *           and decoded after get using `reads(u: U): T`
  * @tparam U the Document's content type, ex JsValue, primitive types,..
  *           See classes that implement `com.couchbase.client.java.document.Document` for all available type
  *           (of course you can implement your own)
  * @tparam D the Document type
  */
abstract class CaoBase[T, U, D <: Document[U]: ClassTag](bucket: ScalaBucket)
    extends CaoTrait[T, String, U, D] {

  /** @param id document id */
  final def get(id: String): Future[T] =
    bucket.get[D](id).map(d => reads(d.content))

  /** @param id document id */
  final def getWithCAS(id: String): Future[DocumentCAS] =
    bucket.get[D](id).map(d => (reads(d.content), d.cas()))

  /** @param id document id
    * @param t the object of your own type `T` ex T=`case class User(...)`
    *          to be upsert into cb server
    */
  final def set(id: String, t: T): Future[D] =
    bucket.upsert(createDoc(id, expiry(), writes(t)))

  /** @param id document id
    * @param t the object of your own type `T` ex T=`case class User(...)`
    *          to be upsert into cb server
    */
  final def update(id: String, t: T, cas: Long = 0): Future[D] =
    bucket.replace(createDoc(id, expiry(), writes(t), cas))

  /** @param id document id */
  final def remove(id: String): Future[D] = bucket.remove[D](id)
}

/** Common interface for [[CaoBase]] and [[WithCaoKey1]] */
private[access] trait CaoTrait[T, A, U, D <: Document[U]] {
  protected def expiry(): Int = 0

  protected def reads(u: U): T
  protected def writes(t: T): U

  final type DocumentCAS = (T, Long)

  protected def createDoc(
    id: String,
    expiry: Int,
    content: U,
    cas: Long = 0L
  ): D

  def get(a: A): Future[T]
  def getWithCAS(a: A): Future[(T, Long)]

  /** @param a document id or the param of WithCaoKey1.key(a: A) */
  final def getOrElse(a: A)(default: => T): Future[T] = get(a).recover {
    case _: DocumentDoesNotExistException => default
  }

  /** @param a document id or the param of WithCaoKey1.key(a: A) */
  final def getOrElseWithCAS(a: A)(default: => T): Future[DocumentCAS] =
    getWithCAS(a).recover { case _: DocumentDoesNotExistException =>
      (default, -1)
    }

  /** @param a document id or the param of WithCaoKey1.key(a: A) */
  final def getOrUpdate(a: A)(default: => T): Future[T] = get(a).recoverWith {
    case _: DocumentDoesNotExistException => setT(a, default)
  }

  final def getBulk(aa: Seq[A]): Future[Seq[T]] = Future.traverse(aa)(get)
  final def getBulkWithCAS(aa: Seq[A]): Future[Seq[DocumentCAS]] =
    Future.traverse(aa)(getWithCAS)

  final def setBulk(aa: Seq[A], tt: Seq[T]): Future[Seq[D]] =
    Future.traverse(aa zip tt) { case (a, t) => set(a, t) }

  def set(a: A, t: T): Future[D]
  def update(id: A, t: T, cas: Long = 0): Future[D]

  /** convenient method. = set(..).map(_ => t) */
  final def setT(a: A, t: T): Future[T] = set(a, t).map(_ => t)

  def remove(a: A): Future[D]

  final def change(a: A)(f: Option[T] => T): Future[D] = get(a)
    .map(Option(_))
    .recover { case _: DocumentDoesNotExistException => None }
    .flatMap { o => set(a, f(o)) }

  final def flatChange(a: A)(f: Option[T] => Future[T]): Future[D] = get(a)
    .map(Option(_))
    .recover { case _: DocumentDoesNotExistException => None }
    .flatMap(f)
    .flatMap(set(a, _))

  final def changeBulk(aa: Seq[A])(f: Option[T] => T): Future[Seq[D]] =
    Future.traverse(aa)(change(_)(f))

  final def flatChangeBulk(aa: Seq[A])(
    f: Option[T] => Future[T]
  ): Future[Seq[D]] = Future.traverse(aa)(flatChange(_)(f))
}
