package com.sandinh.couchbase

import javax.inject.Inject
import com.google.inject.{Guice, AbstractModule}
import com.sandinh.couchbase.transcoder.JsTranscoder
import com.typesafe.config.{ConfigFactory, Config}
import org.specs2.mutable.Specification
import org.specs2.specification.{Step, Fragments}

trait GuiceSpecBase extends Specification {
  //  private lazy val cbCluster = Guice.createInjector(new CBModule).getInstance(classOf[CBCluster])
  @Inject private[this] var cbCluster: CBCluster = null

  lazy val cb = cbCluster.openBucket("bk1", JsTranscoder)
  def setup = Guice.createInjector(new CBModule).injectMembers(this)
  def teardown = cbCluster.cluster.disconnect()

  override def map(fs: => Fragments) = Step(setup) ^ fs ^ Step(teardown)
}

class CBModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Config]).toInstance(ConfigFactory.load())
  }
}
