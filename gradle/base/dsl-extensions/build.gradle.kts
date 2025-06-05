import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions.jvmTarget = JVM_21
}
