package org.junit.lambda.proposal02.examples;

import junit.framework.TestResult;
import org.junit.lambda.proposal02.Decorate;
import org.junit.lambda.proposal02.Test;
import org.junit.lambda.proposal02.TestCase;
import org.junit.lambda.proposal02.TestDecorator;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@Decorate(MyData.class)
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


class MyData extends Parameters<MyData> {

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
     * For creation by decorator
     */
    public MyData() {
        this(0,0,0,0);
    }

    @Override
    MyData[] data() {
        return new MyData[] {
                new MyData(1,2,3,2),
                new MyData(3,5,8,15)
        };
    }
}

abstract class Parameters<T> implements TestDecorator {
    abstract T[] data();

    private Stream<T> stream() {
        return Arrays.stream(data());
    }

    @Override
    public TestResult runTest(TestCase testCase) throws Exception {
        //todo:
        // wrap all tests in additional contexts
        // create tests case instances for all data supplied in stream
        // run all tests cases and collect results in new TestResult
        return null;
    }
}