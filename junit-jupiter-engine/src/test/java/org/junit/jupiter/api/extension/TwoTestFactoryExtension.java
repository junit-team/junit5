package org.junit.jupiter.api.extension;

import org.junit.jupiter.api.DynamicTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class TwoTestFactoryExtension implements TestFactoryExtension {

	private static final List<DynamicTest> TWO_TESTS = Arrays.asList(
			dynamicTest("succeedingTest", () -> assertTrue(true, "succeeding")),
			dynamicTest("failingTest", () -> fail("failing")));

	@Override
	public Stream<DynamicTest> createForContainer(ContainerExtensionContext context) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public Stream<DynamicTest> createForMethod(TestExtensionContext context) {
		return TWO_TESTS.stream();
	}

}
