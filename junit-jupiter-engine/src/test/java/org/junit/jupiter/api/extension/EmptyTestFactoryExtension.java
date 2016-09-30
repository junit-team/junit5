package org.junit.jupiter.api.extension;

import org.junit.jupiter.api.DynamicTest;

import java.util.stream.Stream;

public class EmptyTestFactoryExtension implements TestFactoryExtension {

	@Override
	public Stream<DynamicTest> createForContainer(
			ContainerExtensionContext context) {
		return Stream.empty();
	}

	@Override
	public Stream<DynamicTest> createForMethod(TestExtensionContext context) {
		return Stream.empty();
	}
}
