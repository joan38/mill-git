package com.goyeau.mill.git

import mill._
import os.{CommandResult, Pipe}
import scala.util.Try

trait GitVersionedModule extends Module {

  /**
    * Length of the git hash.
    */
  def hashLength: T[Int] = 7

  /**
    * Version derived from git.
    */
  def gitVersion: T[String] = T.input {
    os.proc("git", "describe", "--tags", "--match=v[0-9]*", "--always", s"--abbrev=${hashLength()}", "--dirty")
      .call(check = false, stderr = Pipe) match {
      case result @ CommandResult(0, _) =>
        val taggedRegex   = """v(\d.*?)(?:-(\d+)-g([\da-f]+))?(-dirty)?""".r
        val untaggedRegex = """([\da-f]+)(-dirty)?""".r

        result.out.trim() match {
          case taggedRegex(tag, distance, hash, dirty) =>
            val isDirty = Option(dirty).isDefined
            val distanceHash = Option(distance).fold {
              if (isDirty) uncommittedHash()
              else ""
            } { distance =>
              if (isDirty) s"${distance.toInt + 1}-${uncommittedHash()}"
              else s"$distance-$hash"
            }
            s"$tag-$distanceHash"
          case untaggedRegex(hash, dirty) =>
            Option(dirty).fold(hash)(_ => uncommittedHash())
        }
      case _ => uncommittedHash()
    }
  }

  private def uncommittedHash = T.input {
    val indexCopy = T.ctx.dest / "index"
    Try(os.copy(os.pwd / ".git" / "index", indexCopy, replaceExisting = true, createFolders = true))
    val indexFileEnv = Map("GIT_INDEX_FILE" -> indexCopy.toString)
    os.proc("git", "add", "--all").call(env = indexFileEnv)
    os.proc("git", "write-tree").call(env = indexFileEnv).out.trim().take(1 + hashLength())
  }
}
