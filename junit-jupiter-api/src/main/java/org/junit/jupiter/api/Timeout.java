/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.apiguardian.api.API;

/**
 * {@code @Timeout} is used to define a timeout for a method or all testable
 * methods within one class and its {@link Nested @Nested} classes.
 *
 * <p>This annotation may also be used on lifecycle methods annotated with
 * {@link BeforeAll @BeforeAll}, {@link BeforeEach @BeforeEach},
 * {@link AfterEach @AfterEach}, or {@link AfterAll @AfterAll}.
 *
 * <p>Applying this annotation to a test class has the same effect as applying
 * it to all testable methods, i.e. all methods annotated or meta-annotated with
 * {@link Test @Test}, {@link TestFactory @TestFactory}, or
 * {@link TestTemplate @TestTemplate}, but not to its lifecycle methods.
 *
 * <h3>Default Timeouts</h3>
 *
 * <p>If this annotation is not present, no timeout will be used unless a
 * default timeout is defined via one of the following configuration parameters:
 *
 * <dl>
 *     <dt>{@code junit.jupiter.execution.timeout.default}</dt>
 *     <dd>Default timeout for all testable and lifecycle methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.testable.method.default}</dt>
 *     <dd>Default timeout for all testable methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.test.method.default}</dt>
 *     <dd>Default timeout for {@link Test @Test} methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.testtemplate.method.default}</dt>
 *     <dd>Default timeout for {@link TestTemplate @TestTemplate} methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.testfactory.method.default}</dt>
 *     <dd>Default timeout for {@link TestFactory @TestFactory} methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.lifecycle.method.default}</dt>
 *     <dd>Default timeout for all lifecycle methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.beforeall.method.default}</dt>
 *     <dd>Default timeout for {@link BeforeAll @BeforeAll} methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.beforeeach.method.default}</dt>
 *     <dd>Default timeout for {@link BeforeEach @BeforeEach} methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.aftereach.method.default}</dt>
 *     <dd>Default timeout for {@link AfterEach @AfterEach} methods</dd>
 *     <dt>{@code junit.jupiter.execution.timeout.afterall.method.default}</dt>
 *     <dd>Default timeout for {@link AfterAll @AfterAll} methods</dd>
 * </dl>
 *
 * <p>More specific configuration parameters override less specific ones. For
 * example, {@code junit.jupiter.execution.timeout.test.method.default}
 * overrides {@code junit.jupiter.execution.timeout.testable.method.default}
 * which overrides {@code junit.jupiter.execution.timeout.default}.
 *
 * <p>Values must be in the following, case-insensitive format:
 * {@code <number> [ns|μs|ms|s|m|h|d]}. The space between the number and the
 * unit may be omitted. Specifying no unit is equivalent to using seconds.
 *
 * <table class="plain">
 * <tr><th> Value         </th><th> Equivalent annotation                             </th></tr>
 * <tr><td> {@code 42}    </td><td> {@code @Timeout(42)}                              </td></tr>
 * <tr><td> {@code 42 ns} </td><td> {@code @Timeout(value = 42, unit = NANOSECONDS)}  </td></tr>
 * <tr><td> {@code 42 μs} </td><td> {@code @Timeout(value = 42, unit = MICROSECONDS)} </td></tr>
 * <tr><td> {@code 42 ms} </td><td> {@code @Timeout(value = 42, unit = MILLISECONDS)} </td></tr>
 * <tr><td> {@code 42 s}  </td><td> {@code @Timeout(value = 42, unit = SECONDS)}      </td></tr>
 * <tr><td> {@code 42 m}  </td><td> {@code @Timeout(value = 42, unit = MINUTES)}      </td></tr>
 * <tr><td> {@code 42 h}  </td><td> {@code @Timeout(value = 42, unit = HOURS)}        </td></tr>
 * <tr><td> {@code 42 d}  </td><td> {@code @Timeout(value = 42, unit = DAYS)}         </td></tr>
 * </table>
 *
 * @since 5.5
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(status = EXPERIMENTAL, since = "5.5")
public @interface Timeout {

	/**
	 * The duration of this timeout.
	 *
	 * @return timeout duration; must be a positive number
	 */
	long value();

	/**
	 * The time unit of this timeout.
	 *
	 * @return time unit
	 * @see TimeUnit
	 */
	TimeUnit unit() default TimeUnit.SECONDS;

}
