/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithTwoConstants.BAR;
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithTwoConstants.FOO;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.0
 */
class EnumArgumentsProviderTests {

	private ExtensionContext extensionContext = mock();

	@Test
	void providesAllEnumConstants() {
		var arguments = provideArguments(EnumWithTwoConstants.class);

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR });
	}

	@Test
	void provideSingleEnumConstant() {
		var arguments = provideArguments(EnumWithTwoConstants.class, "FOO");

		assertThat(arguments).containsExactly(new Object[] { FOO });
	}

	@Test
	void provideAllEnumConstantsWithNamingAll() {
		var arguments = provideArguments(EnumWithTwoConstants.class, "FOO", "BAR");

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR });
	}

	@Test
	void duplicateConstantNameIsDetected() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithTwoConstants.class, "FOO", "BAR", "FOO"));
		assertThat(exception).hasMessageContaining("Duplicate enum constant name(s) found");
	}

	@Test
	void invalidConstantNameIsDetected() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithTwoConstants.class, "FO0", "B4R"));
		assertThat(exception).hasMessageContaining("Invalid enum constant name(s) in");
	}

	@Test
	void invalidPatternIsDetected() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithTwoConstants.class, Mode.MATCH_ALL, "(", ")"));
		assertThat(exception).hasMessageContaining("Pattern compilation failed");
	}

	@Test
	void providesEnumConstantsBasedOnTestMethod() throws Exception {
		when(extensionContext.getRequiredTestMethod()).thenReturn(
			TestCase.class.getDeclaredMethod("methodWithCorrectParameter", EnumWithTwoConstants.class));

		var arguments = provideArguments(NullEnum.class);

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR });
	}

	@Test
	void incorrectParameterTypeIsDetected() throws Exception {
		when(extensionContext.getRequiredTestMethod()).thenReturn(
			TestCase.class.getDeclaredMethod("methodWithIncorrectParameter", Object.class));

		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(NullEnum.class));
		assertThat(exception).hasMessageStartingWith("First parameter must reference an Enum type");
	}

	@Test
	void methodsWithoutParametersAreDetected() throws Exception {
		when(extensionContext.getRequiredTestMethod()).thenReturn(
			TestCase.class.getDeclaredMethod("methodWithoutParameters"));

		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(NullEnum.class));
		assertThat(exception).hasMessageStartingWith("Test method must declare at least one parameter");
	}

	static class TestCase {
		void methodWithCorrectParameter(EnumWithTwoConstants parameter) {
		}

		void methodWithIncorrectParameter(Object parameter) {
		}

		void methodWithoutParameters() {
		}
	}

	enum EnumWithTwoConstants {
		FOO, BAR
	}

	private <E extends Enum<E>> Stream<Object[]> provideArguments(Class<E> enumClass, String... names) {
		return provideArguments(enumClass, Mode.INCLUDE, names);
	}

	private <E extends Enum<E>> Stream<Object[]> provideArguments(Class<E> enumClass, Mode mode, String... names) {
		var annotation = mock(EnumSource.class);
		when(annotation.value()).thenAnswer(invocation -> enumClass);
		when(annotation.mode()).thenAnswer(invocation -> mode);
		when(annotation.names()).thenAnswer(invocation -> names);
		when(annotation.toString()).thenReturn(String.format("@EnumSource(value=%s.class, mode=%s, names=%s)",
			enumClass.getSimpleName(), mode, Arrays.toString(names)));

		var provider = new EnumArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(extensionContext).map(Arguments::get);
	}

}
