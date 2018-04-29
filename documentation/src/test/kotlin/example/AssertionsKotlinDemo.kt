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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows

class AssertionsKotlinDemo {

    // end::user_guide[]
    val person = Person("John", "Doe")
    val people = setOf(person, Person("Jane", "Doe"))

    // tag::user_guide[]
    @Test
    fun `grouped assertions`() {
        assertAll("person",
            { assertEquals("John", person.firstName) },
            { assertEquals("Doe", person.lastName) }
        )
    }

    @Test
    fun `exception testing`() {
        val exception = assertThrows<IllegalArgumentException> ("Should throw an exception") {
            throw IllegalArgumentException("a message")
        }
        assertEquals("a message", exception.message)
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
