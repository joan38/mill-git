//package com.goyeau.mill.git
//
//import mill.testkit.ExampleTester
//import munit.FunSuite
//import scala.concurrent.duration.*
//
//class ExampleTests extends FunSuite {
//  override val munitTimeout: Duration = 1.minute
//
//  test("Custom example") {
//    val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
//    ExampleTester.run(
//      daemonMode = true,
//      workspaceSourcePath = resourceFolder / "custom",
//      millExecutable = os.Path(sys.env("MILL_EXECUTABLE_PATH"))
//    )
//  }
//}
