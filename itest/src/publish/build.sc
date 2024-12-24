import $file.plugins
import com.goyeau.mill.git.GitVersionedPublishModule
import mill._
import mill.scalalib.JavaModule
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import os._

object project extends JavaModule with GitVersionedPublishModule {
  override def pomSettings = PomSettings(
    description = "JVM Project",
    organization = "com.goyeau",
    url = "https://github.com/joan38/mill-git",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("joan38", "mill-git"),
    developers = Seq(Developer("joan38", "Joan Goyeau", "https://github.com/joan38"))
  )
}

// Uncommitted changes
def setupUncommittedChanges = T.input {
  remove.all(T.workspace / ".git")

  proc("git", "init").call(cwd = T.workspace)
}
def uncommittedChanges() = T.command {
  setupUncommittedChanges()

  assert("""[\da-f]{7}""".r.findFirstIn(project.publishVersion()).isDefined)
}

// Commit without tag
def setupCommitWithoutTag = T.input {
  remove.all(T.workspace / ".git")

  proc("git", "init").call(cwd = T.workspace)
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit").call(cwd = T.workspace)
}
def commitWithoutTag() = T.command {
  setupCommitWithoutTag()

  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(project.publishVersion() == hash)
}

// Uncommitted changes after commit without tag
def setupUncommittedChangesAfterCommitWithoutTag = T.input {
  remove.all(T.workspace / ".git")
  remove(T.workspace / "some-file")

  proc("git", "init").call(cwd = T.workspace)
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit").call(cwd = T.workspace)
  write(T.workspace / "some-file", "Some change!")
}
def uncommittedChangesAfterCommitWithoutTag() = T.command {
  setupUncommittedChangesAfterCommitWithoutTag()

  assert("""[\da-f]{7}""".r.findFirstIn(project.publishVersion()).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!project.publishVersion().contains(hash))
}

// Head tagged
def setupHeadTagged = T.input {
  remove.all(T.workspace / ".git")

  proc("git", "init").call(cwd = T.workspace)
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit").call(cwd = T.workspace)
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = T.workspace)
}
def headTagged() = T.command {
  setupHeadTagged()

  assert(project.publishVersion() == "1.0.0")
}

// Uncommitted changes after tag
def setupUncommittedChangesAfterTag = T.input {
  remove.all(T.workspace / ".git")
  remove(T.workspace / "some-file")

  proc("git", "init").call(cwd = T.workspace)
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit").call(cwd = T.workspace)
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = T.workspace)
  write(T.workspace / "some-file", "Some change!")
}
def uncommittedChangesAfterTag() = T.command {
  setupUncommittedChangesAfterTag()

  assert("""1\.0\.0-1-[\da-f]{7}""".r.findFirstIn(project.publishVersion()).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!project.publishVersion().contains(hash))
}

// Commit after tag
def setupCommitAfterTag = T.input {
  remove.all(T.workspace / ".git")
  remove(T.workspace / "some-file")

  proc("git", "init").call(cwd = T.workspace)
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit").call(cwd = T.workspace)
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = T.workspace)
  write(T.workspace / "some-file", "Some change!")
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit 2").call(cwd = T.workspace)
}
def commitAfterTag() = T.command {
  setupCommitAfterTag()

  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(project.publishVersion() == s"1.0.0-1-$hash")
}

// Uncommitted changes after tag and after commit
def setupUncommittedChangesAfterTagAndCommit = T.input {
  remove.all(T.workspace / ".git")
  remove(T.workspace / "some-file")

  proc("git", "init").call(cwd = T.workspace)
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit").call(cwd = T.workspace)
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = T.workspace)
  write(T.workspace / "some-file", "Some change!")
  proc("git", "add", "--all").call(cwd = T.workspace)
  proc("git", "commit", "-m", "Some commit 2").call(cwd = T.workspace)
  write.over(T.workspace / "some-file", "Some change 2!")
}
def uncommittedChangesAfterTagAndCommit() = T.command {
  setupUncommittedChangesAfterTagAndCommit()

  assert("""1\.0\.0-2-[\da-f]{7}""".r.findFirstIn(project.publishVersion()).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!project.publishVersion().contains(hash))
}
