package org.junit.lambda.proposal02.examples;

import org.junit.Assert;
import org.junit.lambda.proposal02.Test;
import org.junit.lambda.proposal02.Testable;

/**
 * We might also allow for even simpler test cases.
 */
@Test("a simple test")
public class SimpleTest implements Testable {

    /**
     * Implementing before is optional
     */
    @Override
    public void before() {
        System.out.println("Before a simple test");
    }

    @Override
    public void run() throws Exception {
        Assert.assertEquals(1, 1);
    }

    /**
     * Implementing after is optional
     */
    @Override
    public void after() {
        System.out.println("After a simple test");
    }

}
