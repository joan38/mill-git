import $ivy.`com.goyeau::mill-git:0.2.2`
import $ivy.`com.goyeau::mill-scalafix:0.2.5`
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest_mill0.9:0.4.0`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.2.0`
import com.goyeau.mill.git.{GitVersionModule, GitVersionedPublishModule}
import com.goyeau.mill.scalafix.StyleModule
import de.tobiasroeser.mill.integrationtest._
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalalib._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}

object `mill-git` extends ScalaModule with TpolecatModule with StyleModule with GitVersionedPublishModule {
  override def scalaVersion = "2.13.6"

  lazy val millVersion = "0.9.9"
  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-main:$millVersion",
    ivy"com.lihaoyi::mill-scalalib:$millVersion",
    ivy"com.lihaoyi::mill-contrib-docker:$millVersion"
  )
  override def ivyDeps = super.ivyDeps() ++ Agg(ivy"org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")

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

object itest extends MillIntegrationTestModule {
  override def millSourcePath = super.millSourcePath / ammonite.ops.up

  def millTestVersion  = `mill-git`.millVersion
  def pluginsUnderTest = Seq(`mill-git`)
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
