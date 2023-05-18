/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @SelectMethod} is a {@linkplain Repeatable repeatable} annotation that
 * specifies a method to <em>select</em> when running a test suite on the JUnit
 * Platform.
 *
 * @since 1.10
 * @see Suite
 * @see org.junit.platform.runner.JUnitPlatform
 * @see org.junit.platform.engine.discovery.DiscoverySelectors#selectMethod(Class, String)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = EXPERIMENTAL, since = "1.10")
@Repeatable(SelectMethods.class)
public @interface SelectMethod {

	/**
	 * The class that declares the method to select.
	 */
	Class<?> clazz();

	/**
	 * The name of the method to select.
	 */
	String name();

	/**
	 * The parameter types of the method to select in the format "type1,type2,type3".
	 * If the method takes no parameters, this attribute must be an empty string.
	 */
	String parameters() default "";

}
