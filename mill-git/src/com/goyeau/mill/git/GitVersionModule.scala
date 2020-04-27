package com.goyeau.mill.git

import mill._
import mill.define.{Discover, ExternalModule}
import os._
import scala.util.Try

object GitVersionModule extends ExternalModule {
  private val hashLength = 7

  /**
    * Version derived from git.
    */
  def version: T[String] = T.input {
    val isDirty = proc("git", "status", "--porcelain").call().out.trim().nonEmpty
    proc("git", "describe", "--tags", "--match=v[0-9]*", "--always", s"--abbrev=$hashLength")
      .call(check = false, stderr = Pipe) match {
      case result @ CommandResult(0, _) =>
        val taggedRegex   = """v(\d.*?)(?:-(\d+)-g([\da-f]+))?""".r
        val untaggedRegex = """([\da-f]+)""".r

        result.out.trim() match {
          case taggedRegex(tag, distance, hash) =>
            val distanceHash = Option(distance).fold {
              if (isDirty) s"-1-${uncommittedHash()}"
              else ""
            } { distance =>
              if (isDirty) s"${distance.toInt + 1}-${uncommittedHash()}"
              else s"-$distance-$hash"
            }
            s"$tag$distanceHash"
          case untaggedRegex(hash) =>
            if (isDirty)uncommittedHash()
            else hash
        }
      case _ => uncommittedHash()
    }
  }

  private def uncommittedHash = T.input {
    val indexCopy = T.ctx.dest / "index"
    Try(copy(pwd / ".git" / "index", indexCopy, replaceExisting = true, createFolders = true))
    val indexFileEnv = Map("GIT_INDEX_FILE" -> indexCopy.toString)
    proc("git", "add", "--all").call(env = indexFileEnv)
    proc("git", "write-tree").call(env = indexFileEnv).out.trim().take(hashLength)
  }

  override lazy val millDiscover: Discover[this.type] = Discover[this.type]
}
