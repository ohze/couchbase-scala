couchbase-scala
===============
[![Build Status](https://github.com/ohze/couchbase-scala/actions/workflows/test.yml/badge.svg)](https://github.com/ohze/couchbase-scala/actions/workflows/test.yml)

This is a library for accessing Couchbase in Scala.

## Using
couchbase-scala is [published to maven center](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.sandinh%22%20couchbase-scala)

1. using [typesafe config](https://github.com/typesafehub/config) file application.conf
to config couchbase connection url, buckets, timeout,.. 
```
# see class com.couchbase.client.java.env.DefaultCouchbaseEnvironment
com.couchbase.timeout {
  connect=10s
}
com.sandinh.couchbase {
  connectionString = "couchbase://dev.sandinh.com"
  buckets {
    # syntax: bucketName.password=".."
    # we will use CBCluster.openBucket(bucketName) to retrieve a ScalaBucket object
    acc.password=""
    # we can also use the following verbose syntax to customize the real bucket name when connect to couchbase server
    bk1 {
      name = fodi
      password=""
    }
  }
}
```

2. load the config, instantiate a CBCluster instance, then open a bucket
```scala
val config = ConfigFactory.load()
val cluster = new CBCluster(config);
val accBucket = cluster.openBucket("acc");
```

Or, you can use DI (example google guice):
```scala
class CBModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Config]).toInstance(ConfigFactory.load())
  }
}

class MyClient @Inject() (cluster: CBCluster) {
  val accBucket = cluster.openBucket("acc");
}
```

3. access couchbase using ScalaBucket's api
```scala
val s = accBucket.get[StringDocument]("some_key").map(_.content)
val s = accBucket.getT[String]("some_key")
val s = accBucket.getOrElseT("some_key")("default value")
//see other methods (insert, replace, remove, touch, counter, append, unlock, getFromReplica, getAndLock,..)
//from ScalaBucket class
```

4. you can use play-json to retrieve a JsValue directly
```scala
case class Acc(name: String, gender: Option[Boolean])
object Acc { implicit val fmt = Json.format[Acc] }
val name = accBucket.getJsT[Acc]("some_key").map(_.name)
```

## Changelog
see [CHANGES.md](CHANGES.md)

## Dev guide

+ prepare couchbase for testing
```shell script
docker run -d --name cb -p 8091-8094:8091-8094 -p 11210:11210 couchbase:5.0.1
docker cp cb-test-prepare.sh cb:/tmp
docker exec -i cb /tmp/cb-test-prepare.sh
```
or, if you have prepared before => only run `docker start cb`

```sbtshell
test
```

## publish checklist
+ should add unit test
+ should change [[build.sbt]] / version after publishing
+ MUST change [[build.sbt]] / version when your commit introduce a new break change (increase the minor number)
+ MUST tag the publishing git commit
+ MUST push to github (push tag too)
+ MUST run `sbt clean +test`
(`+test` to test against all crossScalaVersions in [[build.sbt]])
+ if you publish from sbtshell in IDEA or from an already running sbt shell then
MUST run the following tasks:
```sbtshell
reload
clean
+test
```
+ MUST update [CHANGES.md]!

+ after that, [publish by](https://github.com/xerial/sbt-sonatype#publishing-your-artifact):
```sbtshell
+publishSigned
sonatypeBundleRelease
```

## Licence
This software is licensed under the Apache 2 license:
http://www.apache.org/licenses/LICENSE-2.0

Copyright 2014-2019 Sân Đình (https://sandinh.com)
