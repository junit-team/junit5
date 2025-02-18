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

import static org.junit.jupiter.params.provider.Arguments.arguments;

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
	public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
		Preconditions.condition(parameters.getCount() > 0,
			() -> String.format("@NullSource cannot provide a null argument to %s: no formal parameters declared.",
				parameters.getSourceElementDescription()));

		return Stream.of(nullArguments);
	}

}
