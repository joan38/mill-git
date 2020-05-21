package com.goyeau.mill.git

import mill._
import mill.scalalib.PublishModule

trait GitVersionedPublishModule extends PublishModule {
  def publishVersion: T[String] = GitVersionModule.version()()
}
