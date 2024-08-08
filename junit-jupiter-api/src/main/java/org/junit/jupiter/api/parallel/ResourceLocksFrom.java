/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @ResourceLocksFrom} is used to add shared resources
 * to an annotated test class and / or its test methods in runtime.
 *
 * <p>The {@link #value} should represent
 * one or more classes implementing {@link ResourceLocksProvider}.
 *
 * <p>This annotation (in conjunction with {@link ResourceLocksProvider})
 * serves the same purpose as {@link ResourceLock} annotation
 * but for certain cases may be a more flexible and less verbose alternative
 * since it allows to add shared resources programmatically.
 *
 * <p>Given this annotation is {@linkplain Inherited inherited},
 * if it's applied to a parent test class,
 * then {@link ResourceLocksProvider} methods will be called also
 * for child test classes and their test methods.
 *
 * @apiNote If both {@code @ResourceLock} and {@code @ResourceLocksFrom}
 * are used then shared resources from them are summed up.
 * It means that if resource 'A' is declared via {@code @ResourceLock}
 * and resource 'B' added via {@code @ResourceLocksFrom}
 * then a test class or test method will have both resources: 'A' and 'B'.
 *
 * @since 5.12
 * @see Isolated
 * @see Resources
 * @see ResourceAccessMode
 * @see ResourceLock
 * @see ResourceLocksProvider
 * @see ResourceLocksProvider.Lock
 */
@API(status = EXPERIMENTAL, since = "5.12")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ResourceLocksFrom {

	/**
	 * An array of one or more classes implementing {@link ResourceLocksProvider}.
	 */
	Class<? extends ResourceLocksProvider>[] value();

}
