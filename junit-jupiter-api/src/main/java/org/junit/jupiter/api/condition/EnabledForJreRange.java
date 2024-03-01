/*
 * Copyright 2015-2024 the original author or authors.
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
 * {@code @EnabledForJreRange} is used to signal that the annotated test class or
 * test method is only <em>enabled</em> for a specific range of Java Runtime
 * Environment (JRE) versions from {@link #min} to {@link #max}.
 *
 * <p>When applied at the class level, all test methods within that class will
 * be enabled on the same specified JRE versions.
 *
 * <p>This annotation is not {@link java.lang.annotation.Inherited @Inherited}.
 * Consequently, if you wish to apply the same semantics to a subclass, this
 * annotation must be redeclared on the subclass.
 *
 * <p>If a test method is disabled via this annotation, that prevents execution
 * of the test method and method-level lifecycle callbacks such as
 * {@code @BeforeEach} methods, {@code @AfterEach} methods, and corresponding
 * extension APIs. However, that does not prevent the test class from being
 * instantiated, and it does not prevent the execution of class-level lifecycle
 * callbacks such as {@code @BeforeAll} methods, {@code @AfterAll} methods, and
 * corresponding extension APIs.
 *
 * <p>This annotation may be used as a meta-annotation in order to create a
 * custom <em>composed annotation</em> that inherits the semantics of this
 * annotation.
 *
 * <h2>Warning</h2>
 *
 * <p>This annotation can only be declared once on an
 * {@link java.lang.reflect.AnnotatedElement AnnotatedElement} (i.e., test
 * interface, test class, or test method). If this annotation is directly
 * present, indirectly present, or meta-present multiple times on a given
 * element, only the first such annotation discovered by JUnit will be used;
 * any additional declarations will be silently ignored. Note, however, that
 * this annotation may be used in conjunction with other {@code @Enabled*} or
 * {@code @Disabled*} annotations in this package.
 *
 * @since 5.6
 * @see JRE
 * @see org.junit.jupiter.api.condition.EnabledIf
 * @see org.junit.jupiter.api.condition.DisabledIf
 * @see org.junit.jupiter.api.condition.EnabledOnOs
 * @see org.junit.jupiter.api.condition.DisabledOnOs
 * @see org.junit.jupiter.api.condition.EnabledOnJre
 * @see org.junit.jupiter.api.condition.DisabledOnJre
 * @see org.junit.jupiter.api.condition.DisabledForJreRange
 * @see org.junit.jupiter.api.condition.EnabledInNativeImage
 * @see org.junit.jupiter.api.condition.DisabledInNativeImage
 * @see org.junit.jupiter.api.condition.EnabledIfSystemProperty
 * @see org.junit.jupiter.api.condition.DisabledIfSystemProperty
 * @see org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
 * @see org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
 * @see org.junit.jupiter.api.Disabled
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(EnabledForJreRangeCondition.class)
@API(status = STABLE, since = "5.6")
@SuppressWarnings("exports")
public @interface EnabledForJreRange {

	/**
	 * Java Runtime Environment version which should be used as the lower boundary
	 * for the version range that determines if the annotated class or method
	 * should be enabled.
	 *
	 * <p>Defaults to {@link JRE#JAVA_8 JAVA_8}, as this is the lowest
	 * supported JRE version.
	 *
	 * @see JRE
	 */
	JRE min() default JRE.JAVA_8;

	/**
	 * Java Runtime Environment version which should be used as the upper boundary
	 * for the version range that determines if the annotated class or method
	 * should be enabled.
	 *
	 * <p>Defaults to {@link JRE#OTHER OTHER}, as this will always be the highest
	 * possible version.
	 *
	 * @see JRE
	 */
	JRE max() default JRE.OTHER;

	/**
	 * Custom reason to provide if the test or container is disabled.
	 *
	 * <p>If a custom reason is supplied, it will be combined with the default
	 * reason for this annotation. If a custom reason is not supplied, the default
	 * reason will be used.
	 *
	 * @since 5.7
	 */
	@API(status = STABLE, since = "5.7")
	String disabledReason() default "";

}
