/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.gen5.commons.meta.API;

/**
 * @since 5.0
 */
@API(Experimental)
public interface TestDescriptor {
	/**
	 * Get the unique identifier (UID) for the described test.
	 *
	 * <p>Uniqueness must be guaranteed across an entire test plan,
	 * regardless of how many engines are used behind the scenes.
	 */
	String getUniqueId();

	String getName();

	String getDisplayName();

	Optional<TestDescriptor> getParent();

	void setParent(TestDescriptor parent);

	boolean isTest();

	boolean isContainer();

	default boolean isRoot() {
		return !getParent().isPresent();
	}

	Set<TestTag> getTags();

	Set<? extends TestDescriptor> getChildren();

	void addChild(TestDescriptor descriptor);

	void removeChild(TestDescriptor descriptor);

	default Set<? extends TestDescriptor> allDescendants() {
		Set<TestDescriptor> all = new LinkedHashSet<>();
		all.addAll(getChildren());
		for (TestDescriptor child : getChildren()) {
			all.addAll(child.allDescendants());
		}
		return all;
	}

	default long countStaticTests() {
		AtomicLong staticTests = new AtomicLong(0);
		Visitor visitor = (descriptor, remove) -> {
			if (descriptor.isTest()) {
				staticTests.incrementAndGet();
			}
		};
		accept(visitor);
		return staticTests.get();
	}

	default boolean hasTests() {
		return (isTest() || getChildren().stream().anyMatch(TestDescriptor::hasTests));
	}

	default Optional<? extends TestDescriptor> findByUniqueId(String uniqueId) {
		if (getUniqueId().equals(uniqueId)) {
			return Optional.of(this);
		}
		// else
		return getChildren().stream().filter(
			testDescriptor -> testDescriptor.getUniqueId().equals(uniqueId)).findFirst();
	}

	interface Visitor {

		void visit(TestDescriptor descriptor, Runnable remove);
	}

	void accept(Visitor visitor);

	Optional<TestSource> getSource();
}
