package org.junit.lambda.proposal02.examples;

import org.junit.Before;
import org.junit.lambda.proposal02.Context;
import org.junit.lambda.proposal02.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests can be nested in hierarchies using nested context classes.
 * The top level @Context annotation is optional to provide JUnit4 compatibility
 * @Before, @After, @BeforeClass, @AfterClass work for every context
 */
@Context("Main Context")
public class NestedTestContexts {

    Object it;

    @Before
    public void init() {
        it = new Object() {
            @Override
            public String toString() {
                return "object under test";
            }
        };
    }

    @Context("New object")
    class SubContext1 {

        @Test
        public void testToString() {
            assertEquals("object under test", it.toString());
        }

        @Test
        public void testHashCode() {
            assertTrue(it.hashCode() != 0);
        }
    }

    @Context("Equality")
    class SubContext2 {

        Object other;

        @Before
        public void init() {
            other = new Object() {
                @Override
                public String toString() {
                    return "other object";
                }
            };
        }

        @Test
        public void differentObjectsAreDifferent() {
            assertNotEquals(it, other);
        }
    }

}
