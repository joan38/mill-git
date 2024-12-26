import $file.plugins
import com.goyeau.mill.git.GitVersionModule
import mill._
import mill.scalalib.JavaModule
import os._

object project extends JavaModule {
  def jobVersion = T(GitVersionModule.version()())
}

// Uncommitted changes
def setupUncommittedChanges = T.input {
  remove.all(T.workspace / ".git")

  proc("git", "init").call(cwd = T.workspace)
}
def uncommittedChanges() = T.command {
  setupUncommittedChanges()

  assert("""[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)

  remove.all(T.workspace / ".git")
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
  assert(project.jobVersion() == hash)

  remove.all(T.workspace / ".git")
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

  assert("""[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!project.jobVersion().contains(hash))
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

  assert(project.jobVersion() == "1.0.0")
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

  assert("""1\.0\.0-1-[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!project.jobVersion().contains(hash))
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
  assert(project.jobVersion() == s"1.0.0-1-$hash")
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

  assert("""1\.0\.0-2-[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!project.jobVersion().contains(hash))
}
