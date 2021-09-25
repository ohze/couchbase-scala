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

  /** @param id document id */
  // TODO make final
  def getOrElse(id: String)(default: => T): Future[T] = get(id).recover {
    case _: DocumentDoesNotExistException => default
  }

  /** @param id document id */
  def getOrElseWithCAS(id: String)(default: => T): Future[DocumentCAS] =
    getWithCAS(id).recover { case _: DocumentDoesNotExistException =>
      (default, -1)
    }

  /** @param id document id */
  // not override. see the comment in CaoTrait
  def getOrUpdate(id: String)(default: => T): Future[T] = get(id).recoverWith {
    case _: DocumentDoesNotExistException => setT(id, default)
  }

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

  /** @inheritdoc */
  final def setT(id: String, t: T): Future[T] = set(id, t).map(_ => t)

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
  def getOrElse(a: A)(default: => T): Future[T]
  def getOrElseWithCAS(a: A)(default: => T): Future[DocumentCAS]

  // Because WithCaoKey1 in couchbase-scala 7.4.5 don't have this method
  // So, to prevent conflict and maybe not binary compatible case,
  // ex `XxCao extends JsCao1[T, String]` which is a `WithCaoKey1T, A, ..]`
  // with `type A = String`
  // => we don't add this method here.
  // TODO add this method later
  // TODO implement some methods of this trait in this trait
  //  instead of duplicating implementation in both CaoBase and WithCaoKey1
  //def getOrUpdate(id: A)(default: => T): Future[T]

  def set(a: A, t: T): Future[D]
  def update(id: A, t: T, cas: Long = 0): Future[D]

  /** convenient method. = set(..).map(_ => t) */
  def setT(a: A, t: T): Future[T]
  def remove(a: A): Future[D]
}
