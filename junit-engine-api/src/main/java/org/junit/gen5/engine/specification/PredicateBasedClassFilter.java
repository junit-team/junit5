/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.specification;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.gen5.engine.ClassFilter;

public class PredicateBasedClassFilter implements ClassFilter {
	private final Predicate<? super Class<?>> predicate;
	private final Supplier<String> descriptionSupplier;

	public PredicateBasedClassFilter(Predicate<? super Class<?>> predicate, Supplier<String> descriptionSupplier) {
		this.predicate = predicate;
		this.descriptionSupplier = descriptionSupplier;
	}

	@Override
	public String getDescription() {
		return descriptionSupplier.get();
	}

	@Override
	public boolean acceptClass(Class<?> testClass) {
		return predicate.test(testClass);
	}
}
