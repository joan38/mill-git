import $exec.plugins
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
  proc("git", "init").call()
}
def uncommittedChanges() = T.command {
  setupUncommittedChanges()

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  assert(tags(1) == "project:latest")

  remove.all(pwd / ".git")
}

// Commit without tag
def setupCommitWithoutTag = T.input {
  proc("git", "init").call()
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit").call()
}
def commitWithoutTag() = T.command {
  setupCommitWithoutTag()

  val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert(tags(0) == s"project:$hash")
  assert(tags(1) == "project:latest")

  remove.all(pwd / ".git")
}

// Uncommitted changes after commit without tag
def setupUncommittedChangesAfterCommitWithoutTag = T.input {
  proc("git", "init").call()
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit").call()
  write(pwd / "some-file", "Some change!")
}
def uncommittedChangesAfterCommitWithoutTag() = T.command {
  setupUncommittedChangesAfterCommitWithoutTag()

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
  assert(!tags(0).contains(hash))
  assert(tags(1) == "project:latest")

  remove.all(pwd / ".git")
  remove(pwd / "some-file")
}

// Head tagged
def setupHeadTagged = T.input {
  proc("git", "init").call()
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit").call()
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call()
}
def headTagged() = T.command {
  setupHeadTagged()

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert(tags(0) == s"project:1.0.0")
  assert(tags(1) == "project:latest")

  remove.all(pwd / ".git")
}

// Uncommitted changes after tag
def setupUncommittedChangesAfterTag = T.input {
  proc("git", "init").call()
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit").call()
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call()
  write(pwd / "some-file", "Some change!")
}
def uncommittedChangesAfterTag() = T.command {
  setupUncommittedChangesAfterTag()

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:1\.0\.0-1-[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
  assert(!tags(0).contains(hash))
  assert(tags(1) == "project:latest")

  remove.all(pwd / ".git")
  remove(pwd / "some-file")
}

// Commit after tag
def setupCommitAfterTag = T.input {
  proc("git", "init").call()
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit").call()
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call()
  write(pwd / "some-file", "Some change!")
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit 2").call()
}
def commitAfterTag() = T.command {
  setupCommitAfterTag()

  val tags = project.docker.tags()
  assert(tags.size == 2)
  val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
  assert(tags(0) == s"project:1.0.0-1-$hash")
  assert(tags(1) == "project:latest")

  remove.all(pwd / ".git")
  remove(pwd / "some-file")
}

// Uncommitted changes after tag and after commit
def setupUncommittedChangesAfterTagAndCommit = T.input {
  proc("git", "init").call()
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit").call()
  proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call()
  write(pwd / "some-file", "Some change!")
  proc("git", "add", "--all").call()
  proc("git", "commit", "-m", "Some commit 2").call()
  write.over(pwd / "some-file", "Some change 2!")
}
def uncommittedChangesAfterTagAndCommit() = T.command {
  setupUncommittedChangesAfterTagAndCommit()

  val tags = project.docker.tags()
  assert(tags.size == 2)
  assert("""project:1\.0\.0-2-[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
  val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
  assert(!tags(0).contains(hash))
  assert(tags(1) == "project:latest")

  remove.all(pwd / ".git")
  remove(pwd / "some-file")
}
