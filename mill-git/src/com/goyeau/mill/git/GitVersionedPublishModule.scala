package com.goyeau.mill.git

import mill._
import mill.scalalib.PublishModule

trait GitVersionedPublishModule extends GitVersionedModule with PublishModule {
  def publishVersion: T[String] = gitVersion
}
