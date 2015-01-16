package org.junit.lambda.proposal02.examples;

import org.junit.lambda.proposal02.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.lambda.proposal02.LambdaAssert.*;

/*
    The JUnit4 way of defining tests should still work.
    Moreover, we could provide a mechanism for names with any characters.
 */
public class BasicTest {

    @Test
    public void aJunit4CompatibleTest() {
        assertEquals(1, 1);
    }

    @Test("test name can be any string")
    public void methodNameDoesNotMatterHere() {
        assertEquals(1, 1);
    }

    /**
     * Using lambdas allows deferring the execution of "expensive" code
     */
    @Test
    public void testWithLambdaAssertion() {
        assertTrue(() -> true, () -> "failure message");
    }
}
