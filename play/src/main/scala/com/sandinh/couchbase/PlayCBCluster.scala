package com.sandinh.couchbase

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import com.typesafe.config.Config

@Singleton
class PlayCBCluster @Inject() (cfg: Config, lifecycle: ApplicationLifecycle)
    extends CBCluster(cfg) {
  lifecycle.addStopHook(disconnect _)
}
