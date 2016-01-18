/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.DiscoveryFilter;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * The {@code DiscoveryRequestBuilder} provides a light-weight DSL for
 * generating a {@link DiscoveryRequest}.
 *
 * <p>Example:
 *
 * <pre style="code">
 *   DiscoveryRequestBuilder.request()
 *     .select(
 *       packageName("org.junit.gen5"),
 *       packageName("com.junit.samples"),
 *       testClass(TestDescriptorTests.class),
 *       testClassByName("com.junit.samples.SampleTestCase"),
 *       testMethod("com.junit.samples.SampleTestCase", "test2"),
 *       testMethod(TestDescriptorTests.class, "test1"),
 *       testMethod(TestDescriptorTests.class, "test1"),
 *       testMethod(TestDescriptorTests.class, "testWithParams", ParameterType.class),
 *       testMethod(TestDescriptorTests.class, testMethod),
 *       path("/my/local/path1"),
 *       path("/my/local/path2"),
 *       uniqueId("unique-id-1"),
 *       uniqueId("unique-id-2")
 *     )
 *     .filterBy(engineIds("junit5"))
 *     .filterBy(classNamePattern("org.junit.gen5.tests"), classNamePattern("org.junit.sample"))
 *     .filterBy(tagsIncluded("Fast"), tagsExcluded("Slow"))
 *   ).build();
 * </pre>
 */
public final class DiscoveryRequestBuilder {
	private List<DiscoverySelector> selectors = new LinkedList<>();
	private List<EngineIdFilter> engineIdFilters = new LinkedList<>();
	private List<DiscoveryFilter<?>> filters = new LinkedList<>();
	private List<PostDiscoveryFilter> postFilters = new LinkedList<>();

	public static DiscoveryRequestBuilder request() {
		return new DiscoveryRequestBuilder();
	}

	public DiscoveryRequestBuilder select(DiscoverySelector... elements) {
		if (elements != null) {
			select(Arrays.asList(elements));
		}
		return this;
	}

	public DiscoveryRequestBuilder select(List<DiscoverySelector> elements) {
		if (elements != null) {
			this.selectors.addAll(elements);
		}
		return this;
	}

	public DiscoveryRequestBuilder filterBy(EngineIdFilter... filters) {
		if (filters != null) {
			this.engineIdFilters.addAll(Arrays.asList(filters));
		}
		return this;
	}

	public DiscoveryRequestBuilder filterBy(DiscoveryFilter<?>... filters) {
		if (filters != null) {
			this.filters.addAll(Arrays.asList(filters));
		}
		return this;
	}

	public DiscoveryRequestBuilder filterBy(PostDiscoveryFilter... filters) {
		if (filters != null) {
			this.postFilters.addAll(Arrays.asList(filters));
		}
		return this;
	}

	public DiscoveryRequest build() {
		DiscoveryRequest discoveryRequest = new DiscoveryRequest();
		discoveryRequest.addSelectors(this.selectors);
		discoveryRequest.addEngineIdFilters(this.engineIdFilters);
		discoveryRequest.addFilters(this.filters);
		discoveryRequest.addPostFilters(this.postFilters);
		return discoveryRequest;
	}

}
