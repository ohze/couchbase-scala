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
private[access] trait WithCaoKey1[T, A, U, D <: Document[U]]
    extends CaoTrait[T, A, U, D] {
  def self: CaoBase[T, U, D]
  protected def key(a: A): String

  final def get(a: A): Future[T] = self.get(key(a))

  final def getWithCAS(a: A): Future[(T, Long)] = self.getWithCAS(key(a))

  final def set(a: A, t: T): Future[D] = self.set(key(a), t)
  final def update(a: A, t: T, cas: Long = 0): Future[D] =
    self.update(key(a), t, cas)

  /** @deprecated use `update` instead */
  final def updateWithCAS(a: A, t: T, cas: Long = 0): Future[D] =
    self.update(key(a), t, cas)

  final def remove(a: A): Future[D] = self.remove(key(a))
}
