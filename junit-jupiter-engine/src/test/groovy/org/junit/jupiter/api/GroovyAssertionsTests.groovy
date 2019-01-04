package org.junit.jupiter.api

import groovy.transform.CompileStatic

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals

@CompileStatic
class GroovyAssertionsTests {

    @Test
    void "integers can be passed to assertEquals"() {
        assertEquals(42, 42)
        assertEquals(42, Integer.valueOf(42))
        assertEquals(Integer.valueOf(42), 42)
        // TODO add overload for assertEquals(Integer, Integer)
        //assertEquals(Integer.valueOf(42), Integer.valueOf(42))
    }

    @Test
    void "integers can be passed to assertNotEquals"() {
        assertNotEquals(23, 42)
        assertNotEquals(23, Integer.valueOf(42))
        assertNotEquals(Integer.valueOf(23), 42)
        // TODO add overload for assertNotEquals(Integer, Integer)
        //assertNotEquals(Integer.valueOf(23), Integer.valueOf(42))
    }

}
