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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for all test extensions.
 * <p>
 * {@code TestExtensions} can be registered via {@link ExtendWith @ExtendWith}.
 *
 * @since 5.0
 * @see MethodParameterResolver
 * @see ContainerLifecycleExtension
 * @see TestLifecycleExtension
 */
public interface TestExtension {

	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Inherited
	@interface DefaultOrder {
		OrderPosition value();
	}

	/**
	 * Specifies the order in which a lifecycle extension is applied with regard to all other registered lifecycle
	 * extensions. The order can be specified as the {@link DefaultOrder} of any {@link TestExtension} or being
	 * explicitly set when applying an extension to a test or test container with {@link ExtendWith} (order attribute).
	 * Possible values:
	 * <ul>
	 * <li>{@code DEFAULT}: Use the extension's default order; this only makes sense as default value for
	 * {@code ExtendWith.order}</li>
	 * <li>{@code MIDDLE}: Apply in the middle of all lifecycle extensions. This is the normal behaviour for extensions
	 * that don't have dependencies to other stuff.</li>
	 * <li>{@code OUTERMOST}: Apply first (beforeAll, beforeEach, postProcessTestInstance, executeTest) and last
	 * (afterAll, afterEach). Only a single extension is allowed to use this value, otherwise
	 * {@link ExtensionConfigurationException} should be thrown</li>
	 * <li>{@code INNERMOST}: Apply last (beforeAll, beforeEach, postProcessTestInstance, executeTest) and first
	 * (afterAll, afterEach). Only a single extension is allowed to use this value, otherwise
	 * {@link ExtensionConfigurationException} should be thrown</li>
	 * <li>{@code OUTSIDE}: Apply before (beforeAll, beforeEach, postProcessTestInstance, executeTest) and after
	 * (afterAll, afterEach) extensions with order {@code MIDDLE}. Many extensions can have this value - the order among
	 * those is undefined.</li>
	 * <li>{@code INSIDE}: Apply after (beforeAll, beforeEach, postProcessTestInstance, executeTest) and before
	 * (afterAll, afterEach) extensions with order {@code MIDDLE}. Many extensions can have this value - the order among
	 * those is undefined.</li>
	 * </ul>
	 */
	enum OrderPosition {
		DEFAULT, MIDDLE, OUTERMOST, INNERMOST, OUTSIDE, INSIDE
	}

}
