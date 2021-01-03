/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * {@code @Configurations} is a container for one or more
 * {@link Configuration @Configuration} declarations.
 *
 * <p>Note, however, that use of the {@code @Configurations} container is
 * completely optional since {@code @Configuration} is a
 * {@linkplain java.lang.annotation.Repeatable repeatable} annotation.
 *
 * <h4>JUnit 5 Suite Support</h4>
 * <p>Test suites can be run on the JUnit Platform in a JUnit 5 environment via
 * the {@code junit-platform-suite} engine.
 *
 * @since 1.8
 * @see Configuration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = Status.EXPERIMENTAL, since = "1.8")
public @interface Configurations {

	/**
	 * An array of one or more {@link Configuration Configurations}.
	 */
	Configuration[] value();
}
