/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package example

// tag::user_guide[]
import example.domain.Person
import example.util.Calculator

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows

class AssertionsKotlinDemo {

    val person = Person("Jane", "Doe")
    val people = setOf(person, Person("John", "Doe"))

    val calculator = Calculator()

    @Test
    fun `grouped assertions`() {
        assertAll("person",
            { assertEquals("Jane", person.firstName) },
            { assertEquals("Doe", person.lastName) }
        )
    }

    @Test
    fun `exception testing`() {
        val exception = assertThrows<ArithmeticException> ("Should throw an exception") {
            calculator.divide(1, 0)
        }
        assertEquals("/ by zero", exception.message)
    }

    @Test
    fun `assertions from a stream`() {
        assertAll(
            "people with name starting with J",
            people
                .stream()
                .map {
                    // This mapping returns Stream<() -> Unit>
                    { assertTrue(it.firstName.startsWith("J")) }
                }
        )
    }

    @Test
    fun `assertions from a collection`() {
        assertAll(
            "people with last name of Doe",
            people.map { { assertEquals("Doe", it.lastName) } }
        )
    }
}
// end::user_guide[]
