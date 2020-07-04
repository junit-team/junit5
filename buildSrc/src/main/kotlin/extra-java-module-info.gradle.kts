plugins {
    id("de.jjohannes.extra-java-module-info")
}

extraJavaModuleInfo {
    automaticModule("univocity-parsers-${versions["univocity-parsers"]}.jar", "univocity-parsers")
    automaticModule("kotlin-stdlib-1.3.72.jar", "kotlin-stdlib")
    automaticModule("kotlin-stdlib-common-1.3.72.jar", "kotlin-stdlib-common")
    automaticModule("annotations-13.0.jar", "jetbrains-annotations")
    automaticModule("hamcrest-core-1.3.jar", "hamcrest-core")
}
