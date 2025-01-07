/*
 * Copyright 2015-2025 the original author or authors.
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
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithFourConstants.BAR;
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithFourConstants.BAZ;
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithFourConstants.FOO;
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithFourConstants.QUX;
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
		var arguments = provideArguments(EnumWithFourConstants.class);

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR }, new Object[] { BAZ },
			new Object[] { QUX });
	}

	@Test
	void provideSingleEnumConstant() {
		var arguments = provideArguments(EnumWithFourConstants.class, "FOO");

		assertThat(arguments).containsExactly(new Object[] { FOO });
	}

	@Test
	void provideAllEnumConstantsWithNamingAll() {
		var arguments = provideArguments(EnumWithFourConstants.class, "FOO", "BAR");

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR });
	}

	@Test
	void duplicateConstantNameIsDetected() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithFourConstants.class, "FOO", "BAR", "FOO").findAny());
		assertThat(exception).hasMessageContaining("Duplicate enum constant name(s) found");
	}

	@Test
	void invalidConstantNameIsDetected() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithFourConstants.class, "FO0", "B4R").findAny());
		assertThat(exception).hasMessageContaining("Invalid enum constant name(s) in");
	}

	@Test
	void invalidPatternIsDetected() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithFourConstants.class, Mode.MATCH_ALL, "(", ")").findAny());
		assertThat(exception).hasMessageContaining("Pattern compilation failed");
	}

	@Test
	void providesEnumConstantsBasedOnTestMethod() throws Exception {
		when(extensionContext.getRequiredTestMethod()).thenReturn(
			TestCase.class.getDeclaredMethod("methodWithCorrectParameter", EnumWithFourConstants.class));

		var arguments = provideArguments(NullEnum.class);

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR }, new Object[] { BAZ },
			new Object[] { QUX });
	}

	@Test
	void incorrectParameterTypeIsDetected() throws Exception {
		when(extensionContext.getRequiredTestMethod()).thenReturn(
			TestCase.class.getDeclaredMethod("methodWithIncorrectParameter", Object.class));

		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(NullEnum.class).findAny());
		assertThat(exception).hasMessageStartingWith("First parameter must reference an Enum type");
	}

	@Test
	void methodsWithoutParametersAreDetected() throws Exception {
		when(extensionContext.getRequiredTestMethod()).thenReturn(
			TestCase.class.getDeclaredMethod("methodWithoutParameters"));

		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(NullEnum.class).findAny());
		assertThat(exception).hasMessageStartingWith("Test method must declare at least one parameter");
	}

	@Test
	void providesEnumConstantsStartingFromBar() {
		var arguments = provideArguments(EnumWithFourConstants.class, "BAR", "", Mode.INCLUDE);

		assertThat(arguments).containsExactly(new Object[] { BAR }, new Object[] { BAZ }, new Object[] { QUX });
	}

	@Test
	void providesEnumConstantsEndingAtBaz() {
		var arguments = provideArguments(EnumWithFourConstants.class, "", "BAZ", Mode.INCLUDE);

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR }, new Object[] { BAZ });
	}

	@Test
	void providesEnumConstantsFromBarToBaz() {
		var arguments = provideArguments(EnumWithFourConstants.class, "BAR", "BAZ", Mode.INCLUDE);

		assertThat(arguments).containsExactly(new Object[] { BAR }, new Object[] { BAZ });
	}

	@Test
	void providesEnumConstantsFromFooToBazWhileExcludingBar() {
		var arguments = provideArguments(EnumWithFourConstants.class, "FOO", "BAZ", Mode.EXCLUDE, "BAR");

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAZ });
	}

	@Test
	void providesNoEnumConstant() {
		var arguments = provideArguments(EnumWithNoConstant.class);

		assertThat(arguments).isEmpty();
	}

	@Test
	void invalidConstantNameIsDetectedInRange() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithFourConstants.class, "FOO", "BAZ", Mode.EXCLUDE, "QUX").findAny());
		assertThat(exception).hasMessageContaining("Invalid enum constant name(s) in");
	}

	@Test
	void invalidStartingRangeIsDetected() {
		Exception exception = assertThrows(IllegalArgumentException.class,
			() -> provideArguments(EnumWithFourConstants.class, "B4R", "", Mode.INCLUDE).findAny());
		assertThat(exception).hasMessageContaining("No enum constant");
	}

	@Test
	void invalidEndingRangeIsDetected() {
		Exception exception = assertThrows(IllegalArgumentException.class,
			() -> provideArguments(EnumWithFourConstants.class, "", "B4R", Mode.INCLUDE).findAny());
		assertThat(exception).hasMessageContaining("No enum constant");
	}

	@Test
	void invalidRangeOrderIsDetected() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithFourConstants.class, "BAR", "FOO", Mode.INCLUDE).findAny());
		assertThat(exception).hasMessageContaining("Invalid enum range");
	}

	@Test
	void invalidRangeIsDetectedWhenEnumWithNoConstantIsProvided() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(EnumWithNoConstant.class, "BAR", "FOO", Mode.INCLUDE).findAny());
		assertThat(exception).hasMessageContaining("No enum constant");
	}

	static class TestCase {
		void methodWithCorrectParameter(EnumWithFourConstants parameter) {
		}

		void methodWithIncorrectParameter(Object parameter) {
		}

		void methodWithoutParameters() {
		}
	}

	enum EnumWithFourConstants {
		FOO, BAR, BAZ, QUX
	}

	enum EnumWithNoConstant {
	}

	private <E extends Enum<E>> Stream<Object[]> provideArguments(Class<E> enumClass, String... names) {
		return provideArguments(enumClass, Mode.INCLUDE, names);
	}

	private <E extends Enum<E>> Stream<Object[]> provideArguments(Class<E> enumClass, Mode mode, String... names) {
		return provideArguments(enumClass, "", "", mode, names);
	}

	private <E extends Enum<E>> Stream<Object[]> provideArguments(Class<E> enumClass, String from, String to, Mode mode,
			String... names) {
		var annotation = mock(EnumSource.class);
		when(annotation.value()).thenAnswer(invocation -> enumClass);
		when(annotation.from()).thenAnswer(invocation -> from);
		when(annotation.to()).thenAnswer(invocation -> to);
		when(annotation.mode()).thenAnswer(invocation -> mode);
		when(annotation.names()).thenAnswer(invocation -> names);
		when(annotation.toString()).thenReturn(
			String.format("@EnumSource(value=%s.class, from=%s, to=%s, mode=%s, names=%s)", enumClass.getSimpleName(),
				from, to, mode, Arrays.toString(names)));

		var provider = new EnumArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(extensionContext).map(Arguments::get);
	}

}
