/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.platform.commons.util.CollectionUtils.getFirstElement;

import java.lang.reflect.Constructor;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.12
 */
class ParameterizedTestSpiInstantiator {

	static <T> T instantiate(Class<T> spiInterface, Class<? extends T> implementationClass,
			ExtensionContext extensionContext) {
		return extensionContext.getExecutableInvoker() //
				.invoke(findConstructor(spiInterface, implementationClass));
	}

	/**
	 * Find the "best" constructor for the supplied class.
	 *
	 * <p>For backward compatibility, it first checks for a default constructor
	 * which takes precedence over any other constructor. If no default
	 * constructor is found, it checks for a single constructor and returns it.
	 */
	private static <T, V extends T> Constructor<? extends V> findConstructor(Class<T> spiInterface,
			Class<V> implementationClass) {

		Preconditions.condition(!ReflectionUtils.isInnerClass(implementationClass),
			() -> String.format("The %s [%s] must be either a top-level class or a static nested class",
				spiInterface.getSimpleName(), implementationClass.getName()));

		return findDefaultConstructor(implementationClass) //
				.orElseGet(() -> findSingleConstructor(spiInterface, implementationClass));
	}

	@SuppressWarnings("unchecked")
	private static <T> Optional<Constructor<T>> findDefaultConstructor(Class<T> clazz) {
		return getFirstElement(ReflectionUtils.findConstructors(clazz, it -> it.getParameterCount() == 0)) //
				.map(it -> (Constructor<T>) it);
	}

	private static <T, V extends T> Constructor<V> findSingleConstructor(Class<T> spiInterface,
			Class<V> implementationClass) {

		try {
			return ReflectionUtils.getDeclaredConstructor(implementationClass);
		}
		catch (PreconditionViolationException ex) {
			String message = String.format(
				"Failed to find constructor for %s [%s]. "
						+ "Please ensure that a no-argument or a single constructor exists.",
				spiInterface.getSimpleName(), implementationClass.getName());
			throw new JUnitException(message);
		}
	}
}
