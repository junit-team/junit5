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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * specifies a method to select when running a test suite on the JUnit Platform,
 * This annotation can be repeated to add multiple methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Repeatable(SelectMethods.class)
@API(status = EXPERIMENTAL, since = "1.10")
public @interface SelectMethod {

	/**
	 * the fully qualified name of the class in which the method is declared,
	 * or a subclass thereof; never null or blank
	 */
	Class<?> methodClass();

	/**
	 * the name of the method to select; never null or blank
	 */
	String methodName();

	/**
	 * The method parameter types as an array of Class objects;
	 * never null, not to be entered if the function has no input parameters.
	 */
	Class<?>[] methodParameterTypes() default {};

}
