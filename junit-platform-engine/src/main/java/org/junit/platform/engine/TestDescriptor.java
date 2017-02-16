/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.support.descriptor.DefaultLegacyReportingInfo;

/**
 * Mutable descriptor for a test or container that has been discovered by a
 * {@link TestEngine}.
 *
 * @see TestEngine
 * @since 1.0
 */
@API(Experimental)
public interface TestDescriptor {

	/**
	 * Get the unique identifier (UID) for this descriptor.
	 *
	 * <p>Uniqueness must be guaranteed across an entire test plan,
	 * regardless of how many engines are used behind the scenes.
	 *
	 * @return the {@code UniqueId} for this descriptor; never {@code null}
	 */
	UniqueId getUniqueId();

	/**
	 * Get the display name for this descriptor.
	 *
	 * <p>A <em>display name</em> is a human-readable name for a test or
	 * container that is typically used for test reporting in IDEs and build
	 * tools. Display names may contain spaces, special characters, and emoji,
	 * and the format may be customized by {@link TestEngine TestEngines} or
	 * potentially by end users as well. Consequently, display names should
	 * never be parsed; rather, they should be used for display purposes only.
	 *
	 * @return the display name for this descriptor; never {@code null} or blank
	 * @see #getSource()
	 */
	String getDisplayName();

	/**
	 * Get the set of {@linkplain TestTag tags} associated with this descriptor.
	 *
	 * @return the set of tags associated with this descriptor; never {@code null}
	 * but potentially empty
	 * @see TestTag
	 */
	Set<TestTag> getTags();

	/**
	 * Get the {@linkplain TestSource source} of the test or container described
	 * by this descriptor, if available.
	 *
	 * @see TestSource
	 */
	Optional<TestSource> getSource();

	/**
	 * Get the <em>parent</em> of this descriptor, if available.
	 */
	Optional<TestDescriptor> getParent();

	/**
	 * Set the <em>parent</em> of this descriptor.
	 *
	 * @param parent the new parent of this descriptor; may be {@code null}.
	 */
	void setParent(TestDescriptor parent);

	/**
	 * Get the set of <em>children</em> of this descriptor.
	 *
	 * @return the set of children of this descriptor; never {@code null}
	 * but potentially empty
	 * @see #getDescendants()
	 */
	Set<? extends TestDescriptor> getChildren();

	/**
	 * Get the set of all <em>descendants</em> of this descriptor.
	 *
	 * <p>A <em>descendant</em> is a child of this descriptor or a child of one of
	 * its children, recursively.
	 *
	 * @see #getChildren()
	 */
	default Set<? extends TestDescriptor> getDescendants() {
		Set<TestDescriptor> descendants = new LinkedHashSet<>();
		descendants.addAll(getChildren());
		for (TestDescriptor child : getChildren()) {
			descendants.addAll(child.getDescendants());
		}
		return descendants;
	}

	/**
	 * Add a <em>child</em> to this descriptor.
	 *
	 * @param descriptor the child to add to this descriptor; never {@code null}
	 */
	void addChild(TestDescriptor descriptor);

	/**
	 * Remove a <em>child</em> from this descriptor.
	 *
	 * @param descriptor the child to remove from this descriptor; never
	 * {@code null}
	 */
	void removeChild(TestDescriptor descriptor);

	/**
	 * Remove this non-root descriptor from its parent and remove all the
	 * children from this descriptor.
	 *
	 * <p>If this method is invoked on a {@linkplain #isRoot root} descriptor,
	 * this method must throw a {@link org.junit.platform.commons.JUnitException
	 * JUnitException} explaining that a root cannot be removed from the
	 * hierarchy.
	 */
	void removeFromHierarchy();

	/**
	 * Determine if this descriptor is a <em>root</em> descriptor.
	 *
	 * <p>A <em>root</em> descriptor is a descriptor without a parent.
	 */
	default boolean isRoot() {
		return !getParent().isPresent();
	}

	/**
	 * Determine if this descriptor describes a container.
	 */
	boolean isContainer();

	/**
	 * Determine if this descriptor describes a test.
	 */
	boolean isTest();

	/**
	 * Determine if this descriptor or any of its descendants describes a test.
	 */
	default boolean hasTests() {
		return (isTest() || getChildren().stream().anyMatch(TestDescriptor::hasTests));
	}

	/**
	 * Find the descriptor with the supplied unique ID.
	 *
	 * <p>The search algorithm begins with this descriptor and then searches
	 * through its descendants.
	 *
	 * @param uniqueId the {@code UniqueId} to search for; never {@code null}
	 */
	Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId);

	/**
	 * Accept a visitor to the subtree starting with this descriptor.
	 *
	 * @param visitor the {@code Visitor} to accept; never {@code null}
	 */
	default void accept(Visitor visitor) {
		visitor.visit(this);
		// Create a copy of the set in order to avoid a ConcurrentModificationException
		new LinkedHashSet<>(this.getChildren()).forEach(child -> child.accept(visitor));
	}

	/**
	 * Does a logical level display name resolution for legacy reporting formats, instead of a source level name resolution.
	 *
	 * @return A LegacyReportingInfo containing the logical method and class display names.
	 */
	default LegacyReportingInfo getLegacyReportingInfo() {
		final String displayName = getDisplayName();
		final Optional<String> className = getParent().map(TestDescriptor::getDisplayName);

		return new DefaultLegacyReportingInfo(displayName, className.orElse(null));
	}

	interface LegacyReportingInfo {
		Optional<String> getMethodName();

		Optional<String> getClassName();
	}

	/**
	 * Visitor for the tree-like {@link TestDescriptor} structure.
	 *
	 * @see TestDescriptor#accept
	 */
	interface Visitor {

		/**
		 * Visit a {@link TestDescriptor}.
		 *
		 * @param descriptor the {@code TestDescriptor} to visit; never {@code null}
		 */
		void visit(TestDescriptor descriptor);
	}

}
