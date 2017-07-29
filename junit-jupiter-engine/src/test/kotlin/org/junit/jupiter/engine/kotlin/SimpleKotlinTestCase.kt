package org.junit.jupiter.engine.kotlin

import org.junit.jupiter.api.*

class SimpleKotlinTestCase {

    companion object {
        @JvmField
        val TEST_INSTANCES: MutableMap<Any, MutableMap<String, Int>> = HashMap()
    }

    @BeforeAll
    fun beforeAll() {
        increment("beforeAll")
    }

    @BeforeEach
    fun beforeEach() {
        increment("beforeEach")
    }

    @AfterEach
    fun afterEach() {
        increment("afterEach")
    }

    @AfterAll
    fun afterAll() {
        increment("afterAll")
    }

    @Test
    fun firstTest() {
        increment("test")
    }

    @Test
    fun secondTest() {
        increment("test")
    }

    private fun increment(name: String) {
        TEST_INSTANCES.computeIfAbsent(this, { _ -> HashMap() })
                .compute(name, { _, oldValue -> (oldValue ?: 0) + 1 })
    }
}
