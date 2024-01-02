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

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.4
 * @see NullSource
 */
class NullArgumentsProvider implements ArgumentsProvider {

	private static final Arguments nullArguments = arguments(new Object[] { null });

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		Preconditions.condition(testMethod.getParameterCount() > 0, () -> String.format(
			"@NullSource cannot provide a null argument to method [%s]: the method does not declare any formal parameters.",
			testMethod.toGenericString()));

		return Stream.of(nullArguments);
	}

}
