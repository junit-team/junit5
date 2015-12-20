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
 * @see ContainerExecutionCondition
 * @see TestExecutionCondition
 * @see InstancePostProcessor
 * @see MethodParameterResolver
 * @see BeforeEachExtensionPoint
 * @see AfterEachExtensionPoint
 * @see BeforeAllExtensionPoint
 * @see AfterAllExtensionPoint
 */

public interface ExtensionPoint extends TestExtension {

	/**
	 * Specifies the order in which a registered extension point is applied with regard to all other registered
	 * extension points of the same type. The order can be specified when registering an extension point. Possible
	 * values:
	 * <ul>
	 * <li>{@code OUTERMOST}: Apply first in order. Only a single extension is allowed to use this value, otherwise
	 * {@link ExtensionConfigurationException} should be thrown.</li>
	 * <li>{@code OUTSIDE_DEFAULT}: Apply after {@code OUTERMOST}, but before {@code DEFAULT}, {@code INSIDE_DEFAULT} and {@code INNERMOST}. Many
	 * extensions can have this value - the order among those is undefined.</li>
	 * <li>{@code DEFAULT}: ...</li>
	 * <li>{@code INSIDE_DEFAULT}: ...</li>
	 * <li>{@code INNERMOST}: ...</li>
	 * </ul>
	 */
	enum Position {
		OUTERMOST(1), OUTSIDE_DEFAULT(2), DEFAULT(3), INSIDE_DEFAULT(4), INNERMOST(5);

		public final int sortingOrder;

		Position(int sortingOrder) {
			this.sortingOrder = sortingOrder;
		}
	}

}
