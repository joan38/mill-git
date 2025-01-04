import $ivy.`com.goyeau::mill-git::0.2.6`
import $ivy.`com.goyeau::mill-scalafix::0.4.2`
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.7.1`
import $ivy.`org.typelevel::scalac-options:0.1.7`
import com.goyeau.mill.git.{GitVersionModule, GitVersionedPublishModule}
import com.goyeau.mill.scalafix.StyleModule
import de.tobiasroeser.mill.integrationtest._
import mill._
import mill.scalalib._
import mill.scalalib.api.ZincWorkerUtil.scalaNativeBinaryVersion
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import org.typelevel.scalacoptions.ScalacOptions._
import org.typelevel.scalacoptions.{ScalaVersion, ScalacOptions}

val millVersions                           = Seq("0.10.12", "0.11.1", "0.12.4")
def millBinaryVersion(millVersion: String) = scalaNativeBinaryVersion(millVersion)

object `mill-git` extends Cross[MillGitCross](millVersions: _*)
class MillGitCross(millVersion: String) extends CrossModuleBase with StyleModule with GitVersionedPublishModule {
  override def crossScalaVersion = "2.13.10"
  override def scalacOptions = super.scalacOptions() ++ ScalacOptions.tokensForVersion(
    ScalaVersion.unsafeFromString(scalaVersion()),
    ScalacOptions.default + source3 ++ fatalWarningOptions
  )
  override def artifactSuffix = s"_mill${millBinaryVersion(millVersion)}" + super.artifactSuffix()

  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-main:$millVersion",
    ivy"com.lihaoyi::mill-scalalib:$millVersion",
    ivy"com.lihaoyi::mill-contrib-docker:$millVersion"
  )
  override def ivyDeps = super.ivyDeps() ++ Agg(ivy"org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")

  override def publishVersion = GitVersionModule.version(withSnapshotSuffix = true)()
  def pomSettings = PomSettings(
    description = "A git version plugin for Mill build tool",
    organization = "com.goyeau",
    url = "https://github.com/joan38/mill-git",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("joan38", "mill-git"),
    developers = Seq(Developer("joan38", "Joan Goyeau", "https://github.com/joan38"))
  )
}

object itest extends Cross[ITestCross](millVersions: _*)
class ITestCross(millVersion: String) extends MillIntegrationTestModule {
  override def millSourcePath   = super.millSourcePath / os.up
  override def millTestVersion  = millVersion
  override def pluginsUnderTest = Seq(`mill-git`(millVersion))
  override def testInvocations = testCases().map(
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
