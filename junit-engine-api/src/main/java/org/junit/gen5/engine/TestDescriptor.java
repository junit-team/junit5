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
	UniqueId getUniqueId();

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

	void removeFromHierarchy();

	default Set<? extends TestDescriptor> allDescendants() {
		Set<TestDescriptor> all = new LinkedHashSet<>();
		all.addAll(getChildren());
		for (TestDescriptor child : getChildren()) {
			all.addAll(child.allDescendants());
		}
		return all;
	}

	default boolean hasTests() {
		return (isTest() || getChildren().stream().anyMatch(TestDescriptor::hasTests));
	}

	Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId);

	interface Visitor {

		void visit(TestDescriptor descriptor);
	}

	void accept(Visitor visitor);

	Optional<TestSource> getSource();

}
