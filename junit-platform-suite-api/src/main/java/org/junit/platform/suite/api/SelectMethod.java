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
 * @see org.junit.platform.engine.discovery.DiscoverySelectors#selectMethod(Class, String, String methodParameterTypes)
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
	 * The parameter types of the method to select.
	 *
	 * <p>This is typically a comma-separated list of atomic types, fully
	 * qualified class names, or array types; however, the exact syntax
	 * depends on the underlying test engine.
	 *
	 * <p>If the method takes no parameters, this attribute must be an
	 * empty string.
	 *
	 * <p>Array parameter types may be specified using either the JVM's internal
	 * String representation (e.g., {@code [[I} for {@code int[][]},
	 * {@code [Ljava.lang.String;} for {@code java.lang.String[]}, etc.) or
	 * <em>source code syntax</em> (e.g., {@code int[][]}, {@code java.lang.String[]},
	 * etc.).
	 * <p>
	 * <table class="plain">
	 * <caption>Examples</caption>
	 * <tr><th>Method</th><th>Parameter types list</th></tr>
	 * <tr><td>{@code java.lang.String.chars()}</td><td>The empty string</td></tr>
	 * <tr><td>{@code java.lang.String.equalsIgnoreCase(String)}</td><td>{@code java.lang.String}</td></tr>
	 * <tr><td>{@code java.lang.String.substring(int, int)}</td><td>{@code int, int}</td></tr>
	 * <tr><td>{@code example.Calc.avg(int[])}</td><td>{@code [I}</td></tr>
	 * <tr><td>{@code example.Calc.avg(int[])}</td><td>{@code int[]}</td></tr>
	 * <tr><td>{@code example.Matrix.multiply(double[][])}</td><td>{@code [[D}</td></tr>
	 * <tr><td>{@code example.Matrix.multiply(double[][])}</td><td>{@code double[][]}</td></tr>
	 * <tr><td>{@code example.Service.process(String[])}</td><td>{@code [Ljava.lang.String;}</td></tr>
	 * <tr><td>{@code example.Service.process(String[])}</td><td>{@code java.lang.String[]}</td></tr>
	 * <tr><td>{@code example.Service.process(String[][])}</td><td>{@code [[Ljava.lang.String;}</td></tr>
	 * <tr><td>{@code example.Service.process(String[][])}</td><td>{@code java.lang.String[][]}</td></tr>
	 * </table>
	 */
	String parameters() default "";

}
