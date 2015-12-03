/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @since 5.0
 */
// TODO Divide into public facing TestDescriptor and engine-internal MutableTestDescriptor.
//
// The tree of TestDescriptors should be parallel to (and not just a super type of)
// MutableTestDescriptor so that clients won't rely on implementation details of
// MutableTestDescriptor.
public interface TestDescriptor {

	/**
	 * Get the unique identifier (UID) for the described test.
	 *
	 * <p>Uniqueness must be guaranteed across an entire test plan,
	 * regardless of how many engines are used behind the scenes.
	 */
	String getUniqueId();

	String getDisplayName();

	Optional<TestDescriptor> getParent();

	boolean isTest();

	default boolean isRoot() {
		return getParent() == null;
	}

	Set<TestTag> getTags();

	void addChild(TestDescriptor descriptor);

	void removeChild(TestDescriptor descriptor);

	Set<TestDescriptor> getChildren();

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

	default Optional<TestDescriptor> findByUniqueId(String uniqueId) {
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
