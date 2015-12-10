/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * Super interface for all extension points.
 *
 * <p>{@code ExtensionPoint} can be registered via {@link ExtendWith @ExtendWith}.
 *
 * @since 5.0
 * @see InstancePostProcessor
 * @see MethodParameterResolver
 * @see BeforeEachExtensionPoint
 * @see AfterEachExtensionPoint
 * @see BeforeAllExtensionPoint
 * @see AfterAllExtensionPoint
 * @see ShouldTestBeExecutedCondition
 * @see ShouldContainerBeExecutedCondition
 */

public interface ExtensionPoint extends TestExtension {

	/**
	 * Specifies the order in which a registered extension point is applied with regard to all other registered
	 * extension points of the same type. The order can be specified when registering an extension point. Possible
	 * values:
	 * <ul>
	 * <li>{@code FIRST}: Apply first in order. Only a single extension is allowed to use this value, otherwise
	 * {@link ExtensionConfigurationException} should be thrown.</li>
	 * <li>{@code BEFORE}: Apply after {@code FIRST}, but before {@code DEFAULT}, {@code AFTER} and {@code LAST}. Many
	 * extensions can have this value - the order among those is undefined.</li>
	 * <li>{@code DEFAULT}: ...</li>
	 * <li>{@code AFTER}: ...</li>
	 * <li>{@code LAST}: ...</li>
	 * </ul>
	 */
	enum Position {
		FIRST, BEFORE, DEFAULT, AFTER, LAST
	}

	default boolean sortBackwards() {
		return false;
	}
}
