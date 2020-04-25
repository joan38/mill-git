import $ivy.`com.goyeau::mill-git:f90dd3f`
import $ivy.`com.lihaoyi::mill-contrib-bsp:$MILL_VERSION`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.2`
import com.goyeau.mill.git.GitVersionedPublishModule
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
