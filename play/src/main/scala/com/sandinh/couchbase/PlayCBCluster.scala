package com.sandinh.couchbase

import java.lang
import javax.inject.{Inject, Singleton}

import play.api.inject.ApplicationLifecycle

import scala.concurrent.duration._
import com.sandinh.rx.Implicits._
import com.typesafe.config.Config

import scala.concurrent.Future

@Singleton
class PlayCBCluster @Inject() (cfg: Config, lifecycle: ApplicationLifecycle)
    extends CBCluster(cfg) {

  /** convention val for using with play.api.inject.ApplicationLifecycle#addStopHook */
  val disconnectFuture: () => Future[lang.Boolean] = () =>
    asJava
      .disconnect()
      .timeout(env.disconnectTimeout, MILLISECONDS)
      .toFuture

  lifecycle.addStopHook(disconnectFuture)
}
