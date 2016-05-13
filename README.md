# JUnit 5

This repository is the home of the next generation of JUnit, _JUnit 5_.

The project is currently in _Phase 4_, working toward the first official [_milestone_](https://github.com/junit-team/junit5/milestones/5.0%20M1) release.

[JUnit 5.0.0-ALPHA](https://github.com/junit-team/junit5/releases/tag/r5.0.0-ALPHA) was released on February 1st, 2016.

## Roadmap

Consult the wiki for details on the current [JUnit 5 roadmap](https://github.com/junit-team/junit5/wiki#roadmap).

## Documentation

### User Guide

The [JUnit 5 User Guide] is available online.

### Javadoc

The [JUnit 5 Javadoc] is available online.

## Contributing

Contributions to JUnit 5 are both welcomed and appreciated.  For specific
guidelines regarding contributions, please see the [CONTRIBUTIONS.md](https://github.com/junit-team/junit5/blob/master/CONTRIBUTING.md) in the root directory of the project.
Those willing to use the ALPHA and SNAPSHOT releases are encouraged to file
feature requests and bug requests using the the project's [issue tracker](https://github.com/junit-team/junit5/issues).  Issues are marked with an <a href="#"><svg
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:cc="http://creativecommons.org/ns#"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:svg="http://www.w3.org/2000/svg"
   xmlns="http://www.w3.org/2000/svg"
   version="1.1"
   width="91.0625"
   height="19"
   id="svg2">
  <defs
     id="defs4" />
  <metadata
     id="metadata7">
    <rdf:RDF>
      <cc:Work
         rdf:about="">
        <dc:format>image/svg+xml</dc:format>
        <dc:type
           rdf:resource="http://purl.org/dc/dcmitype/StillImage" />
        <dc:title></dc:title>
      </cc:Work>
    </rdf:RDF>
  </metadata>
  <g
     transform="translate(-89.5,-151.875)"
     id="layer1">
    <rect
       width="90.226646"
       height="18.178434"
       rx="1.5612383"
       ry="1.9765702"
       x="89.910782"
       y="152.27278"
       id="rect2987"
       style="fill:#159818;fill-opacity:1;fill-rule:evenodd;stroke:#159818;stroke-width:0.82156605px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1" />
    <text
       x="99.757614"
       y="165.4026"
       id="text3757"
       xml:space="preserve"
       style="font-size:10px;font-style:normal;font-variant:normal;font-weight:bold;font-stretch:normal;text-align:start;line-height:125%;letter-spacing:0px;word-spacing:0px;writing-mode:lr-tb;text-anchor:start;fill:#ffffff;fill-opacity:1;stroke:none;font-family:Sans;-inkscape-font-specification:Sans Bold"><tspan
         x="99.757614"
         y="165.4026"
         id="tspan3759">up-for-grabs</tspan></text>
  </g>
</svg></a> label are specifically targeted for community contributions.

## Continuous Integration Builds

[![Travis CI build status](https://travis-ci.org/junit-team/junit5.svg?branch=master)](https://travis-ci.org/junit-team/junit5)

## Code Coverage

Code coverage using [Clover](https://www.atlassian.com/software/clover/) for the latest build is available on the [Jenkins CI server](https://junit.ci.cloudbees.com/job/JUnit5/lastSuccessfulBuild/clover-report/). We are thankful to [Atlassian](https://www.atlassian.com/) for providing the Clover license free of charge.

A code coverage report can also be generated locally by executing `gradlew -PenableClover clean cloverHtmlReport` if you have a local Clover license file on your computer. The results will be available in
`junit-tests/build/reports/clover/html/index.html`.


## Building from Source

All modules can be built with Gradle using the following command.

```
gradlew clean assemble
```

All modules can be tested with Gradle using the following command.

```
gradlew clean test
```

## Installing in Local Maven Repository

All modules can be installed in a local Maven repository for consumption in other projects via the following command.

```
gradlew clean install
```

## Dependency Metadata

- **Group ID**: `org.junit`
- **Version**: `5.0.0-ALPHA` OR `5.0.0-SNAPSHOT`
- **Artifact IDs**:
	- `junit-commons`
	- `junit-console`
	- `junit-engine-api`
	- `junit-gradle`
	- `junit-launcher`
	- `junit4-engine`
	- `junit4-runner`
	- `junit5-api`
	- `junit5-engine`
	- `surefire-junit5`

See also: <https://oss.sonatype.org/content/repositories/snapshots/org/junit/>

[JUnit 5 Javadoc]: https://junit.ci.cloudbees.com/job/JUnit5/javadoc/
[JUnit 5 User Guide]: http://junit-team.github.io/junit5/
[Prototype]: https://github.com/junit-team/junit5/wiki/Prototype
[Twitter]: https://twitter.com/junitlambda
