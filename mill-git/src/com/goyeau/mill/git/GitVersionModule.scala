package com.goyeau.mill.git

import java.io.File
import mill._
import mill.define.{Command, Discover, ExternalModule}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import os._
import scala.util.Try

object GitVersionModule extends ExternalModule {

  /** Version derived from git.
    */
  def version(hashLength: Int = 7, withSnapshotSuffix: Boolean = false): Command[String] =
    T.command {
      val git            = Git.open(new File("."))
      val status         = git.status().call()
      val isDirty        = status.hasUncommittedChanges || !status.getUntracked.isEmpty
      val snapshotSuffix = if (withSnapshotSuffix) "-SNAPSHOT" else ""
      def uncommitted()  = s"${uncommittedHash(git, T.ctx().dest, hashLength)}$snapshotSuffix"

      Try(git.describe().setTags(true).setMatch("v[0-9]*").setAlways(true).call()).fold(
        _ => uncommitted(),
        description => {
          val taggedRegex   = """v(\d.*?)(?:-(\d+)-g([\da-f]+))?""".r
          val untaggedRegex = """([\da-f]+)""".r

          description match {
            case taggedRegex(tag, distance, hash) =>
              val distanceHash = Option(distance).fold {
                if (isDirty) s"-1-${uncommitted()}"
                else ""
              } { distance =>
                if (isDirty) s"-${distance.toInt + 1}-${uncommitted()}"
                else s"-$distance-${hash.take(hashLength)}$snapshotSuffix"
              }
              s"$tag$distanceHash"
            case untaggedRegex(hash) =>
              if (isDirty) uncommitted()
              else hash.take(hashLength)
          }
        }
      )
    }

  private def uncommittedHash(git: Git, temp: Path, hashLength: Int) = {
    val indexCopy = temp / "index"
    Try(copy(pwd / ".git" / "index", indexCopy, replaceExisting = true, createFolders = true))

    // Use different index file to avoid messing up current git status
    val altGit = Git.wrap(
      new RepositoryBuilder()
        .setFS(git.getRepository.getFS)
        .setGitDir(git.getRepository.getDirectory)
        .setIndexFile(indexCopy.toIO)
        .build()
    )
    val cache = altGit.add().addFilepattern(".").call()
    cache.writeTree(altGit.getRepository.newObjectInserter()).abbreviate(hashLength).name()
  }

  override lazy val millDiscover: Discover[this.type] = Discover[this.type]
}
