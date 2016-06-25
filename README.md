couchbase-scala
===============
[![Build Status](https://travis-ci.org/giabao/couchbase-scala.svg)](https://travis-ci.org/giabao/couchbase-scala)

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

## Licence
This software is licensed under the Apache 2 license:
http://www.apache.org/licenses/LICENSE-2.0

Copyright 2014 Sân Đình (http://sandinh.com)
