package org.junit.jupiter.theories.domain;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@linke TheoryParameterDetails}.
 */
class TheoryParameterDetailsTests {
    @Test
    public void testConstructorAndGetters() {
        //Setup
        int expectedIndex = 42;
        Class<?> expectedType = int.class;
        Class<?> expectedNonPrimitiveType = Integer.class;
        String expectedName = "testParameter";
        List<String> expectedQualifiers = Arrays.asList("foo", "bar", "baz");
        Optional<? extends Annotation> expectedParameterSupplierAnnotation = Optional.empty();

        //Test
        TheoryParameterDetails objectUnderTest = new TheoryParameterDetails(expectedIndex, expectedType, expectedName, expectedQualifiers,
                expectedParameterSupplierAnnotation);
      
        int actualIndex = objectUnderTest.getIndex();
        Class<?> actualType = objectUnderTest.getType();
        Class<?> actualNonPrimitiveType = objectUnderTest.getNonPrimitiveType();
        String actualName = objectUnderTest.getName();
        List<String> actualQualifiers = objectUnderTest.getQualifiers();
        Optional<? extends Annotation> actualParameterSupplierAnnotation = objectUnderTest.getArgumentSupplierAnnotation();

        //Verify
        assertEquals(expectedIndex, actualIndex);
        assertEquals(expectedType, actualType);
        assertEquals(expectedNonPrimitiveType, actualNonPrimitiveType);
        assertEquals(expectedName, actualName);
        assertEquals(expectedQualifiers, actualQualifiers);
        assertEquals(expectedParameterSupplierAnnotation, actualParameterSupplierAnnotation);
    }
}