package com.sandinh.couchbase

import com.couchbase.client.java.bucket.AsyncBucketManager
import com.couchbase.client.java.document.{Document, JsonLongDocument}
import com.couchbase.client.java.query.{AsyncQueryResult, Query}
import com.couchbase.client.java.view.{AsyncViewResult, ViewQuery}
import com.couchbase.client.java.{AsyncBucket, PersistTo, ReplicaMode, ReplicateTo}
import com.sandinh.couchbase.document.JsDocument
import com.sandinh.rx.Scala._
import play.api.libs.json.Reads
import rx.lang.scala.Observable
import scala.reflect.ClassTag

final class ScalaBucket(val asJava: AsyncBucket) {
  @inline def name = asJava.name()

  //  def get(id: String): Observable[JsDocument] = asJava.get(id, classOf[JsDocument]).asScala
  def get[D <: Document[_]](id: String)(implicit tag: ClassTag[D]): Observable[D] =
    asJava.get(id, tag.runtimeClass.asInstanceOf[Class[D]]).asScala

  //  def getFromReplica(id: String, tpe: ReplicaMode): Observable[JsDocument] = asJava.getFromReplica(id, tpe, classOf[JsDocument]).asScala
  def getFromReplica[D <: Document[_]](id: String, tpe: ReplicaMode)(implicit tag: ClassTag[D]): Observable[D] =
    asJava.getFromReplica(id, tpe, tag.runtimeClass.asInstanceOf[Class[D]]).asScala

  //  def getAndLock(id: String, lockTime: Int): Observable[JsDocument] = asJava.getAndLock(id, lockTime, classOf[JsDocument]).asScala
  def getAndLock[D <: Document[_]](id: String, lockTime: Int)(implicit tag: ClassTag[D]): Observable[D] =
    asJava.getAndLock(id, lockTime, tag.runtimeClass.asInstanceOf[Class[D]]).asScala

  //  def getAndTouch(id: String, expiry: Int): Observable[JsDocument] = asJava.getAndTouch(id, expiry, classOf[JsDocument]).asScala
  def getAndTouch[D <: Document[_]](id: String, expiry: Int)(implicit tag: ClassTag[D]): Observable[D] =
    asJava.getAndTouch(id, expiry, tag.runtimeClass.asInstanceOf[Class[D]]).asScala

  def insert[D <: Document[_]](document: D): Observable[D] = asJava.insert(document).asScala
  def insert[D <: Document[_]](document: D, persistTo: PersistTo): Observable[D] = asJava.insert(document, persistTo).asScala
  def insert[D <: Document[_]](document: D, replicateTo: ReplicateTo): Observable[D] = asJava.insert(document, replicateTo).asScala
  def insert[D <: Document[_]](document: D, persistTo: PersistTo, replicateTo: ReplicateTo): Observable[D] =
    asJava.insert(document, persistTo, replicateTo).asScala

  def upsert[D <: Document[_]](document: D): Observable[D] = asJava.upsert(document).asScala
  def upsert[D <: Document[_]](document: D, persistTo: PersistTo): Observable[D] = asJava.upsert(document, persistTo).asScala
  def upsert[D <: Document[_]](document: D, replicateTo: ReplicateTo): Observable[D] = asJava.upsert(document, replicateTo).asScala
  def upsert[D <: Document[_]](document: D, persistTo: PersistTo, replicateTo: ReplicateTo): Observable[D] =
    asJava.upsert(document, persistTo, replicateTo).asScala

  def replace[D <: Document[_]](document: D): Observable[D] = asJava.replace(document).asScala
  def replace[D <: Document[_]](document: D, persistTo: PersistTo): Observable[D] = asJava.replace(document, persistTo).asScala
  def replace[D <: Document[_]](document: D, replicateTo: ReplicateTo): Observable[D] = asJava.replace(document, replicateTo).asScala
  def replace[D <: Document[_]](document: D, persistTo: PersistTo, replicateTo: ReplicateTo): Observable[D] =
    asJava.replace(document, persistTo, replicateTo).asScala

