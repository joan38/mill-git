package com.goyeau.mill.git

import munit.FunSuite

import scala.concurrent.duration.*

class CustomProjectIntegrationTests extends FunSuite:
  override val munitTimeout: Duration = 2.minute

  test("Uncommitted changes") {
    val tester = Tester.create(os.rel / "custom")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)

    val result = tester.eval(Seq("show", "project.jobVersion"))
    assert(result.isSuccess, result.err)
    assert(result.out.matches("""(?s).*"[\da-f]{7}""""), s"${result.out} is not a 7 chars hash")
  }

  test("Commit without tag") {
    val tester = Tester.create(os.rel / "custom")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val hash   = os.proc("git", "rev-parse", "HEAD").call(cwd = tester.workspacePath).out.trim().take(7)

    val result = tester.eval(Seq("show", "project.jobVersion"))
    assert(result.isSuccess, result.err)
    assert(result.out.contains(s""""$hash""""))
  }

  test("Uncommitted changes after commit without tag") {
    val tester = Tester.create(os.rel / "custom")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")

    val result = tester.eval(Seq("show", "project.jobVersion"))
    assert(result.isSuccess, result.err)
    assert(result.out.matches("""(?s).*"[\da-f]{7}""""), s"${result.out} is not a 7 chars hash")
  }

  test("Head tagged") {
    val tester = Tester.create(os.rel / "custom")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)

    val result = tester.eval(Seq("show", "project.jobVersion"))
    assert(result.isSuccess, result.err)
    assert(result.out.contains(""""1.0.0""""))
  }

  test("Uncommitted changes after tag") {
    val tester = Tester.create(os.rel / "custom")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")

    val result = tester.eval(Seq("show", "project.jobVersion"))
    assert(result.isSuccess, result.err)
    assert(
      result.out.matches("""(?s).*"1\.0\.0-1-[\da-f]{7}""""),
      s"${result.out} is not a version and distance from it, followed by a 7 chars hash"
    )
  }

  test("Commit after tag") {
    val tester = Tester.create(os.rel / "custom")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit 2").call(cwd = tester.workspacePath)
    val hash   = os.proc("git", "rev-parse", "HEAD").call(cwd = tester.workspacePath).out.trim().take(7)

    val result = tester.eval(Seq("show", "project.jobVersion"))
    assert(result.isSuccess, result.err)
    assert(result.out.contains(s""""1.0.0-1-$hash""""))
  }

  test("Uncommitted changes after tag and after commit") {
    val tester = Tester.create(os.rel / "custom")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit 2").call(cwd = tester.workspacePath)
    val _      = os.write.append(tester.workspacePath / "some-file", "Some change 2!")

    val result = tester.eval(Seq("show", "project.jobVersion"))
    assert(result.isSuccess, result.err)
    assert(
      result.out.matches("""(?s).*"1\.0\.0-2-[\da-f]{7}""""),
      s"${result.out} is not a version and distance from it, followed by a 7 chars hash"
    )
  }
