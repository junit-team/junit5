package junitbuild.extensions

import java.util.Locale

fun String.capitalized() = replaceFirstChar {
    it.uppercase(Locale.US)
}
