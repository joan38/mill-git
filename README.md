# mill-git

[![Latest version](https://index.scala-lang.org/joan38/mill-git/mill-git/latest.svg?color=lightgrey)](https://index.scala-lang.org/joan38/mill-git/mill-git)

A git version plugin for Mill build tool.


## Requirements

 * [Mill](https://www.lihaoyi.com/mill)
 * A initialized git project (`git init`)


## Usage

### With PublishModule

*build.sc*:
```scala
import $ivy.`com.goyeau::mill-git:<latest version>`
import com.goyeau.mill.git.GitVersionedPublishModule

object `jvm-project` extends JavaModule with GitVersionedPublishModule
```

```shell script
> mill show jvm-project.publishVersion
[1/1] show 
[2/2] jvm-project.gitVersion 
"0.0.0-123-303eee40"
```

### With DockerModule

*build.sc*:
```scala
import $ivy.`com.goyeau::mill-git:<latest version>`
import com.goyeau.mill.git.GitTaggedDockerModule

object `docker-project` extends JavaModule with GitTaggedDockerModule {
  object docker extends DockerConfig with GitTaggedDocker {
    def tagLatest = true // Default is false
  }
}
```

```shell script
> mill show docker-project.docker.tags
[1/1] show 
[4/6] docker-project.gitVersion 
[
    "docker-project:0.0.0-123-303eee40",
    "docker-project:latest"
]
```

### Custom

```scala
import $ivy.`com.goyeau::mill-git:<latest version>`
import com.goyeau.mill.git.GitVersionedModule

object `job-project` extends JavaModule with GitVersionedModule {
  def jobVersion = gitVersion
}
```

```shell script
> mill show job-project.jobVersion
[1/1] show 
[2/2] job-project.gitVersion 
"0.0.0-123-303eee40"
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

Note that we are generating a git hash for the uncommitted changes even if they have not been committed.
 

## Related projects

* Inspired from [sbt-dynver](https://github.com/dwijnand/sbt-dynver)
* [sbt-git](https://github.com/sbt/sbt-git)


## Contributing

Contributions are more than welcome!  
See [CONTRIBUTING.md](CONTRIBUTING.md) for all the information and getting help.
