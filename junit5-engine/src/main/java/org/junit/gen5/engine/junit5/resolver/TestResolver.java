/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import java.util.Optional;

import org.junit.gen5.engine.DiscoveryFilter;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;

/**
 * A {@code TestResolver} is responsible for resolving different kind of test
 * representatives. Each resolver can contribute to the list of children of
 * the given parent.
 *
 * <p>A {@code TestResolver} must not modify existing tree nodes! It is only
 * allowed to contribute new {@link TestDescriptor}. It is possible to attach
 * new {@link TestDescriptor} by calling {@link TestDescriptor#addChild} on
 * the parent.
 *
 * <p>{@code TestResolver} may only resolve children, that accomplish the
 * {@link EngineDiscoveryRequest}. In this manner, each resolver may allow
 * to add {@link DiscoveryFilter} to the {@link EngineDiscoveryRequest}.
 * If it does, it must guarantee, that these filters are applied during test
 * discovery.
 *
 * <p>A child that has been created by the {@code TestResolver} must be
 * reported to the bound {@link TestResolverRegistry} if and only if it
 * was created on demand of resolving the test tree top-down, i.e. if it
 * was attached to the given parent {@link TestDescriptor} within a call to
 * {@link #resolveAllFrom(TestDescriptor, EngineDiscoveryRequest)}. Calls to
 * {@link #fetchBySelector(DiscoverySelector, TestDescriptor)} should never
 * report an actual creation of a {@link TestDescriptor}. These are required
 * for bottom-up resolution and should not lead to further notifications.
 *
 * @since 5.0
 * @see TestDescriptor
 * @see DiscoveryFilter
 */
public interface TestResolver {
	void bindTestResolveryRegistry(TestResolverRegistry testResolverRegistry);

	void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest);

	void resolveUniqueId(TestDescriptor parent, String remainingUniqueId, EngineDiscoveryRequest discoveryRequest);

	Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root);
}
