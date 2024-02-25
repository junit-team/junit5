/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * Class-related predicate used by reflection utilities.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public class ClassFilter implements Predicate<Class<?>> {

	/**
	 * Create a {@link ClassFilter} instance that accepts all names but filters classes.
	 */
	public static ClassFilter of(Predicate<Class<?>> classPredicate) {
		return of(name -> true, classPredicate);
	}

	/**
	 * Create a {@link ClassFilter} instance that filters by names and classes.
	 */
	public static ClassFilter of(Predicate<String> namePredicate, Predicate<Class<?>> classPredicate) {
		return new ClassFilter(namePredicate, classPredicate);
	}

	private final Predicate<String> namePredicate;
	private final Predicate<Class<?>> classPredicate;

	private ClassFilter(Predicate<String> namePredicate, Predicate<Class<?>> classPredicate) {
		this.namePredicate = Preconditions.notNull(namePredicate, "name predicate must not be null");
		this.classPredicate = Preconditions.notNull(classPredicate, "class predicate must not be null");
	}

	/**
	 * Test name using the stored name predicate.
	 */
	public boolean match(String name) {
		return namePredicate.test(name);
	}

	/**
	 * Test class using the stored class predicate.
	 */
	public boolean match(Class<?> type) {
		return classPredicate.test(type);
	}

	/**
	 * @implNote This implementation combines all tests stored in the predicates
	 * of this instance. Any new predicate must be added to this test method as
	 * well.
	 */
	@Override
	public boolean test(Class<?> type) {
		return match(type.getName()) && match(type);
	}
}
