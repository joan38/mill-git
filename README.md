# mill-git

![Maven Central](https://img.shields.io/maven-central/v/com.goyeau/mill-git_mill0.10_2.13)

A git version plugin for Mill build tool.


## Requirements

 * [Mill](https://www.lihaoyi.com/mill)
 * An initialized git project (`git init`)


## Usage

### With PublishModule

*build.sc*:
```scala
import $ivy.`com.goyeau::mill-git:<latest version>`
import com.goyeau.mill.git.GitVersionedPublishModule
import mill.scalalib.JavaModule
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}

object `jvm-project` extends JavaModule with GitVersionedPublishModule {
  override def pomSettings = PomSettings(
    description = "JVM Project",
    organization = "com.goyeau",
    url = "https://github.com/joan38/mill-git",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("joan38", "mill-git"),
    developers = Seq(Developer("joan38", "Joan Goyeau", "https://github.com/joan38"))
  )
}
```

```shell script
> mill show jvm-project.publishVersion
[1/1] show 
[2/2] com.goyeau.mill.git.GitVersionModule.version 
"0.0.0-470-6d0b3d9"
```

### With DockerModule

*build.sc*:
```scala
import $ivy.`com.goyeau::mill-git:<latest version>`
import com.goyeau.mill.git.GitTaggedDockerModule
import mill.scalalib.JavaModule

object `docker-project` extends JavaModule with GitTaggedDockerModule {
  object docker extends DockerConfig with GitTaggedDocker {
    def tagLatest = true // Default is false
  }
}
```

```shell script
> mill show docker-project.docker.tags
[1/1] show 
[6/6] docker-project.docker.tags 
[
    "docker-project:0.0.0-470-6d0b3d9",
    "docker-project:latest"
]
```

### Custom options

Here is a custom configuration with the default option:
*build.sc*:
```scala
import $ivy.`com.goyeau::mill-git:<latest version>`
import com.goyeau.mill.git.GitVersionModule
import mill.scalalib.JavaModule

object `job-project` extends JavaModule {
  def jobVersion = GitVersionModule.version(
    hashLength = 7,            // Sets the length of the commit hash to use as a version.
    withSnapshotSuffix = false // Add the -SNAPSHOT suffix so that versions gets pushed to the snapshot sonatype.
  )()
}
```

```shell script
> mill show job-project.jobVersion
[1/1] show 
[2/2] com.goyeau.mill.git.GitVersionModule.version 
"0.0.0-470-6d0b3d9"
```


## Versioning strategy

| Tag    | Tag distance | HEAD hash | Uncommitted changes | Uncommitted hash | gitVersion        |
|:------ |:------------:|:---------:|:-------------------:|:----------------:|:----------------- |
| v1.0.0 | 0            | c85ec8a   | false               |                  | 1.0.0             |
| v1.0.0 | 0            | c85ec8a   | true                | 303eee4          | 1.0.0-1-303eee4   |
| v1.0.0 | 123          | c85ec8a   | false               |                  | 1.0.0-123-c85ec8a |
| v1.0.0 | 123          | c85ec8a   | true                | 303eee4          | 1.0.0-124-303eee4 |
| none   |              | c85ec8a   | false               |                  | c85ec8a           |
| none   |              | c85ec8a   | true                | 303eee4          | 303eee4           |
| none   |              | none      | true                | 303eee4          | 303eee4           |

Note that we are generating a git hash for the uncommitted changes (even if they are not committed yet).
 

## Related projects

* Inspired by [sbt-dynver](https://github.com/dwijnand/sbt-dynver)
* [sbt-git](https://github.com/sbt/sbt-git)


## Contributing

Contributions are more than welcome!  
See [CONTRIBUTING.md](CONTRIBUTING.md) for all the information and getting help.
