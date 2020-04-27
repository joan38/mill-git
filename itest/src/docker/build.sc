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
  def setupUncommittedChanges() = T.command {
    proc("git", "init").call()
  }
def uncommittedChanges() = T.command {
    val tags = project.docker.tags()
    assert(tags.size == 2)
    assert("""project:[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
    assert(tags(1) == "project:latest")
  }
def cleanUncommittedChanges() = T.command {
    remove.all(pwd / ".git")
  }

// Commit no tag
  def setupCommitNoTag() = T.command {
    proc("git", "init").call()
    proc("git", "add", "--all").call()
    proc("git", "commit", "-m", "Some commit").call()
  }
def commitNoTag() = T.command {
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    val tags = project.docker.tags()
    assert(tags.size == 2)
    assert(tags(0) == s"project:$hash")
    assert(tags(1) == "project:latest")
  }
def cleanCommitNoTag() = T.command {
    remove.all(pwd / ".git")
  }

// Head tagged
  def setupHeadTagged() = T.command {
    proc("git", "init").call()
    proc("git", "add", "--all").call()
    proc("git", "commit", "-m", "Some commit").call()
    proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call()
  }
def headTagged() = T.command {
    val tags = project.docker.tags()
    assert(tags.size == 2)
    assert(tags(0) == s"project:1.0.0")
    assert(tags(1) == "project:latest")
  }
def cleanHeadTagged() = T.command {
    remove.all(pwd / ".git")
  }

// Uncommitted changes after tag
  def setupUncommittedChangesAfterTag() = T.command {
    proc("git", "init").call()
    proc("git", "add", "--all").call()
    proc("git", "commit", "-m", "Some commit").call()
    proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call()
    write(pwd / "some-file", "Some change!")
  }
def uncommittedChangesAfterTag() = T.command {
    val tags = project.docker.tags()
    assert(tags.size == 2)
    assert("""project:1\.0\.0-1-[\da-f]{7}""".r.findFirstIn(tags(0)).isDefined)
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(!tags(0).contains(hash))
    assert(tags(1) == "project:latest")
  }
def cleanUncommittedChangesAfterTag() = T.command {
    remove.all(pwd / ".git")
    remove(pwd / "some-file")
  }

// Commit after tag
  def setupCommitAfterTag() = T.command {
    proc("git", "init").call()
    proc("git", "add", "--all").call()
    proc("git", "commit", "-m", "Some commit").call()
    proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call()
    write(pwd / "some-file", "Some change!")
    proc("git", "add", "--all").call()
    proc("git", "commit", "-m", "Some commit 2").call()
  }
def commitAfterTag() = T.command {
    val tags = project.docker.tags()
    assert(tags.size == 2)
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(tags(0) == s"project:1.0.0-1-$hash")
    assert(tags(1) == "project:latest")
  }
def cleanCommitAfterTag() = T.command {
    remove.all(pwd / ".git")
    remove(pwd / "some-file")
  }
