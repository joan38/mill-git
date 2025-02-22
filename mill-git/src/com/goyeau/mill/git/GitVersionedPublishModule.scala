package com.goyeau.mill.git

import mill.*
import mill.scalalib.PublishModule

trait GitVersionedPublishModule extends PublishModule {
  def publishVersion: T[String] = GitVersionModule.version()()
}
