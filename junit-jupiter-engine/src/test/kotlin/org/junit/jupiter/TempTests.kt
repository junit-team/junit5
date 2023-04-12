package org.junit.jupiter

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Test
fun failure() = assertTrue(false)

@Test
fun success() = assertTrue(true)
