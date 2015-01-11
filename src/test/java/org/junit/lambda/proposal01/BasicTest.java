package org.junit.lambda.proposal01;

import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;

/**
 * Based on https://github.com/junit-team/junit/wiki/Quo-Vadis-JUnit
 */
public class BasicTest extends JUnitTest {{

	test("Test something", () -> {
		assertEquals(1, 1);
	});

	range(0, 10).forEach(i -> test("Test something with " + i, () -> {
		assertEquals(i, i);
	}));

}}
