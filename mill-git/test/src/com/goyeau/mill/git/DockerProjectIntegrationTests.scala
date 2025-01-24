package com.goyeau.mill.git

import munit.FunSuite

class DockerProjectIntegrationTests extends FunSuite {
  test("Uncommitted changes") {
    val tester = Tester.create(os.rel / "docker")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)

    val result = tester.eval(Seq("show", "project.docker.tags"))
    assert(result.isSuccess, result.err)
    assert(
      result.out.matches("""\[
                           |  "project:[\da-f]{7}",
                           |  "project:latest"
                           |\]""".stripMargin),
      s"${result.out} is not an array with 7 chars hash and latest tags"
    )
  }

  test("Commit without tag") {
    val tester = Tester.create(os.rel / "docker")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val hash   = os.proc("git", "rev-parse", "HEAD").call(cwd = tester.workspacePath).out.trim().take(7)

    val result = tester.eval(Seq("show", "project.docker.tags"))
    assert(result.isSuccess, result.err)
    assertEquals(
      result.out,
      s"""[
         |  "project:$hash",
         |  "project:latest"
         |]""".stripMargin
    )
  }

  test("Uncommitted changes after commit without tag") {
    val tester = Tester.create(os.rel / "docker")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")

    val result = tester.eval(Seq("show", "project.docker.tags"))
    assert(result.isSuccess, result.err)
    assert(
      result.out.matches("""\[
                           |  "project:[\da-f]{7}",
                           |  "project:latest"
                           |\]""".stripMargin),
      s"${result.out} is not a 7 chars hash"
    )
  }

  test("Head tagged") {
    val tester = Tester.create(os.rel / "docker")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)

    val result = tester.eval(Seq("show", "project.docker.tags"))
    assert(result.isSuccess, result.err)
    assertEquals(
      result.out,
      """[
        |  "project:1.0.0",
        |  "project:latest"
        |]""".stripMargin
    )
  }

  test("Uncommitted changes after tag") {
    val tester = Tester.create(os.rel / "docker")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")

    val result = tester.eval(Seq("show", "project.docker.tags"))
    assert(result.isSuccess, result.err)
    assert(
      result.out.matches("""\[
                           |  "project:1\.0\.0-1-[\da-f]{7}",
                           |  "project:latest"
                           |\]""".stripMargin),
      s"${result.out} is not a version and distance from it, followed by a 7 chars hash"
    )
  }

  test("Commit after tag") {
    val tester = Tester.create(os.rel / "docker")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit 2").call(cwd = tester.workspacePath)
    val hash   = os.proc("git", "rev-parse", "HEAD").call(cwd = tester.workspacePath).out.trim().take(7)

    val result = tester.eval(Seq("show", "project.docker.tags"))
    assert(result.isSuccess, result.err)
    assertEquals(
      result.out,
      s"""[
         |  "project:1.0.0-1-$hash",
         |  "project:latest"
         |]""".stripMargin
    )
  }

  test("Uncommitted changes after tag and after commit") {
    val tester = Tester.create(os.rel / "docker")
    val _      = os.proc("git", "init").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "tag", "-a", "v1.0.0", "-m", "v1.0.0").call(cwd = tester.workspacePath)
    val _      = os.write(tester.workspacePath / "some-file", "Some change!")
    val _      = os.proc("git", "add", "--all").call(cwd = tester.workspacePath)
    val _      = os.proc("git", "commit", "-m", "Some commit 2").call(cwd = tester.workspacePath)
    val _      = os.write.append(tester.workspacePath / "some-file", "Some change 2!")

    val result = tester.eval(Seq("show", "project.docker.tags"))
    assert(result.isSuccess, result.err)
    assert(
      result.out.matches("""\[
                           |  "project:1\.0\.0-2-[\da-f]{7}",
                           |  "project:latest"
                           |\]""".stripMargin),
      s"${result.out} is not a version and distance from it, followed by a 7 chars hash"
    )
  }
}
