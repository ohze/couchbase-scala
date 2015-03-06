package com.sandinh.couchbase

import javax.inject.Inject
import com.google.inject.{Guice, AbstractModule}
import org.specs2.specification.core.Fragments
import scala.concurrent.duration._
import com.typesafe.config.{ConfigFactory, Config}
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait GuiceSpecBase extends Specification {
  implicit class CustomFutureMatchable[T](m: Matcher[T]) {
    def await: Matcher[Future[T]] = new FutureMatchable(m).await(0, 5.seconds)
  }
  @Inject private[this] var _cb: CB = null
  protected def cb = _cb

  def setup() = Guice.createInjector(new CBModule).injectMembers(this)
  def teardown() = cb.cluster.disconnect()

  override def map(fs: => Fragments) = step(setup()) ^ fs ^ step(teardown())
}

class CBModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Config]).toInstance(ConfigFactory.load())
  }
}
