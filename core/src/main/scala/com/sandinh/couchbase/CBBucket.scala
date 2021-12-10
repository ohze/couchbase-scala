package com.sandinh.couchbase

import com.couchbase.client.scala.codec.{
  JsonDeserializer,
  JsonSerializer,
  Transcoder
}
import com.couchbase.client.scala.durability.Durability
import com.couchbase.client.scala.durability.Durability.Disabled
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

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.duration.Duration.MinusInf
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.WeakTypeTag
import Implicits.DocNotExistFuture

/** @define CounterDoc      though it is common to use Couchbase to store exclusively JSON, Couchbase is actually
  *                         agnostic to what is stored.  It is possible to use a document as a 'counter' - e.g. it
  *                         stores an integer.  This is useful for use-cases such as implementing
  *                         AUTO_INCREMENT-style functionality, where each new document can be given a unique
  *                         monotonically increasing id.
  * @define OnlyBinary      This method should not be used with JSON documents.  This operates
  *                         at the byte level and is unsuitable for dealing with JSON documents. Use this method only
  *                         when explicitly dealing with binary or UTF-8 documents. It may invalidate an existing JSON
  *                         document.
  * @define OnlyCounter     this method should not be used with JSON documents.  Use this method only
  *                         when explicitly dealing with counter documents. It may invalidate an existing JSON
  *                         document.
  * @define Id              the unique identifier of the document
  * @define CAS             Couchbase documents all have a CAS (Compare-And-Set) field, a simple integer that allows
  *                         optimistic concurrency - e.g. it can detect if another agent has modified a document
  *                         in-between this agent getting and modifying the document.  See
  *                         [[https://docs.couchbase.com/scala-sdk/1.0/howtos/json.html these JSON docs]] for a full
  *                         description.  The default is 0, which disables CAS checking.
  * @define Timeout         when the operation will timeout. This will default to `timeoutConfig().kvTimeout()` in the
  *                         provided [[com.couchbase.client.scala.env.ClusterEnvironment]].
  * @define ErrorHandling   any `scala.util.control.NonFatal` error returned will derive ultimately from
  *                         `com.couchbase.client.core.error.CouchbaseException`.  See
  *                         [[https://docs.couchbase.com/scala-sdk/1.0/howtos/error-handling.html the error handling docs]]
  *                         for more detail.
  * @define SupportedTypes this can be of any type for which an implicit
  *                        `com.couchbase.client.scala.codec.Conversions.JsonSerializer` can be found: a list
  *                        of types that are supported 'out of the box' is available at
  *                        [[https://docs.couchbase.com/scala-sdk/1.0/howtos/json.html these JSON docs]]
  * @define Durability      writes in Couchbase are written to a single node, and from there the Couchbase Server will
  *                         take care of sending that mutation to any configured replicas.  This parameter provides
  *                         some control over ensuring the success of the mutation's replication.  See
  *                         [[com.couchbase.client.scala.durability.Durability]]
  *                         for a detailed discussion.
  * @define Options         configure options that affect this operation
  * @define OrNotFound      or fail with a `CouchbaseException`.
  *                         This could be [[com.couchbase.client.core.error.DocumentNotFoundException]],
  *                         indicating the document could not be found.
  * @define ExpiryNote On mutations if this is left at the default (0), then any expiry
  *       will be removed and the document will never expire. If the application wants to
  *       preserve expiration then they should use the `withExpiration` parameter on any gets,
  *       and provide the returned expiration parameter to any mutations.
  *
  * @define ExpiryTimeNote If both `expiry` and `expiryTime` are provided then `expiryTime` is used -
  *
  *                        see [[com.couchbase.client.scala.util.ExpiryUtil.expiryActual]]
  * @define Transcoder control over how JSON is converted and stored on the Couchbase Server.
  *                    If not specified it will default to to `transcoder()` in the
  *                    [[com.couchbase.client.scala.env.ClusterEnvironment]]
  */
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

  /** Fetches a full document from this collection.
    *
    * This overload provides only the most commonly used options.  If you need to configure something more
    * esoteric, use the overload that takes an [[com.couchbase.client.scala.kv.GetOptions]] instead, which supports all available options.
    *
    * @param id         $Id
    * @param timeout    $Timeout
    * @param transcoder $Transcoder
    * @param withExpiry Couchbase documents optionally can have an expiration field set, e.g. when they will
    *                   automatically expire.  For efficiency reasons, by default the value of this expiration
    *                   field is not fetched upon getting a document.  If expiry is being used, then set this
    *                   field to true to ensure the expiration is fetched.  This will not only make it available
    *                   in the returned result, but also ensure that the expiry is available to use when mutating
    *                   the document, to avoid accidentally resetting the expiry to the default of 0.
    * @param project    Projection is an advanced feature allowing one or more fields to be fetched from a JSON
    *                   document, and the results combined into a `JsonObject` result.
    *
    *                   It combines the efficiency of a Sub-Document fetch, in that only specific fields need to be retrieved, with
    *                   the ease-of-handling of a regular fetch, in that the results can be handled as one JSON.
    * @return Future success with a `GetResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def get(
    id: String,
    timeout: Duration = MinusInf, // MinusInf will be converted to kvReadTimeout
    transcoder: Transcoder = null,
    withExpiry: Boolean = false,
    project: Seq[String] = Nil
  ): Future[GetResult] = defaultCol.get(
    id,
    GetOptions(
      withExpiry,
      project,
      timeout,
      transcoder = Option(transcoder)
    )
  )

  /** See doc of the other overload method */
  def get(
    id: String,
    options: GetOptions
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

  /** Retrieves any available version of the document.
    *
    * The application should default to using `.get()` instead.  This method is intended for high-availability
    * situations where, say, a `.get()` operation has failed, and the
    * application wants to return any - even possibly stale - data as soon as possible.
    *
    * Under the hood this sends a request to all configured replicas for the document, including the active, and
    * whichever returns first is returned.
    *
    * @param id            $Id
    * @param timeout       $Timeout
    * @param transcoder $Transcoder
    * @return Future success with a `GetReplicaResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def getAnyReplica(
    id: String,
    timeout: Duration = MinusInf, // MinusInf will be converted to kvReadTimeout
    transcoder: Transcoder = null,
  ): Future[GetReplicaResult] = defaultCol.getAnyReplica(
    id,
    GetAnyReplicaOptions(
      timeout,
      transcoder = Option(transcoder)
    )
  )

  /** See doc of the other overload method */
  def getAnyReplica(
    id: String,
    options: GetAnyReplicaOptions
  ): Future[GetReplicaResult] = defaultCol.getAnyReplica(id, options)

  /** Retrieves all available versions of the document.
    *
    * The application should default to using `.get()` instead.  This method is intended for advanced scenarios,
    * including where a particular write has ambiguously failed (e.g. it may or may not have succeeded), and the
    * application wants to attempt manual verification and resolution.
    *
    * @param id         $Id
    * @param timeout    $Timeout
    * @param transcoder $Transcoder
    * @return Future success with a `GetReplicaResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def getAllReplicas(
    id: String,
    timeout: Duration = MinusInf, // MinusInf will be converted to kvReadTimeout
    transcoder: Transcoder = null,
  ): Seq[Future[GetReplicaResult]] = defaultCol.getAllReplicas(
    id,
    GetAllReplicasOptions(
      timeout,
      transcoder = Option(transcoder)
    )
  )

  /** See doc of the other overload method */
  def getAllReplicas(
    id: String,
    options: GetAllReplicasOptions
  ): Seq[Future[GetReplicaResult]] = defaultCol.getAllReplicas(id, options)

  /** Fetches a full document from this collection, and simultaneously lock the document from writes.
    *
    * The CAS value returned in the [[com.couchbase.client.scala.kv.GetResult]] is the document's 'key':
    * during the locked period, the document may only be modified by providing this CAS.
    * @param id         $Id
    * @param lockTime   how long to lock the document for
    * @param timeout    $Timeout
    * @param transcoder $Transcoder
    * @return Future success with a `GetResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def getAndLock(
    id: String,
    lockTime: Duration,
    timeout: Duration = MinusInf, // MinusInf will be converted to kvReadTimeout
    transcoder: Transcoder = null,
  ): Future[GetResult] = defaultCol.getAndLock(
    id,
    lockTime,
    GetAndLockOptions(
      timeout,
      transcoder = Option(transcoder)
    )
  )

  /** See doc of the other overload method */
  def getAndLock(
    id: String,
    lockTime: Duration,
    options: GetAndLockOptions
  ): Future[GetResult] = defaultCol.getAndLock(id, lockTime, options)

  /** Fetches a full document from this collection, and simultaneously update the expiry value of the document.
    * @param id         $Id
    * @param expiry     $Expiry
    * @param timeout    $Timeout
    * @param transcoder $Transcoder
    * @return Future success with a `GetResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def getAndTouch(
    id: String,
    expiry: Duration,
    timeout: Duration = MinusInf, // MinusInf will be converted to kvReadTimeout
    transcoder: Transcoder = null,
  ): Future[GetResult] = defaultCol.getAndTouch(
    id,
    expiry,
    GetAndTouchOptions(
      timeout,
      transcoder = Option(transcoder)
    )
  )

  /** See doc of the other overload method */
  def getAndTouch(
    id: String,
    expiry: Duration,
    options: GetAndTouchOptions
  ): Future[GetResult] = defaultCol.getAndTouch(id, expiry, options)

  /** Inserts a full document into this collection, if it does not exist already.
    * @param id         $Id
    * @param content    $SupportedTypes
    * @param durability $Durability
    * @param timeout    $Timeout
    * @param transcoder $Transcoder
    * @param expiry should be used for any expiration times 30 days.
    *               If over that, use `expiryTime: Instant` instead.
    * @param expiryTime should be used for any expiration times >= 30 days.
    *                   If below that, use `expiry: Duration` instead.
    * @return Future success with a `MutationResult`, or fail with a `CouchbaseException`.
    *         This could be [[com.couchbase.client.core.error.DocumentExistsException]],
    *         indicating the document already exists.
    *
    *         $ErrorHandling
    * @note $ExpiryNote
    * @note $ExpiryTimeNote
    * @see [[upsert]], [[replace]]
    */
  def insert[T: JsonSerializer](
    id: String,
    content: T,
    durability: Durability = Disabled,
    timeout: Duration = MinusInf,
    transcoder: Transcoder = null,
    expiry: Duration = null,
    expiryTime: Instant = null,
  ): Future[MutationResult] = defaultCol.insert(
    id,
    content,
    InsertOptions(
      durability,
      timeout,
      transcoder = Option(transcoder),
      expiry = expiry,
      expiryTime = Option(expiryTime),
    )
  )

  /** See doc of the other overload method */
  def insert[T: JsonSerializer](
    id: String,
    content: T,
    options: InsertOptions
  ): Future[MutationResult] = defaultCol.insert(id, content, options)

  /** Upserts the contents of a full document in this collection.
    *
    * Upsert here means to insert the document if it does not exist, or replace the content if it does.
    *
    * @param id         $Id
    * @param content    $SupportedTypes
    * @param durability $Durability
    * @param timeout    $Timeout
    * @param transcoder $Transcoder
    * @param expiry should be used for any expiration times 30 days.
    *               If over that, use `expiryTime: Instant` instead.
    * @param expiryTime should be used for any expiration times >= 30 days.
    *                   If below that, use `expiry: Duration` instead.
    * @param preserveExpiry Whether an existing document's expiry should be preserved.
    *                       Requires Couchbase Server 7.0 or later.
    * @return Future success with a `MutationResult`, or fail with a `CouchbaseException`.
    *
    *         $ErrorHandling
    * @note $ExpiryNote
    * @note $ExpiryTimeNote
    * @see [[insert]], [[replace]]
    */
  def upsert[T: JsonSerializer](
    id: String,
    content: T,
    durability: Durability = Disabled,
    timeout: Duration = MinusInf,
    transcoder: Transcoder = null,
    expiry: Duration = null,
    expiryTime: Instant = null,
    preserveExpiry: Boolean = false,
  ): Future[MutationResult] = defaultCol.upsert(
    id,
    content,
    UpsertOptions(
      durability,
      timeout,
      transcoder = Option(transcoder),
      expiry = expiry,
      expiryTime = Option(expiryTime),
      preserveExpiry = preserveExpiry
    )
  )

  /** See doc of the other overload method */
  def upsert[T: JsonSerializer](
    id: String,
    content: T,
    options: UpsertOptions
  ): Future[MutationResult] = defaultCol.upsert(id, content, options)

  /** Replaces the contents of a full document in this collection, if it already exists.
    * @param id         $Id
    * @param content    $SupportedTypes
    * @param cas        $CAS
    * @param durability $Durability
    * @param timeout    $Timeout
    * @param transcoder $Transcoder
    * @param expiry should be used for any expiration times 30 days.
    *               If over that, use `expiryTime: Instant` instead.
    * @param expiryTime should be used for any expiration times >= 30 days.
    *                   If below that, use `expiry: Duration` instead.
    * @param preserveExpiry Whether an existing document's expiry should be preserved.
    *                       Requires Couchbase Server 7.0 or later.
    * @return Future success with a `MutationResult`, $OrNotFound
    *
    *         $ErrorHandling
    * @note $ExpiryNote
    * @note $ExpiryTimeNote
    * @see [[insert]], [[upsert]]
    */
  def replace[T: JsonSerializer](
    id: String,
    content: T,
    cas: Long = 0,
    durability: Durability = Disabled,
    timeout: Duration = MinusInf,
    transcoder: Transcoder = null,
    expiry: Duration = null,
    expiryTime: Instant = null,
    preserveExpiry: Boolean = false,
  ): Future[MutationResult] = defaultCol.replace(
    id,
    content,
    ReplaceOptions(
      cas,
      durability,
      timeout,
      transcoder = Option(transcoder),
      expiry = expiry,
      expiryTime = Option(expiryTime),
      preserveExpiry = preserveExpiry
    )
  )

  /** See doc of the other overload method */
  def replace[T: JsonSerializer](
    id: String,
    content: T,
    options: ReplaceOptions
  ): Future[MutationResult] = defaultCol.replace(id, content, options)

  /** Removes a document from this collection, if it exists.
    * @param id            $Id
    * @param cas           $CAS
    * @param durability    $Durability
    * @param timeout       $Timeout
    * @return Future success with a `MutationResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def remove(
    id: String,
    cas: Long = 0,
    durability: Durability = Disabled,
    timeout: Duration = MinusInf
  ): Future[MutationResult] = defaultCol.remove(
    id,
    RemoveOptions(
      cas,
      durability,
      timeout
    )
  )

  /** See doc of the other overload method */
  def remove(
    id: String,
    options: RemoveOptions
  ): Future[MutationResult] = defaultCol.remove(id, options)

  /** Performs a view query against the cluster.
    * @param designDoc the view design document to use
    * @param viewName  the view to use
    * @param options   any view query options - see [[com.couchbase.client.scala.view.ViewOptions]] for documentation
    */
  def viewQuery(
    designDoc: String,
    viewName: String,
    options: ViewOptions = ViewOptions()
  ): Future[ViewResult] = underlying.viewQuery(designDoc, viewName, options)

  /** See doc of the other overload method */
  def query(statement: String, options: QueryOptions): Future[QueryResult] =
    cluster.query(statement, options)

  /** Performs a N1QL query against the cluster.
    * @param statement the N1QL statement to execute
    * @param parameters provides named or positional parameters for queries parameterised that way.
    * @param timeout sets a maximum timeout for processing.
    * @param adhoc if true (the default), adhoc mode is enabled: queries are just run.  If false, adhoc mode is disabled
    *              and transparent prepared statement mode is enabled: queries are first prepared so they can be executed
    *              more efficiently in the future.
    */
  def query(
    statement: String,
    parameters: QueryParameters = QueryParameters.None,
    timeout: Duration =
      cluster.env.core.timeoutConfig.queryTimeout().toNanos.nanos,
    adhoc: Boolean = true
  ): Future[QueryResult] =
    cluster.query(statement, parameters, timeout, adhoc)

  /** Unlock a locked document.
    * @param id             $Id
    * @param cas            must match the CAS value return from a previous `.getAndLock()` to successfully
    *                       unlock the document
    * @param timeout        $Timeout
    * @return Future success with a `Unit`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def unlock(
    id: String,
    cas: Long,
    timeout: Duration = MinusInf
  ): Future[Unit] = defaultCol.unlock(id, cas, UnlockOptions(timeout))

  /** See doc of the other overload method */
  def unlock(
    id: String,
    cas: Long,
    options: UnlockOptions
  ): Future[Unit] = defaultCol.unlock(id, cas, options)

  /** Updates the expiry of the document with the given id.
    * @param id             $Id
    * @param timeout        $Timeout
    *
    * @return Future success with a `MutationResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def touch(
    id: String,
    expiry: Duration,
    timeout: Duration = MinusInf
  ): Future[MutationResult] = defaultCol.touch(
    id,
    expiry,
    TouchOptions(
      timeout
    )
  )

  /** See doc of the other overload method */
  def touch(
    id: String,
    expiry: Duration,
    options: TouchOptions
  ): Future[MutationResult] = defaultCol.touch(id, expiry, options)

  /** Increment a Couchbase 'counter' document. $CounterDoc
    *
    * $OnlyCounter
    *
    * @param id            $Id
    * @param delta         the amount to increment by
    * @param initial       if not-None, the amount to initialise the document too, if it does not exist. If this is
    *                      not set, and the document does not exist, the result Future will failed with DocumentNotFoundException
    * @param durability    $Durability
    * @param timeout       $Timeout
    * @return Future success with a `CounterResult`, $OrNotFound
    *
    *         $ErrorHandling
    *
    * @note $ExpiryNote
    * @note $ExpiryTimeNote
    */
  def counter(
    id: String,
    delta: Long = 0L,
    initial: Option[Long] = None,
    durability: Durability = Disabled,
    timeout: Duration = MinusInf, // MinusInf will be converted to kvReadTimeout
    expiry: Duration = null,
    expiryTime: Instant = null,
  ): Future[CounterResult] = defaultCol.binary.increment(
    id,
    delta,
    IncrementOptions(
      initial,
      durability,
      timeout,
      expiry = expiry,
      expiryTime = Option(expiryTime)
    )
  )

  /** convenient method. {{{ = counter(id).map(_.content).recoverNotExist(default) }}} */
  def getCounter(id: String, default: Long = 0L)(
    implicit ec: ExecutionContext
  ): Future[Long] =
    counter(id).map(_.content).recoverNotExist(default)

  /** See doc of the other overload method */
  def counter(
    id: String,
    delta: Long,
    options: IncrementOptions
  ): Future[CounterResult] = defaultCol.binary.increment(id, delta, options)

  /** See doc of the other overload method */
  def counter(
    id: String,
    delta: Long,
    initial: Long
  ): Future[CounterResult] =
    defaultCol.binary.increment(id, delta, IncrementOptions(Some(initial)))

  /** See doc of the other overload method */
  def counter(
    id: String,
    delta: Long,
    initial: Long,
    expiry: Duration
  ): Future[CounterResult] =
    defaultCol.binary.increment(
      id,
      delta,
      IncrementOptions(Some(initial), expiry = expiry)
    )

  /** Add bytes to the end of a Couchbase binary document.
    *
    * $OnlyBinary
    *
    * @param id            $Id
    * @param content       the bytes to append
    * @param cas           $CAS
    * @param durability    $Durability
    * @param timeout       $Timeout
    *
    * @return Future success with a `MutationResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def append(
    id: String,
    content: Array[Byte],
    cas: Long = 0,
    durability: Durability = Disabled,
    timeout: Duration = MinusInf
  ): Future[MutationResult] = defaultCol.binary.append(
    id,
    content,
    AppendOptions(
      cas,
      durability,
      timeout
    )
  )

  /** See doc of the other overload method */
  def append(
    id: String,
    content: Array[Byte],
    options: AppendOptions
  ): Future[MutationResult] = defaultCol.binary.append(id, content, options)

  /** Add bytes to the beginning of a Couchbase binary document.
    *
    * $OnlyBinary
    *
    * @param id            $Id
    * @param content       the bytes to prepend
    * @param cas           $CAS
    * @param durability    $Durability
    * @param timeout       $Timeout
    *
    * @return Future success with a `MutationResult`, $OrNotFound
    *
    *         $ErrorHandling
    */
  def prepend(
    id: String,
    content: Array[Byte],
    cas: Long = 0,
    durability: Durability = Disabled,
    timeout: Duration = MinusInf
  ): Future[MutationResult] = defaultCol.binary.prepend(
    id,
    content,
    PrependOptions(
      cas,
      durability,
      timeout
    )
  )

  /** See doc of the other overload method */
  def prepend(
    id: String,
    content: Array[Byte],
    options: PrependOptions
  ): Future[MutationResult] = defaultCol.binary.prepend(id, content, options)

  def viewIndexes: AsyncViewIndexManager = underlying.viewIndexes

  /** The AsyncBucketManager provides access to creating and getting buckets. */
  def bucketManager: AsyncBucketManager = cluster.buckets
}
