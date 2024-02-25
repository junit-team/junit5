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

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * {@code @ResourceLock} is used to declare that the annotated test class or test
 * method requires access to a shared resource identified by a key.
 *
 * <p>The resource key is specified via {@link #value}. In addition,
 * {@link #mode} allows you to specify whether the annotated test class or test
 * method requires {@link ResourceAccessMode#READ_WRITE READ_WRITE} or only
 * {@link ResourceAccessMode#READ READ} access to the resource. In the former case,
 * execution of the annotated element will occur while no other test class or
 * test method that uses the shared resource is being executed. In the latter case,
 * the annotated element may be executed concurrently with other test classes or
 * methods that also require {@code READ} access but not at the same time as any
 * other test that requires {@code READ_WRITE} access.
 *
 * <p>This guarantee extends to lifecycle methods of a test class or method. For
 * example, if a test method is annotated with a {@code @ResourceLock}
 * annotation the "lock" will be acquired before any
 * {@link BeforeEach @BeforeEach} methods are executed and released after all
 * {@link AfterEach @AfterEach} methods have been executed.
 *
 * <p>This annotation can be repeated to declare the use of multiple shared resources.
 *
 * <p>Since JUnit Jupiter 5.4, this annotation is {@linkplain Inherited inherited}
 * within class hierarchies.
 *
 * @see Isolated
 * @see Resources
 * @see ResourceAccessMode
 * @see ResourceLocks
 * @since 5.3
 */
@API(status = STABLE, since = "5.10")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
@Repeatable(ResourceLocks.class)
public @interface ResourceLock {

	/**
	 * The resource key.
	 *
	 * @see Resources
	 */
	String value();

	/**
	 * The resource access mode.
	 *
	 * <p>Defaults to {@link ResourceAccessMode#READ_WRITE READ_WRITE}.
	 *
	 * @see ResourceAccessMode
	 */
	ResourceAccessMode mode() default ResourceAccessMode.READ_WRITE;

}
