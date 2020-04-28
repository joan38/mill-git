import $exec.plugins
import com.goyeau.mill.git.GitVersionModule
import mill._
import mill.scalalib.JavaModule
import os._

object project extends JavaModule {
  def jobVersion = GitVersionModule.version
}

// Uncommitted changes
  def setupUncommittedChanges = T.input {
    proc("git", "init").call()
  }
def uncommittedChanges() = T.command {
    setupUncommittedChanges()

    assert("""[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)

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
    assert(project.jobVersion() == hash)

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

    assert("""[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(!project.jobVersion().contains(hash))

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

    assert(project.jobVersion() == "1.0.0")

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

    assert("""1\.0\.0-1-[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(!project.jobVersion().contains(hash))

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

    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(project.jobVersion() == s"1.0.0-1-$hash")

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

    assert("""1\.0\.0-2-[\da-f]{7}""".r.findFirstIn(project.jobVersion()).isDefined)
    val hash = proc("git", "rev-parse", "HEAD").call().out.trim().take(7)
    assert(!project.jobVersion().contains(hash))

    remove.all(pwd / ".git")
    remove(pwd / "some-file")
  }
