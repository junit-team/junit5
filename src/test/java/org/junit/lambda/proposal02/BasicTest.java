package org.junit.lambda.proposal02;

import static org.junit.Assert.assertEquals;

public class BasicTest {

    @Test
    public void aJunit4CompatibleTest() {
        assertEquals(1, 1);
    }


    @Test("test name can be any string")
    public void methodNameDoesNotMatterHere() {
        assertEquals(1, 1);
    }


}
