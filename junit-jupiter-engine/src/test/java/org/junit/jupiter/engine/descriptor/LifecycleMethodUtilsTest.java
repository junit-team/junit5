package org.junit.jupiter.engine.descriptor;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.commons.JUnitException;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.*;

class LifecycleMethodUtilsTest {

    @Test
    void findBeforeEachMethodsWithStandardLifecycle() {
        List<Method> methods = findBeforeEachMethods(TestCaseWithStandardLifecycle.class);

        assertEquals(2, methods.size());
        assertTrue(contains(methods, "nine"));
        assertTrue(contains(methods, "ten"));
    }

    @Test
    void findAfterEachMethodsWithStandardLifecycle() {
        List<Method> methods = findAfterEachMethods(TestCaseWithStandardLifecycle.class);

        assertEquals(2, methods.size());
        assertTrue(contains(methods, "eleven"));
        assertTrue(contains(methods, "twelve"));
    }

    @Test
    void findBeforeAllMethodsWithStandardLifecycleAndWithoutRequiringStatic() {
        List<Method> methods = findBeforeAllMethods(TestCaseWithStandardLifecycle.class, false);

        assertEquals(2, methods.size());
        assertTrue(contains(methods, "one"));
        assertTrue(contains(methods, "two"));
    }


    @Test
    void findBeforeAllMethodsWithStandardLifecycleAndRequiringStatic() {
        assertThrows(JUnitException.class, () -> findBeforeAllMethods(TestCaseWithStandardLifecycle.class, true));
    }

    @Test
    void findBeforeAllMethodsWithLifeCyclePerClassAndRequiringStatic() {
        List<Method> methods = findBeforeAllMethods(TestCaseWithLifecyclePerClass.class, false);

        assertEquals(2, methods.size());
        assertTrue(contains(methods, "three"));
        assertTrue(contains(methods, "four"));
    }

    @Test
    void findAfterAllMethodsWithStandardLifecycleAndWithoutRequiringStatic() {
        List<Method> methods = findAfterAllMethods(TestCaseWithStandardLifecycle.class, false);

        assertEquals(2, methods.size());
        assertTrue(contains(methods, "five"));
        assertTrue(contains(methods, "six"));
    }


    @Test
    void findAfterAllMethodsWithStandardLifecycleAndRequiringStatic() {
        assertThrows(JUnitException.class, () -> findAfterAllMethods(TestCaseWithStandardLifecycle.class, true));
    }

    @Test
    void findAfterAllMethodsWithLifeCyclePerClassAndRequiringStatic() {
        List<Method> methods = findAfterAllMethods(TestCaseWithLifecyclePerClass.class, false);

        assertEquals(2, methods.size());
        assertTrue(contains(methods, "seven"));
        assertTrue(contains(methods, "eight"));
    }

    private boolean contains(List<Method> methods, String methodName) {
        // @formatter:off
        return methods.stream()
                .map(Method::getName)
                .anyMatch(name -> name.equals(methodName));
        // @formatter:on
    }

}

class TestCaseWithStandardLifecycle {

    @BeforeAll
    void one(){}

    @BeforeAll
    void two(){}

    @BeforeEach
    void nine(){}

    @BeforeEach
    void ten(){}

    @AfterEach
    void eleven(){}

    @AfterEach
    void twelve(){}

    @AfterAll
    void five(){}

    @AfterAll
    void six(){}

}

@TestInstance(Lifecycle.PER_CLASS)
class TestCaseWithLifecyclePerClass {

    @BeforeAll
    void three(){}

    @BeforeAll
    void four(){}

    @AfterAll
    void seven(){}

    @AfterAll
    void eight(){}

}
