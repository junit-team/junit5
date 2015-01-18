package org.junit.lambda.proposal02.examples;

import org.junit.lambda.proposal02.DataProvider;
import org.junit.lambda.proposal02.Parameters;
import org.junit.lambda.proposal02.Test;

import static org.junit.Assert.assertEquals;


/**
 * Under the hood @Parameters could be implemented by decorators
 */
@Parameters(MyData.class)
public class ParameterizedTests {

    @Test
    public void testAddition(MyData data) {
        assertEquals(data.plus, data.x + data.y);
    }

    /**
     * Test name can be filled in with pieces from parameters
     */
    @Test("Multiply $data.x with $data.y")
    public void testMultiplication(MyData data) {
        assertEquals(data.times, data.x * data.y);
    }
}


class MyData implements DataProvider<MyData> {

    public final int x;
    public final int y;
    public final long plus;
    public final long times;

    public MyData(int x, int y, int plus, int times) {
        this.x = x;
        this.y = y;
        this.plus = plus;
        this.times = times;
    }

    /**
     * For creation by Parameters
     */
    public MyData() {
        this(0,0,0,0);
    }

    @Override
    public MyData[] data() {
        return new MyData[] {
                new MyData(1,2,3,2),
                new MyData(3,5,8,15)
        };
    }
}