  def remove[D <: Document[_]](document: D): Observable[D] = asJava.remove(document).asScala
  def remove[D <: Document[_]](document: D, persistTo: PersistTo): Observable[D] = asJava.remove(document, persistTo).asScala
  def remove[D <: Document[_]](document: D, replicateTo: ReplicateTo): Observable[D] = asJava.remove(document, replicateTo).asScala
  def remove[D <: Document[_]](document: D, persistTo: PersistTo, replicateTo: ReplicateTo): Observable[D] =
    asJava.remove(document, persistTo, replicateTo).asScala

  //  def remove(id: String): Observable[JsDocument] = asJava.remove(id, classOf[JsDocument]).asScala
  //  def remove(id: String, persistTo: PersistTo): Observable[JsDocument] = asJava.remove(id, persistTo, classOf[JsDocument]).asScala
  //  def remove(id: String, replicateTo: ReplicateTo): Observable[JsDocument] = asJava.remove(id, replicateTo, classOf[JsDocument]).asScala
  //  def remove(id: String, persistTo: PersistTo, replicateTo: ReplicateTo): Observable[JsDocument] =
  //    asJava.remove(id, persistTo, replicateTo, classOf[JsDocument]).asScala

  def remove[D <: Document[_]](id: String)(implicit tag: ClassTag[D]) = asJava.remove(id, tag.runtimeClass.asInstanceOf[Class[D]]).asScala
  def remove[D <: Document[_]](id: String, persistTo: PersistTo)(implicit tag: ClassTag[D]): Observable[D] =
    asJava.remove(id, persistTo, tag.runtimeClass.asInstanceOf[Class[D]]).asScala
  def remove[D <: Document[_]](id: String, replicateTo: ReplicateTo)(implicit tag: ClassTag[D]): Observable[D] =
    asJava.remove(id, replicateTo, tag.runtimeClass.asInstanceOf[Class[D]]).asScala
  def remove[D <: Document[_]](id: String, persistTo: PersistTo, replicateTo: ReplicateTo)(implicit tag: ClassTag[D]): Observable[D] =
    asJava.remove(id, persistTo, replicateTo, tag.runtimeClass.asInstanceOf[Class[D]]).asScala

  def query(query: ViewQuery): Observable[AsyncViewResult] = asJava.query(query).asScala
  def query(query: Query): Observable[AsyncQueryResult] = asJava.query(query).asScala
  def query(query: String): Observable[AsyncQueryResult] = asJava.query(query).asScala

  def unlock(id: String, cas: Long): Observable[Boolean] = asJava.unlock(id, cas).asScala.map(_.booleanValue)
  def unlock[D <: Document[_]](document: D): Observable[Boolean] = asJava.unlock(document).asScala.map(_.booleanValue)

  def touch(id: String, expiry: Int): Observable[Boolean] = asJava.touch(id, expiry).asScala.map(_.booleanValue)
  def touch[D <: Document[_]](document: D): Observable[Boolean] = asJava.touch(document).asScala.map(_.booleanValue)

  def counter(id: String, delta: Long): Observable[Long] = asJava.counter(id, delta).asScala.map(_.content.longValue)
  def counter(id: String, delta: Long, initial: Long, expiry: Int = 0): Observable[Long] =
    asJava.counter(id, delta, initial, expiry).asScala.map(_.content.longValue)

  /** @note the result document has expiry = 0 & content = null
    * @see https://github.com/couchbase/couchbase-java-client/commit/6f0c7cf2247a3ef99a71ef2edd67f1077e4646e0 */
  def append[D <: Document[_]](document: D): Observable[D] = asJava.append(document).asScala
  /** @note the result document has expiry = 0 & content = null
    * @see https://github.com/couchbase/couchbase-java-client/commit/6f0c7cf2247a3ef99a71ef2edd67f1077e4646e0 */
  def prepend[D <: Document[_]](document: D): Observable[D] = asJava.prepend(document).asScala

  def bucketManager: Observable[AsyncBucketManager] = asJava.bucketManager().asScala

  def close(): Observable[Boolean] = asJava.close().asScala.map(_.booleanValue)

  ///////////////////
  def read[T: Reads](id: String): Observable[T] = this.get[JsDocument](id).map(_.content.as[T])
}
