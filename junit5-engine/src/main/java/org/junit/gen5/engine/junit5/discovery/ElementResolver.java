/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;

public interface ElementResolver {

	/**
	 * Return a set of {@linkplain TestDescriptor testDescriptors} that can be resolved by this resolver.
	 * Returned set must be empty if {@code element} cannot be resolved.
	 */
	Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent);

	/**
	 * Return an optional {@linkplain TestDescriptor testDescriptor}.
	 * Return {@code Optional.empty()} if {@code segment} cannot be resolved.
	 */
	Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent);
}
