package com.goyeau.mill.git

import mill.*
import mill.contrib.docker.DockerModule
import mill.scalalib.JavaModule

trait GitTaggedDockerModule extends DockerModule:
  outer: JavaModule =>
  trait GitTaggedDocker extends DockerConfig:
    def tagLatest: T[Boolean]         = false
    override def tags: T[Seq[String]] =
      super.tags().map(tag => s"$tag:${GitVersionModule.version()()}") ++
        (if tagLatest() then super.tags().map(tag => s"$tag:latest") else Seq.empty)
