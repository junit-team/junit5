//usr/bin/env jshell --show-version --execution local "$0" "$@"; exit $?

/open https://raw.githubusercontent.com/junit-team/junit5-samples/master/junit5-modular-world/BUILDING

run("javac", "--version")

//
// Resolve external libraries
//

get("build/modules/lib", "junit", "junit", "4.12")
get("build/modules/lib", "org.assertj", "assertj-core", "3.12.2")
get("build/modules/lib", "org.apiguardian", "apiguardian-api", "1.0.0")
get("build/modules/lib", "org.opentest4j", "opentest4j", "1.1.1")

//
// Compile module descriptors
//

run("javac", "@src/modules/javac-args.txt", "--module-version=1.5.0-SNAPSHOT", "@src/modules/platform.txt")
run("javac", "@src/modules/javac-args.txt", "--module-version=5.5.0-SNAPSHOT", "@src/modules/jupiter+vintage.txt")

//
// Upgrade selected plain jars to modular jars...
//

var commons = "junit-platform-commons/build/libs/junit-platform-commons-1.5.0-SNAPSHOT.jar"
run("jar", "--describe-module", "--file", commons)
run("jar", "--update", "--file", commons, "-C", "build/modules/bin/org.junit.platform.commons", ".")
run("jar", "--describe-module", "--file", commons)

var jupiter = "junit-jupiter-engine/build/libs/junit-jupiter-engine-5.5.0-SNAPSHOT.jar"
run("jar", "--describe-module", "--file", jupiter)
run("jar", "--update", "--file", jupiter, "-C", "build/modules/bin/org.junit.jupiter.engine", ".")
run("jar", "--describe-module", "--file", jupiter)

var vintage = "junit-vintage-engine/build/libs/junit-vintage-engine-5.5.0-SNAPSHOT.jar"
run("jar", "--describe-module", "--file", vintage)
run("jar", "--update", "--file", vintage, "-C", "build/modules/bin/org.junit.vintage.engine", ".")
run("jar", "--describe-module", "--file", vintage)

/exit
