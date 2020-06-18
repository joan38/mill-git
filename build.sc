import $ivy.`com.goyeau::mill-git:0.1.0-4-9b459c6`
import $ivy.`com.goyeau::mill-scalafix:0.1.3`
import $ivy.`com.lihaoyi::mill-contrib-bsp:$MILL_VERSION`
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest:0.3.1`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.3`
import com.goyeau.mill.git.GitVersionedPublishModule
import com.goyeau.mill.scalafix.StyleModule
import de.tobiasroeser.mill.integrationtest._
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalalib._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}

object `mill-git` extends Cross[MillGitModule](crossScalaVersions: _*)
class MillGitModule(val crossScalaVersion: String)
    extends CrossScalaModule
    with TpolecatModule
    with StyleModule
    with GitVersionedPublishModule {
  lazy val millVersion = millVersionFor(crossScalaVersion)
  override def compileIvyDeps =
    super.compileIvyDeps() ++ Agg(
      ivy"com.lihaoyi::mill-main:$millVersion",
      ivy"com.lihaoyi::mill-contrib-docker:$millVersion"
    )
  override def ivyDeps = super.ivyDeps() ++ Agg(ivy"org.eclipse.jgit:org.eclipse.jgit:5.7.0.202003110725-r")

  override def artifactName = "mill-git"
  def pomSettings =
    PomSettings(
      description = "A git version plugin for Mill build tool",
      organization = "com.goyeau",
      url = "https://github.com/joan38/mill-git",
      licenses = Seq(License.MIT),
      versionControl = VersionControl.github("joan38", "mill-git"),
      developers = Seq(Developer("joan38", "Joan Goyeau", "https://github.com/joan38"))
    )
}

object itest extends Cross[IntegrationTestModule](crossScalaVersions: _*)
class IntegrationTestModule(val crossScalaVersion: String) extends MillIntegrationTestModule {
  override def millSourcePath = super.millSourcePath / ammonite.ops.up

  def millTestVersion  = millVersionFor(crossScalaVersion)
  def pluginsUnderTest = Seq(`mill-git`(crossScalaVersion))
  override def testInvocations =
    testCases().map(
      _ -> Seq(
        TestInvocation.Targets(Seq("uncommittedChanges")),
        TestInvocation.Targets(Seq("commitWithoutTag")),
        TestInvocation.Targets(Seq("uncommittedChangesAfterCommitWithoutTag")),
        TestInvocation.Targets(Seq("headTagged")),
        TestInvocation.Targets(Seq("uncommittedChangesAfterTag")),
        TestInvocation.Targets(Seq("commitAfterTag")),
        TestInvocation.Targets(Seq("uncommittedChangesAfterTagAndCommit"))
      )
    )
}

lazy val crossScalaVersions = Seq("2.13.2", "2.12.11")
def millVersionFor(scalaVersion: String) = if (scalaVersion.startsWith("2.13")) "0.6.2-35-7d1144" else "0.6.2"
