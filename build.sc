import $ivy.`com.goyeau::mill-git:f90dd3f`
import $ivy.`com.lihaoyi::mill-contrib-bsp:$MILL_VERSION`
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest:0.2.1`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.2`
import com.goyeau.mill.git.GitVersionedPublishModule
import de.tobiasroeser.mill.integrationtest._
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import scalalib._
import mill.scalalib.scalafmt.ScalafmtModule

object `mill-git` extends ScalaModule with TpolecatModule with ScalafmtModule with GitVersionedPublishModule {
  def scalaVersion = "2.12.11"

  def millVersion = "0.6.2"
  override def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-main:$millVersion",
    ivy"com.lihaoyi::mill-contrib-docker:$millVersion"
  )

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "com.goyeau",
    url = "https://github.com/joan38/mill-git",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("joan38", "mill-git"),
    developers = Seq(Developer("joan38", "Joan Goyeau", "https://github.com/joan38"))
  )
}

object itest extends MillIntegrationTestModule {
  def millTestVersion  = "0.6.2"
  def pluginsUnderTest = Seq(`mill-git`)
  override def testInvocations =
    testCases().map(
      _ -> Seq(
        TestInvocation.Targets(Seq("all", "setupUncommittedChanges", "uncommittedChanges", "cleanUncommittedChanges")),
        TestInvocation.Targets(Seq("all", "setupCommitNoTag", "commitNoTag", "cleanCommitNoTag")),
        TestInvocation.Targets(Seq("all", "setupCommitNoTag", "commitNoTag", "cleanCommitNoTag")),
        TestInvocation.Targets(Seq("all", "setupHeadTagged", "headTagged", "cleanHeadTagged")),
        TestInvocation.Targets(
          Seq("all", "setupUncommittedChangesAfterTag", "uncommittedChangesAfterTag", "cleanUncommittedChangesAfterTag")
        ),
        TestInvocation.Targets(Seq("all", "setupCommitAfterTag", "commitAfterTag", "cleanCommitAfterTag"))
      )
    )
}
