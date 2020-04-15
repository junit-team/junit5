/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DisabledOnOs} is used to signal that the annotated test class or
 * test method is <em>disabled</em> on one or more specified
 * {@linkplain #value operating systems}.
 *
 * <p>When applied at the class level, all test methods within that class
 * will be disabled on the same specified operating systems.
 *
 * <p>If a test method is disabled via this annotation, that does not prevent
 * the test class from being instantiated. Rather, it prevents the execution of
 * the test method and method-level lifecycle callbacks such as {@code @BeforeEach}
 * methods, {@code @AfterEach} methods, and corresponding extension APIs.
 *
 * <p>This annotation may be used as a meta-annotation in order to create a
 * custom <em>composed annotation</em> that inherits the semantics of this
 * annotation.
 *
 * <h4>Warning</h4>
 *
 * <p>As of JUnit Jupiter 5.1, this annotation can only be declared once on an
 * {@link java.lang.reflect.AnnotatedElement AnnotatedElement} (i.e., test
 * interface, test class, or test method). If this annotation is directly
 * present, indirectly present, or meta-present multiple times on a given
 * element, only the first such annotation discovered by JUnit will be used;
 * any additional declarations will be silently ignored. Note, however, that
 * this annotation may be used in conjunction with other {@code @Enabled*} or
 * {@code @Disabled*} annotations in this package.
 *
 * @since 5.1
 * @see OS
 * @see org.junit.jupiter.api.condition.EnabledOnOs
 * @see org.junit.jupiter.api.condition.EnabledOnJre
 * @see org.junit.jupiter.api.condition.DisabledOnJre
 * @see org.junit.jupiter.api.condition.EnabledForJreRange
 * @see org.junit.jupiter.api.condition.DisabledForJreRange
 * @see org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
 * @see org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
 * @see org.junit.jupiter.api.condition.EnabledIfSystemProperty
 * @see org.junit.jupiter.api.condition.DisabledIfSystemProperty
 * @see org.junit.jupiter.api.condition.EnabledIf
 * @see org.junit.jupiter.api.condition.DisabledIf
 * @see org.junit.jupiter.api.Disabled
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(DisabledOnOsCondition.class)
@API(status = STABLE, since = "5.1")
public @interface DisabledOnOs {

	/**
	 * Operating systems on which the annotated class or method should be
	 * disabled.
	 *
	 * @see OS
	 */
	OS[] value();

	/**
	 * Reason to provide if the test of container ends up being disabled.
	 */
	String disabledReason() default "";

}
