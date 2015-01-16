package org.junit.lambda.proposal02.examples;

import org.junit.lambda.proposal02.Person;
import org.junit.lambda.proposal02.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.lambda.proposal02.LambdaAssert.*;

/*
    How can we make use of lambdas for better assertions?
 */
public class LambdaAssertionsTest {

    /**
     * Using lambdas allows deferring the execution of "expensive" code
     */
    @Test
    public void testWithLambdaAssertion() {
        assertTrue(() -> true, () -> "failure message");
    }

    @Test
    public void testExceptionType() {
        assertException(() -> {
            throw new RuntimeException();
        }, RuntimeException.class, () -> "expected RTE");
    }

    @Test
    public void testExceptionDetails() {
        assertException(
                () -> {
                    throw new RuntimeException("a message");
                },
                (ex) -> {
                    assertEquals(RuntimeException.class, ex.getClass());
                    assertEquals("a message", ex.getMessage());
                },
                () -> "expected RTE"
        );
    }

    /**
     * Sometimes we want to use more than one assert and get results of all.
     * assertAll will collect result of all into single test result
     */
    @Test
    public void testWithCombinedAsserts() {
        Person person = new Person("Johannes", "Link", "Germany");
        assertAll(
                () -> assertEquals("Johannes", person.getFirstName()),
                () -> assertEquals("Link", person.getFirstName()),
                () -> assertEquals("Germany", person.getFirstName())
        );
    }


}
