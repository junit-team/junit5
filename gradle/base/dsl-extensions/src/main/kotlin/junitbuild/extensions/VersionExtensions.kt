package junitbuild.extensions

fun Any.isSnapshot(): Boolean = toString().contains("SNAPSHOT")
