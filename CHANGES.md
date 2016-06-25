## Changelog
we use [Semantic Versioning](http://semver.org/)

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
