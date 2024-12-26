import $file.plugins
import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`
import com.goyeau.mill.git.GitTaggedDockerModule
import mill._
import mill.contrib.docker.DockerModule
import mill.scalalib.JavaModule
import os._

object project extends JavaModule with GitTaggedDockerModule {
  object docker extends DockerConfig with GitTaggedDocker {
    override def tagLatest = true // Default is false
  }
}

// Uncommitted changes
def setupUncommittedChanges = T.input {
  remove.all(T.workspace / ".git")

  proc("git", "init").call(cwd = T.workspace)
}
def uncommittedChanges() = T.command {
  setupUncommittedChanges()

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  assert(tags(1) == "project:latest")
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
  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert(tags(0) == s"project:$hash")
  assert(tags(1) == "project:latest")
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

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!tags(0).contains(hash))
  assert(tags(1) == "project:latest")
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

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert(tags(0) == s"project:1.0.0")
  assert(tags(1) == "project:latest")
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

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:1\.0\.0-1-[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!tags(0).contains(hash))
  assert(tags(1) == "project:latest")
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

  val tags = project.docker.tags()
  assert(tags.size == 2)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(tags(0) == s"project:1.0.0-1-$hash")
  assert(tags(1) == "project:latest")
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

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:1\.0\.0-2-[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call(cwd = T.workspace).out.trim().take(7)
  assert(!tags(0).contains(hash))
  assert(tags(1) == "project:latest")
}
