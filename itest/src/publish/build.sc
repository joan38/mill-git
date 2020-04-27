import $exec.plugins
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
  def setupUncommittedChanges() = T.command {
    proc("git", "init").call()
  }
def uncommittedChanges() = T.command {
    assert("""[\da-f]{7}""".r.findFirstIn(project.publishVersion()).isDefined)
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
    assert(project.publishVersion() == hash)
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
    assert(project.publishVersion() == "1.0.0")
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
    assert("""1\.0\.0-1-[\da-f]{7}""".r.findFirstIn(project.publishVersion()).isDefined)
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(!project.publishVersion().contains(hash))
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
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(project.publishVersion() == s"1.0.0-1-$hash")
  }
def cleanCommitAfterTag() = T.command {
    remove.all(pwd / ".git")
    remove(pwd / "some-file")
  }
