package com.goyeau.mill.git

import mill.testkit.ExampleTester
import munit.FunSuite

class ExampleTests extends FunSuite {
  test("Custom example") {
    val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
    ExampleTester.run(
      clientServerMode = true,
      workspaceSourcePath = resourceFolder / "custom",
      millExecutable = os.Path(sys.env("MILL_EXECUTABLE_PATH"))
    )
  }
}
