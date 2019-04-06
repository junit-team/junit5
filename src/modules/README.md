# JUnit 5 Modules

![Dependency Diagram](https://junit.org/junit5/docs/current/user-guide/images/component-diagram.svg)

## Compile Module Descriptors

- Change to root "junit5" directory.
- `gradlew build`
- `jshell src\modules\build.jsh`
- Find compiled module descriptors in `build/modules/bin/${module}`.

```text
├───bin
│   ├───org.junit.jupiter
│   │       module-info.class
│   │
│   ├───org.junit.jupiter.api
│   │       module-info.class
│   │
│   ├───org.junit.jupiter.engine
8< ...
│   │
│   └───org.junit.vintage.engine
│           module-info.class
│
└───lib
        apiguardian-api-1.0.0.jar
        assertj-core-3.12.2.jar
        junit-4.12.jar
        opentest4j-1.1.1.jar

```

## Known Warnings

All 4 external libraries are not explicit modules, yet.

```text
javac @src/modules/javac-args.txt --module-version 1.5.0-SNAPSHOT @src/modules/platform.txt

src\modules\org.junit.platform.commons\module-info.java:14: warning: requires transitive directive for an automatic module
        requires transitive org.apiguardian.api;
                                           ^
src\modules\org.junit.platform.runner\module-info.java:13: warning: requires transitive directive for an automatic module
        requires transitive junit; // 4
                            ^
src\modules\org.junit.platform.suite.api\module-info.java:12: warning: requires transitive directive for an automatic module
        requires transitive org.apiguardian.api;
                                           ^
src\modules\org.junit.platform.testkit\module-info.java:12: warning: requires transitive directive for an automatic module
        requires transitive org.assertj.core;
                                       ^
src\modules\org.junit.platform.testkit\module-info.java:14: warning: requires transitive directive for an automatic module
        requires transitive org.opentest4j;
                               ^
5 warnings
```

```text
javac @src/modules/javac-args.txt --module-version 5.5.0-SNAPSHOT @src/modules/jupiter+vintage.txt

src\modules\org.junit.jupiter.api\module-info.java:13: warning: requires transitive directive for an automatic module
        requires transitive org.opentest4j;
                               ^
src\modules\org.junit.jupiter.migrationsupport\module-info.java:13: warning: requires transitive directive for an automatic module
        requires transitive junit; // 4
                            ^
src\modules\org.junit.vintage.engine\module-info.java:13: warning: requires transitive directive for an automatic module
        requires transitive junit; // 4
                            ^
3 warnings
```
