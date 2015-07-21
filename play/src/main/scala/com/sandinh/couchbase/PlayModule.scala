package com.sandinh.couchbase

import com.typesafe.config.Config
import play.api.{Configuration, Environment}
import play.api.inject.Module

class PlayModule extends Module {
  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[Config].toInstance(configuration.underlying),
    bind[CBCluster].to[PlayCBCluster]
  )
}
