package com.sandinh.couchbase

import play.api.{Configuration, Environment}
import play.api.inject.Module

class PlayModule extends Module {
  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[CBCluster].to[PlayCBCluster]
  )
}
