package org.junit.jupiter.engine.kotlin

import org.junit.jupiter.api.*

class InstancePerMethodKotlinTestCase {

    companion object {
        @JvmField
        val TEST_INSTANCES: MutableMap<Any, MutableMap<String, Int>> = LinkedHashMap()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            increment(this, "beforeAll")
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            increment(this, "afterAll")
        }

        private fun increment(instance: Any, name: String) {
            TEST_INSTANCES.computeIfAbsent(instance, { _ -> LinkedHashMap() })
                    .compute(name, { _, oldValue -> (oldValue ?: 0) + 1 })
        }
    }

    @BeforeEach
    fun beforeEach() {
        increment(this, "beforeEach")
    }

    @AfterEach
    fun afterEach() {
        increment(this, "afterEach")
    }

    @Test
    fun firstTest() {
        increment(this, "test")
    }

    @Test
    fun secondTest() {
        increment(this, "test")
    }
}
