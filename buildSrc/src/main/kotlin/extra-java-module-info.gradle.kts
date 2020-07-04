plugins {
    id("de.jjohannes.extra-java-module-info")
}

extraJavaModuleInfo {
    module("univocity-parsers-${versions["univocity-parsers"]}.jar", "univocity-parsers", versions["univocity-parsers"]) {
        exports("com.univocity.parsers.csv")
    }
    module("kotlin-stdlib-${versions["kotlin.plugin"]}.jar", "kotlin-stdlib", versions["kotlin.plugin"])
    module("kotlin-stdlib-common-${versions["kotlin.plugin"]}.jar", "kotlin-stdlib-common", versions["kotlin.plugin"])
    module("annotations-13.0.jar", "jetbrains-annotations", "13.0")
    module("hamcrest-core-1.3.jar", "hamcrest-core", "1.3")
}
