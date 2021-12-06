couchbase-scala
===============

[![CI](https://github.com/ohze/couchbase-scala/actions/workflows/sbt-devops.yml/badge.svg)](https://github.com/ohze/couchbase-scala/actions/workflows/sbt-devops.yml)

This is a library for accessing Couchbase in Scala.

## Using
couchbase-scala is [published to maven center](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.sandinh%22%20couchbase-scala)

1. using [typesafe config](https://github.com/typesafehub/config) file application.conf
to config couchbase connection url, buckets, timeout,..  
see [reference.conf](core/src/main/resources/reference.conf)
```
com.couchbase.timeout {
  connect=10s
  # ...
}
com.sandinh.couchbase {
  connectionString = "dev.sandinh.com"
  user="?"
  password="?"
}
```

2. load the config, instantiate a CBCluster instance, then open a bucket
```scala
import com.typesafe.config.ConfigFactory
import com.sandinh.couchbase.CBCluster
val cluster = new CBCluster(ConfigFactory.load())
val accBucket = cluster.bucket("acc")
```

Or, you can use DI (example google guice):
```scala
class CBModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Config]).toInstance(ConfigFactory.load())
  }
}

class MyClient @Inject() (cluster: CBCluster) {
  val accBucket = cluster.bucket("acc")
}
```

3. access couchbase using CBBucket's api
```scala
val r: Future[GetResult] = accBucket.get("some_key")
val s = accBucket.getT[String]("some_key")

//see other methods (upsert, insert, replace, remove, touch, counter, append, unlock, getFromReplica, getAndLock,..)
//from CBBucket class
```

4. you can use play-json to retrieve a JsValue directly
```scala
import play.api.libs.json.{Json, Format}
import com.couchbase.client.scala.codec.JsonSerializer, JsonSerializer.PlayEncode

case class Acc(name: String, gender: Option[Boolean])
object Acc {
  implicit val fmt: OFormat[Acc] = Json.format[Acc]
  // Used in upsert
  implicit val ser: JsonSerializer[Trophy] = t => PlayEncode.serialize(Json.toJson(t))
}
accBucket.upsert("some_key", Acc("name", None))
val name = accBucket.getJsT[Acc]("some_key").map(_.name)
```

## Changelog
see [CHANGES.md](CHANGES.md)

## Dev guide

+ prepare couchbase for testing
```shell script
docker run -d --name cb -p 8091-8094:8091-8094 -p 11210:11210 couchbase:5.0.1
docker cp travis-cb-prepare.sh cb:/tmp
docker exec -i cb /tmp/travis-cb-prepare.sh
```
or, if you have prepared before => only run `docker start cb`

```sbtshell
test
```

## publish guide
We use [sd-devops](/ohze/sd-devops) so:
+ Every push (or merge a PR) to `master` branch will be publish to sonatype snapshots
  (only if [QA, test, compatible check](.github/workflows/sd-devops.yml) pass)
+ If push tag match glob `v[0-9]*`, ex `v9.0.0` or even `v9bla.bla`
  then [publish job](.github/workflows/sd-devops.yml) will publish a release version to sonatype release repo
  (which will be sync to maven central)
+ **!!!NOTE!!!** You MUST tag version with **v** prefix or else it will not be published!
+ You should never manually publish from your local machine unless `sbt publishLocal`
+ MUST update [CHANGES.md]!

## Licence
This software is licensed under the Apache 2 license:
http://www.apache.org/licenses/LICENSE-2.0

Copyright 2014-2021 Sân Đình (https://sandinh.com)
