## Changelog
We use [Semantic Versioning](http://semver.org/)

Backward binary compatibility is ensured by [mima](https://github.com/lightbend/mima)

See also [mima-filters](core/src/main/mima-filters)

#### v10.0.0
+ Use com.couchbase.client:scala-client:1.2.3 instead of com.couchbase.client:java-client 2.7.22  
  See [Migrating from SDK2 to SDK3 API](https://docs.couchbase.com/java-sdk/current/project-docs/migrating-sdk-code-to-3.n.html)
  And [Couchbase Scala SDK](https://docs.couchbase.com/scala-sdk/current/hello-world/start-using-sdk.html)
+ Note: In sdk3, couchbase [use Reactor instead of RxJava](https://docs.couchbase.com/java-sdk/current/project-docs/migrating-sdk-code-to-3.n.html#reactive-and-async-apis).
  So couchbase-scala 10.x also depends on Reactor instead of RxJava.
+ Don't support couchbase 4.x
+ [Don't support bucket-level passwords](https://docs.couchbase.com/java-sdk/current/project-docs/migrating-sdk-code-to-3.n.html#authentication)
  You need set config `com.sandinh.couchbase.{user, password}` and give the user corresponding roles
+ `DocumentDoesNotExistException` -> `DocumentNotFoundException`
+ Deprecated `ScalaBucket`. Now `ScalaBucket` is aliased to `CBBucket`
+ In sdk 3, `Document` class is removed and the returned value is now `Result`.
  - `CBBucket.get[D <: Document[_]](id: String)(implicit tag: ClassTag[D]): Future[D]` is changed to  
  `get(id: String, options: GetOptions = GetOptions()): Future[GetResult]`
  - Similar to other methods such as `insert, append, prepend,..`
  - `getT[T](id: String)(implicit c: Class[_ <: Document[T]]): Future[T]` implicit params change:
    Instead of `Class[_ <: Document[T]]`, we now need `JsonDeserializer[T], WeakTypeTag[T], ClassTag[T]`.
    Note: Don't need the implicit `WeakTypeTag[T]` if [this change](https://review.couchbase.org/c/couchbase-jvm-clients/+/166690) is merged.
+ Deprecated `asJava` of `CBCluster`, `CBBucket`. Pls use `underlying`
+ Use `implicit ec: ExecutionContext` param instead of `ExecutionContext.Implicits.global` in:
  - `CBBucket.{getJsT, getT}`
+ Remove the deprecated `CBCluster.getOrElseT`
+ Remove `StrCao, StrCao1, StrCao2, WithCaoKey1, WithCaoKey2, rx.Implicits, document.*, transcoder.*`

##### v9.2.0
+ Compatible with 9.0.0 except [9.0.0.backward.excludes](core/src/main/mima-filters/9.0.0.backward.excludes)
+ Update com.couchbase.client:java-client:2.7.20 -> 2.7.22
+ Update scala-collection-compat:2.5.0 -> 2.6.0
+ (scala3) Update scala 3.0.2 -> 3.1.0

##### v9.0.0
+ Break compatible with v8.x but binary compatible with v7.x except [7.4.5.backward.excludes](core/src/main/mima-filters/7.4.5.backward.excludes)  
  Some source change may need if you use JsCao1 or StrCao1 or WithCaoKey1  
  But if compile success then binary compatibility are ensured.
+ Add support for scala 2.13 & scala3
  - `couchbase-scala` is built against scala 2.11.12, 2.12.15, 2.13.6, 3.0.2
+ Add support for playframework 2.8
  - `couchbase-play` for play 2.6 is renamed to `couchbase-play_2_6` and only support scala 2.11 & 2.12
    for sbt: `libraryDependencies += "com.sandinh" %% "couchbase-play_2_6" % "9.0.0"`
  - For play 2.8:
    `libraryDependencies += "com.sandinh" %% "couchbase-play" % "9.0.0"`
    Only support scala 2.12 & 2.13
+ Incompatible dependencies change of couchbase-play_2_6 against couchbase-play:7.4.5:
  - guava: 22.0 -> 23.6.1-jre
  - ssl-config-core 0.2.2 -> 0.3.8
  - scala-parser-combinators 1.0.6 -> 1.1.2

##### v7.4.5
TODO @thanhpv

##### v7.4.4
+ Support [Concurrent Document Mutations](https://docs.couchbase.com/java-sdk/2.7/concurrent-mutations-cluster.html)
+ Use CAS value


##### 8.1.0
+ break change! rename CaoBase.setTWithId -> setWithIdT
+ add WithCaoKey1.getOrUpdate convenient method
+ change back to normal `version := `, don't use sbt-git versioning

##### 8.0.0-1-g0a620a6
+ update play 2.8.0

##### 8.0.0
+ break changes:
  - rename CaoBase get/set/update/remove methods by adding `WithId` suffix
+ drop scala 2.11 & add 2.13
+ update play 2.8.0-RC5
+ update couchbase java-client 2.7.11
+ update typesafe config 1.4.0 //same as dependency of akka-actor:2.6.0
+ use scala-collection-compat
+ update sbt 1.3.4 & some sbt plugins
+ use sbt-git for versioning

##### v7.4.2
+ update couchbase java-client 2.6.0, play-json 2.6.9 (latest, corresponding with play 2.6.17)
+ cross compile for scala 2.12.6, 2.11.12
+ update sbt 1.1.6 & some sbt plugins

##### v7.4.1
+ update couchbase java-client 2.5.2, play-json 2.6.7
+ cross compile for scala 2.12.4, 2.11.11
+ add travis test on couchbase 5.0.0
+ update sbt 1.0.3 & some sbt plugins

##### v7.4.0
+ update couchbase java-client 2.5.0, play-json 2.6.3
+ couchbase-play now depends on play instead of play-alone
+ cross compile for scala 2.12.3, 2.11.11
+ travis test on couchbase 4.6.3, 4.5.0
+ update sbt 1.0.1, sbt-sonatype 2.0, sbt-pgp 1.1.0
+ use sbt-coursier
+ use sbt-scalafmt-coursier instead of sbt-scalariform
+ move source code to github.com/ohze/couchbase-scala
+ Change in PlayCBCluster:
  - now inject (first constructor's param) Config instead of Configuration
  - `disconnectFuture` now return `Future[lang.Boolean]` instead of `Future[Unit]`
+ breaking changes in `CBCluster`:
  - remove deprecated field `cluster`
  - `openBucket` now return `Future[ScalaBucket]` instead of `ScalaBucket`.
  - Add `openBucketSync` - which is the old synchronous `openBucket` method.
    @note You should never perform long-running blocking operations inside of an asynchronous stream (e.g. inside of maps or flatMaps).
    @see [JVMCBC-79](https://issues.couchbase.com/browse/JVMCBC-79)
  - Similar for `disconnect` (now return `Future[lang.Boolean]`) & `disconnectSync`

##### v7.3.1
+ update couchbase java-client 2.3.1, play-json 2.5.4
+ remove config `com.sandinh.couchbase.queryEnabled`
 because `com.couchbase.client.core.env.DefaultCoreEnvironment.Builder#queryEnabled` is removed fromcouchbase java-client 2.3.x

##### v7.3.0
+ update scala 2.11.8, couchbase client 2.2.7, play-alone 2.5.3
+ test on travis for <oraclejdk8, openjdk8> * <couchbase 4.1.1, couchbase 4.5.0-beta>

##### v7.2.2
+ update couchbase java-client 2.2.4 & simplify scalacOptions
+ minor change: use TranscoderUtils.encodeStringAsUtf8 instead of Unpooled.copiedBuffer
+ fix CompatStringSpec. The failed tests is caused by caching mechanism of couchbase bucket

##### v7.2.1
+ update java-client 2.2.2, rxjava 1.0.17, play-json 2.4.6, play-alone 2.4.3

##### v7.2.0
+ update couchbase java-client 2.2.1, rxjava 1.0.15, play-json 2.4.3, play-alone 2.4.2_1
+ note: This version is compatible with couchbase-server 2.x, 3.x, 4.x.
see [JCBC-880](https://issues.couchbase.com/browse/JCBC-880)

##### v7.1.3
+ add binding: `bind[Config].toInstance(configuration.underlying)` in `com.sandinh.couchbase.PlayModule`

##### v7.1.2
+ `couchbase-play` can be used with [com.sandinh:play-alone](https://github.com/giabao/play-jdbc-standalone)
    or full [com.typesafe.play:play](http://playframework.com/)
+ update `com.couchbase.client:java-client:2.1.4`

##### v7.1.1
change in CBCluster:
+ deprecate cluster. Use asJava instead
+ Make `env` public

##### v7.1.0
+ update play-json 2.4.2 (require java 8), com.couchbase.client:java-client:2.1.3, rxjava:1.0.12
+ add `couchbase-play` module for using couchbase-scala in a play application
+ support n1ql querry
+ add convenience method `JsDocument.as[T: Reads]: T`
+ (minor break change) move the implicit Doc Classes: com.sandinh.couchbase.{Implicits => Implicit.DocCls}
+ add implicit value class DocNotExistFuture, RichJsonObject, RichJsonArray

##### v7.0.1
+ update scala 2.11.6, com.couchbase.client:java-client:2.1.1, rxjava:1.0.7
+ use specs2-core:3.0 

##### v7.0.0
+ update play-json 2.3.8, rxjava 1.0.6 (transitive dep at v1.0.4 from couchbase java-client 2.1.0)
+ update couchbase java-client 2.1.0 with changes:
    + default disconnect timeout is increased from 5s to 25s
    + ScalaBucket.query(String) is replaced by query(Statement)
+ RichAsyncViewResult.flatFoldRows now use scConcatMap instead of scFlatMap to preserve order of underlying observable items.
This fixes the bug in sandinh.com's bank-play project: log rows in bank is out-of-created-order
+ RichAsyncViewResult is moved from com.sandinh.rx.Implicits to com.sandinh.couchbase.Implicits

##### v6.1.0
+ fixes SI-9121 by removing com.sandinh.rx.Implicits.{RichFunction1, RichFunction2}
+ remove some `@inline` annotations
+ add scalacOptions: -optimise -Ybackend:GenBCode -Ydelambdafy:method ..

##### v6.0.0
1. add [CompatStringTranscoderLegacy](src/main/scala/com/sandinh/couchbase/transcoder/CompatStringTranscoder.scala#L51) which:
 + decoding a stored document in format of StringDocument OR JsonStringDocument.
 + encoding a String as StringDocument.

  (same as in previous version, [CompatStringTranscoder](src/main/scala/com/sandinh/couchbase/transcoder/CompatStringTranscoder.scala#L39):
 + decoding a stored document in format of StringDocument OR JsonStringDocument.
 + encoding a String as StringDocument.)

  see [CompatStringSpec](src/test/scala/com/sandinh/couchbase/CompatStringSpec.scala)

2. [CBCluster.openBucket](src/main/scala/com/sandinh/couchbase/CBCluster.scala#L24) now has `legacyEncodeString: Boolean` param, default = true.
 In previous version, CBCluster.openBucket("some_bucket") return a bucket which encode String as JsonString (using CompatStringTranscoder).
  For better compatibility, from v6.0.0 the return bucket will encode String using CompatStringTranscoderLegacy.
  (This is in-compatible with v5.x, so we bump to v6.0.0).

##### v5.1.1
only update scala 2.11.5, couchbase java-client 2.0.3

##### v5.1.0
only use CompatStringDocument instead of StringDocument for StrCao, StrCao1, StrCao2

##### v5.0.0
+ move `def bucket: ScalaBucket` to constructor's param in CaoBase, JsCao, JsCao1, JsCao2, StrCao, StrCao1, StrCao2
+ use `com.couchbase.timeout._` keys to config timeout in [duration format](https://github.com/typesafehub/config/blob/master/HOCON.md#duration-format).
see [reference.conf](src/main/resources/reference.conf) for legend
+ note: from this version, config values `com.couchbase._` will not be set to java system properties
(see class `DefaultCouchbaseEnvironment`)
+ add some convenient methods to ScalaBucket: getT, getOrElseT, getJsT
+ CBCluster now auto add `com.sandinh.couchbase.transcoder._` transcoders when openBucket
+ add CompatStringDocument which works exactly like JsonStringDocument permit decoding a stored StringDocument

##### v4.2.0
reverse `getOrElse` changes in v4.1.0:
`getOrElse(x)(null): type mismatch found Null(null) required XX`
is because x is an instance of class X(..) extend AnyVal

##### v4.1.0
note: Do not use this version. see v4.2.0
This version contain some incompatible change:
+ getOrElse method in CaoBase, WithCaoKey1, WithCaoKey2: do not use a separate param list for `default` param
(fix usage problem when getOrElse(x)(null): type mismatch found Null(null) required XX)
+ rename RichAsyncViewRow.{document => doc}.
 We can't name `document` because scala compiler will not implicitly pick that method.

##### v4.0.1
+ some minor change (no bug fix, no new feature)
+ remove crossScalaVersions 2.10

##### v4.0.0
WARNING: when implement a real project at http://sandinh.com, we found that RxScala (& RxJava) is VERY VERY complex
(compare to scala Future).
At first, we have created https://github.com/giabao/scala-future-vs-rxscala to share knowledge to our team.
But after several weeks, we have decided to use Future only! (many dangerousness of Rx have not been mentioned in scala-future-vs-rxscala).
So, we change couchbase-scala to just expose Future as the API.

##### v3.0.2
+ update rxjava 1.0.3
+ add com.sandinh.rx.Implicits.RichObs.subscribeError

##### v3.0.1
only update libs:
```
  "com.couchbase.client"  %  "java-client" % "2.0.2",
  "io.reactivex"          %% "rxscala"     % "0.23.0",
  "io.reactivex"          % "rxjava"       % "1.0.2",
  "com.typesafe.play"     %% "play-json"   % "2.3.7"
```

##### v3.0.0
+ update rxjava 1.0.1
+ typeof CBCluster.openBucket & CaoBase.bucket is changed from ScalaBucket to Observable[ScalaBucket]
+ typeof CBCluster.disconnect() is changed from Boolean to Observable[Boolean]
+ remove CBCluster.disconnect(FiniteDuration)
+ setBulk in WithCaoKey1 & WithCaoKey2 now use concatMap instead of flatMap to preserve ordering of result with the params
+ fixes http://www.couchbase.com/issues/browse/JCBC-642

##### v2.0.1
narrow dependencies from guice to javax.inject

##### v2.0.0
first public version
