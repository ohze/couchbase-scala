package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import com.couchbase.client.java.error.DocumentDoesNotExistException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** This trait has a similar interface as [[CaoBase]] but:
  * + Has an additional abstract method `def key(a: A): String`
  * + When interact (get/set/update/..) with couchbase server, instead of {{{
  *   get(docId: String): Future[T]
  *   set(docId: String, t: T): Future[D]
  * }}} as in CaoBase, we can: {{{
  *   get(a): Future[T]
  *   set(a: A, t: T): Future[D]
  * }}}
  * So, instead of working with `docId: String` directly,
  * we can create a Cao (Couchbase Access Object) for own type T (ex UserCao for T=`case class User(uid: Long,...)`)
  * , and in `UserCao`, we define `def key(uid: Long) = s"my_prefix$id"`
  * , then using set/get/.. with `uid: Long` instead of `docId: String`
  * To able to do that, we need implement this class' abstract methods: `reads(u: U): T` and `writes(t: T): U`
  *
  * @tparam T type that will be encoded before upsert using `writes(t: T): U`,
  *           and decoded after get using `reads(u: U): T`
  * @tparam U the Document's content type, ex JsValue, primitive types,..
  *           See classes that implement `com.couchbase.client.java.document.Document` for all available type
  *           (of course you can implement your own)
  * @tparam D the Document type
  */
// internal TODO make this trait `private[access]`
// TODO move some method impl, such as `setBulk`
//  to `CaoTrait` trait to enrich CaoBase
trait WithCaoKey1[T, A, U, D <: Document[U]] extends CaoTrait[T, A, U, D] {
  private[access] def self: CaoBase[T, U, D]
  protected def key(a: A): String

  final def get(a: A): Future[T] = self.get(key(a))
  final def getOrElse(a: A)(default: => T): Future[T] =
    self.getOrElse(key(a))(default)
  final def getBulk(aa: Seq[A]): Future[Seq[T]] = Future.traverse(aa)(get)

  final def getWithCAS(a: A): Future[(T, Long)] = self.getWithCAS(key(a))
  final def getOrElseWithCAS(a: A)(default: => T): Future[DocumentCAS] =
    self.getOrElseWithCAS(key(a))(default)
  final def getBulkWithCAS(aa: Seq[A]): Future[Seq[DocumentCAS]] =
    Future.traverse(aa)(getWithCAS)

  final def set(a: A, t: T): Future[D] = self.set(key(a), t)
  final def update(a: A, t: T, cas: Long = 0): Future[D] =
    self.update(key(a), t, cas)

  /** @deprecated use `update` instead */
  final def updateWithCAS(a: A, t: T, cas: Long = 0): Future[D] =
    self.update(key(a), t, cas)

  /** @inheritdoc */
  final def setT(a: A, t: T): Future[T] = self.set(key(a), t).map(_ => t)
  final def setBulk(aa: Seq[A], tt: Seq[T]): Future[Seq[D]] =
    Future.traverse(aa zip tt) { case (a, t) =>
      set(a, t)
    }

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

  final def remove(a: A): Future[D] = self.remove(key(a))
}
