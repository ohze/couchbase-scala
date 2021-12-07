package com.sandinh.couchbase

import com.couchbase.client.scala.codec.{JsonDeserializer, JsonSerializer}
import com.couchbase.client.scala.kv.{
  AppendOptions,
  CounterResult,
  GetAllReplicasOptions,
  GetAndLockOptions,
  GetAndTouchOptions,
  GetAnyReplicaOptions,
  GetOptions,
  GetReplicaResult,
  GetResult,
  IncrementOptions,
  InsertOptions,
  MutationResult,
  PrependOptions,
  RemoveOptions,
  ReplaceOptions,
  TouchOptions,
  UnlockOptions,
  UpsertOptions
}
import com.couchbase.client.scala.manager.bucket.AsyncBucketManager
import com.couchbase.client.scala.manager.view.AsyncViewIndexManager
import com.couchbase.client.scala.query.{
  QueryOptions,
  QueryParameters,
  QueryResult
}
import com.couchbase.client.scala.view.{ViewOptions, ViewResult}
import com.couchbase.client.scala.{AsyncBucket, AsyncCluster, AsyncCollection}
import play.api.libs.json.{JsValue, Reads}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.WeakTypeTag

final class CBBucket(val underlying: AsyncBucket, val cluster: AsyncCluster) {
  @inline def name: String = underlying.name

  lazy val defaultCol: AsyncCollection = underlying.defaultCollection

  @deprecated("Use underlying", "10.0.0")
  def asJava: AsyncBucket = underlying

  def getJsT[T: Reads](
    id: String,
    options: GetOptions = GetOptions()
  )(implicit ec: ExecutionContext): Future[T] =
    get(id, options).map(_.contentAs[JsValue].get.as[T])

  def get(
    id: String,
    options: GetOptions = GetOptions()
  ): Future[GetResult] = defaultCol.get(id, options)

  /** usage: {{{
    * import com.sandinh.couchbase.Implicits._
    *
    * bucket.getT[String](id)
    * bucket.getT[JsValue](id)
    * }}}
    */
  def getT[T](id: String)(
    implicit ec: ExecutionContext,
    ser: JsonDeserializer[T],
    tt: WeakTypeTag[T],
    tag: ClassTag[T]
  ): Future[T] =
    get(id).map(_.contentAs[T].get)

  def getAnyReplica(
    id: String,
    options: GetAnyReplicaOptions = GetAnyReplicaOptions()
  ): Future[GetReplicaResult] = defaultCol.getAnyReplica(id, options)

  def getAllReplicas(
    id: String,
    options: GetAllReplicasOptions = GetAllReplicasOptions()
  ): Seq[Future[GetReplicaResult]] = defaultCol.getAllReplicas(id, options)

  def getAndLock(
    id: String,
    lockTime: Duration,
    options: GetAndLockOptions = GetAndLockOptions()
  ): Future[GetResult] = defaultCol.getAndLock(id, lockTime, options)

  def getAndTouch(
    id: String,
    expiry: Duration,
    options: GetAndTouchOptions = GetAndTouchOptions()
  ): Future[GetResult] = defaultCol.getAndTouch(id, expiry, options)

  /** Inserts a full document into this collection, if it does not exist already.
    * @see [[upsert]], [[replace]]
    */
  def insert[T: JsonSerializer](
    id: String,
    content: T,
    options: InsertOptions = InsertOptions()
  ): Future[MutationResult] = defaultCol.insert(id, content, options)

  /** Upserts the contents of a full document in this collection.
    * @see [[insert]], [[replace]]
    */
  def upsert[T: JsonSerializer](
    id: String,
    content: T,
    options: UpsertOptions = UpsertOptions()
  ): Future[MutationResult] = defaultCol.upsert(id, content, options)

  /** Replaces the contents of a full document in this collection, if it already exists.
    * @see [[insert]], [[upsert]]
    */
  def replace[T: JsonSerializer](
    id: String,
    content: T,
    options: ReplaceOptions = ReplaceOptions()
  ): Future[MutationResult] = defaultCol.replace(id, content, options)

  /** Removes a document from this collection, if it exists. */
  def remove(
    id: String,
    options: RemoveOptions = RemoveOptions()
  ): Future[MutationResult] = defaultCol.remove(id, options)

  def viewQuery(
    designDoc: String,
    viewName: String,
    options: ViewOptions = ViewOptions()
  ): Future[ViewResult] = underlying.viewQuery(designDoc, viewName, options)

  def query(statement: String, options: QueryOptions): Future[QueryResult] =
    cluster.query(statement, options)

  def query(
    statement: String,
    parameters: QueryParameters = QueryParameters.None,
    timeout: Duration =
      cluster.env.core.timeoutConfig.queryTimeout().toNanos.nanos,
    adhoc: Boolean = true
  ): Future[QueryResult] =
    cluster.query(statement, parameters, timeout, adhoc)

  def unlock(
    id: String,
    cas: Long,
    options: UnlockOptions = UnlockOptions()
  ): Future[Unit] = defaultCol.unlock(id, cas, options)

  def touch(
    id: String,
    expiry: Duration,
    options: TouchOptions = TouchOptions()
  ): Future[MutationResult] = defaultCol.touch(id, expiry, options)

  def counter(
    id: String,
    delta: Long,
    options: IncrementOptions
  ): Future[CounterResult] = defaultCol.binary.increment(id, delta, options)

  def counter(
    id: String,
    delta: Long,
    initial: Long,
    expiry: Duration = null
  ): Future[CounterResult] = defaultCol.binary.increment(
    id,
    delta,
    IncrementOptions().initial(initial).expiry(expiry)
  )

  def append(
    id: String,
    content: Array[Byte],
    options: AppendOptions = AppendOptions()
  ): Future[MutationResult] = defaultCol.binary.append(id, content, options)

  def prepend(
    id: String,
    content: Array[Byte],
    options: PrependOptions = PrependOptions()
  ): Future[MutationResult] = defaultCol.binary.prepend(id, content, options)

  def viewIndexes: AsyncViewIndexManager = underlying.viewIndexes

  def bucketManager: AsyncBucketManager = cluster.buckets
}
