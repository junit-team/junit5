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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class EnumArgumentsProvider extends AnnotationBasedArgumentsProvider<EnumSource> {

	@Override
	protected Stream<? extends Arguments> provideArguments(ExtensionContext context, EnumSource enumSource) {
		Set<? extends Enum<?>> constants = getEnumConstants(context, enumSource);
		EnumSource.Mode mode = enumSource.mode();
		String[] declaredConstantNames = enumSource.names();
		if (declaredConstantNames.length > 0) {
			Set<String> uniqueNames = stream(declaredConstantNames).collect(toSet());
			Preconditions.condition(uniqueNames.size() == declaredConstantNames.length,
				() -> "Duplicate enum constant name(s) found in " + enumSource);
			mode.validate(enumSource, constants, uniqueNames);
			constants.removeIf(constant -> !mode.select(constant, uniqueNames));
		}
		return constants.stream().map(Arguments::of);
	}

	private <E extends Enum<E>> Set<? extends E> getEnumConstants(ExtensionContext context, EnumSource enumSource) {
		Class<E> enumClass = determineEnumClass(context, enumSource);
		return EnumSet.allOf(enumClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <E extends Enum<E>> Class<E> determineEnumClass(ExtensionContext context, EnumSource enumSource) {
		Class enumClass = enumSource.value();
		if (enumClass.equals(NullEnum.class)) {
			Method method = context.getRequiredTestMethod();
			Class<?>[] parameterTypes = method.getParameterTypes();
			Preconditions.condition(parameterTypes.length > 0,
				() -> "Test method must declare at least one parameter: " + method.toGenericString());
			Preconditions.condition(Enum.class.isAssignableFrom(parameterTypes[0]),
				() -> "First parameter must reference an Enum type (alternatively, use the annotation's 'value' attribute to specify the type explicitly): "
						+ method.toGenericString());
			enumClass = parameterTypes[0];
		}
		return enumClass;
	}

}
