/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @BeforeAll} is used to signal that the annotated method should be
 * executed <em>before</em> <strong>all</strong> tests in the current test class.
 *
 * <p>In contrast to {@link BeforeEach @BeforeEach} methods, {@code @BeforeAll}
 * methods are only executed once per execution of a given test class. If the
 * test class is annotated with {@link ClassTemplate @ClassTemplate}, the
 * {@code @BeforeAll} methods are executed once before the first invocation of
 * the class template. If a {@link Nested @Nested} test class is declared in a
 * {@link ClassTemplate @ClassTemplate}, its {@code @BeforeAll} methods are
 * called once per execution of the nested test class, namely, once per
 * invocation of the outer class template.
 *
 * <h2>Method Signatures</h2>
 *
 * <p>{@code @BeforeAll} methods must have a {@code void} return type and must
 * be {@code static} by default. Consequently, {@code @BeforeAll} methods are
 * not supported in {@link Nested @Nested} test classes or as <em>interface
 * default methods</em> unless the test class is annotated with
 * {@link TestInstance @TestInstance(Lifecycle.PER_CLASS)}. However, beginning
 * with Java 16 {@code @BeforeAll} methods may be declared as {@code static} in
 * {@link Nested @Nested} test classes, in which case the {@code Lifecycle.PER_CLASS}
 * restriction no longer applies. In addition, {@code @BeforeAll} methods may
 * optionally declare parameters to be resolved by
 * {@link org.junit.jupiter.api.extension.ParameterResolver ParameterResolvers}.
 *
 * <p>Using {@code private} visibility for {@code @BeforeAll} methods is strongly
 * discouraged and will be disallowed in a future release.
 *
 * <h2>Inheritance and Execution Order</h2>
 *
 * <p>{@code @BeforeAll} methods are inherited from superclasses as long as they
 * are not <em>overridden</em> according to the visibility rules of the Java
 * language. Furthermore, {@code @BeforeAll} methods from superclasses will be
 * executed before {@code @BeforeAll} methods in subclasses.
 *
 * <p>Similarly, {@code @BeforeAll} methods declared in an interface are inherited
 * as long as they are not overridden, and {@code @BeforeAll} methods from an
 * interface will be executed before {@code @BeforeAll} methods in the class that
 * implements the interface.
 *
 * <p>JUnit Jupiter does not guarantee the execution order of multiple
 * {@code @BeforeAll} methods that are declared within a single test class or
 * test interface. While it may at times appear that these methods are invoked
 * in alphabetical order, they are in fact sorted using an algorithm that is
 * deterministic but intentionally non-obvious.
 *
 * <p>In addition, {@code @BeforeAll} methods are in no way linked to
 * {@code @AfterAll} methods. Consequently, there are no guarantees with regard
 * to their <em>wrapping</em> behavior. For example, given two
 * {@code @BeforeAll} methods {@code createA()} and {@code createB()} as well as
 * two {@code @AfterAll} methods {@code destroyA()} and {@code destroyB()}, the
 * order in which the {@code @BeforeAll} methods are executed (e.g.
 * {@code createA()} before {@code createB()}) does not imply any order for the
 * seemingly corresponding {@code @AfterAll} methods. In other words,
 * {@code destroyA()} might be called before <em>or</em> after
 * {@code destroyB()}. The JUnit Team therefore recommends that developers
 * declare at most one {@code @BeforeAll} method and at most one
 * {@code @AfterAll} method per test class or test interface unless there are no
 * dependencies between the {@code @BeforeAll} methods or between the
 * {@code @AfterAll} methods.
 *
 * <h2>Composition</h2>
 *
 * <p>{@code @BeforeAll} may be used as a meta-annotation in order to create
 * a custom <em>composed annotation</em> that inherits the semantics of
 * {@code @BeforeAll}.
 *
 * @since 5.0
 * @see AfterAll
 * @see BeforeEach
 * @see AfterEach
 * @see Test
 * @see TestFactory
 * @see TestInstance
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.0")
public @interface BeforeAll {
}
