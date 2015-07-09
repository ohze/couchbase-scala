package com.sandinh.couchbase

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import scala.concurrent.duration._
import com.sandinh.rx.Implicits._

@Singleton
class PlayCBCluster @Inject() (cfg: Configuration, lifecycle: ApplicationLifecycle) extends CBCluster(cfg.underlying) {
  /** convention val for using with play.api.inject.ApplicationLifecycle#addStopHook */
  val disconnectFuture = () =>
    cluster.disconnect()
      .timeout(env.disconnectTimeout, MILLISECONDS)
      .toFuture
      .map(_ => ())

  lifecycle.addStopHook(disconnectFuture)
}
