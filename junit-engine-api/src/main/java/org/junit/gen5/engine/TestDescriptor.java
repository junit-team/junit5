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

/**
 * @author Sam Brannen
 * @since 5.0
 */
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

	long getNumberOfStaticTests();

	default boolean hasTests() {
		if (isTest())
			return true;
		return getChildren().stream().anyMatch(anyDescriptor -> anyDescriptor.hasTests());
	}

	default Optional<TestDescriptor> findByUniqueId(String uniqueId) {
		if (getUniqueId().equals(uniqueId))
			return Optional.of(this);
		return getChildren().stream().filter(
			testDescriptor -> testDescriptor.getUniqueId().equals(uniqueId)).findFirst();
	}

	interface Visitor {

		void visit(TestDescriptor descriptor, Runnable remove);
	}

	void accept(Visitor visitor);

	Optional<TestSource> getSource();
}
