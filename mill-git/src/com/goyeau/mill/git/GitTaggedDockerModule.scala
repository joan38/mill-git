package com.goyeau.mill.git

import mill._
import mill.contrib.docker.DockerModule
import mill.scalalib.JavaModule
import scala.collection.immutable.Seq

trait GitTaggedDockerModule extends GitVersionedModule with DockerModule { outer: JavaModule =>
  trait GitTaggedDocker extends DockerConfig {
    def tagLatest: T[Boolean] = false
    override def tags: T[Seq[String]] =
      super.tags().map(tag => s"$tag:${gitVersion()}") ++
        (if (tagLatest()) super.tags().map(tag => s"$tag:latest") else Seq.empty)
  }
}
