package com.goyeau.mill.git

import mill.*
import mill.Task.workspace
import mill.api.Result
import mill.define.Discover
import mill.define.ExternalModule
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import os.*

import scala.util.Failure as TryFailure
import scala.util.Success as TrySuccess
import scala.util.Try

object GitVersionModule extends ExternalModule {

  /** Version derived from git.
    */
  def version(hashLength: Int = 7, withSnapshotSuffix: Boolean = false): Command[String] =
    Task.Command {
      val git            = Git.open(workspace.toIO)
      val status         = git.status().call()
      val isDirty        = status.hasUncommittedChanges || !status.getUntracked.isEmpty
      val snapshotSuffix = if (withSnapshotSuffix) "-SNAPSHOT" else ""
      def uncommitted()  = s"${uncommittedHash(git, hashLength)}$snapshotSuffix"

      val describeResult = Try(git.describe().setTags(true).setMatch("v[0-9]*").setAlways(true).call())

      describeResult match {
        case TryFailure(_) => Result.Success(uncommitted())
        case TrySuccess(description) =>
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
              Result.Success(s"$tag$distanceHash")
            case untaggedRegex(hash) =>
              if (isDirty) Result.Success(uncommitted())
              else Result.Success(s"${hash.take(hashLength)}$snapshotSuffix")
            case _ => Result.Failure(s"Unexpected git describe output: $description")
          }
      }
    }

  private def uncommittedHash(git: Git, hashLength: Int): String = {
    val indexCopy = os.temp.dir() / "index"
    val _         = Try(copy(pwd / ".git" / "index", indexCopy, replaceExisting = true, createFolders = true))

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

  override lazy val millDiscover = Discover[this.type]
}
