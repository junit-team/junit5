/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.DiscoveryFilter;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.Filter;
import org.junit.gen5.launcher.EngineIdFilter;
import org.junit.gen5.launcher.PostDiscoveryFilter;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * The {@code DiscoveryRequestBuilder} provides a light-weight DSL for
 * generating a {@link TestDiscoveryRequest}.
 *
 * <h4>Example</h4>
 *
 * <pre style="code">
 *   DiscoveryRequestBuilder.request()
 *     .select(
 *       forPackageName("org.junit.gen5"),
 *       forPackageName("com.junit.samples"),
 *       forClass(TestDescriptorTests.class),
 *       forClassName("com.junit.samples.SampleTestCase"),
 *       forTestMethod("com.junit.samples.SampleTestCase", "test2"),
 *       forTestMethod(TestDescriptorTests.class, "test1"),
 *       forTestMethod(TestDescriptorTests.class, "test1"),
 *       forTestMethod(TestDescriptorTests.class, "testWithParams", ParameterType.class),
 *       forTestMethod(TestDescriptorTests.class, testMethod),
 *       forPath("/my/local/path1"),
 *       forPath("/my/local/path2"),
 *       forUniqueId("unique-id-1"),
 *       forUniqueId("unique-id-2")
 *     )
 *     .filter(byEngineIds("junit5"))
 *     .filter(byNamePattern("org\.junit\.gen5\.tests.*"), byNamePattern(".*Test[s]?"))
 *     .filter(requireTags("fast"), excludeTags("flow"))
 *   ).build();
 * </pre>
 */
@API(Experimental)
public final class TestDiscoveryRequestBuilder {

	private List<DiscoverySelector> selectors = new LinkedList<>();
	private List<EngineIdFilter> engineIdFilters = new LinkedList<>();
	private List<DiscoveryFilter<?>> discoveryFilters = new LinkedList<>();
	private List<PostDiscoveryFilter> postDiscoveryFilters = new LinkedList<>();

	public static TestDiscoveryRequestBuilder request() {
		return new TestDiscoveryRequestBuilder();
	}

	public TestDiscoveryRequestBuilder select(DiscoverySelector... elements) {
		if (elements != null) {
			select(Arrays.asList(elements));
		}
		return this;
	}

	public TestDiscoveryRequestBuilder select(List<DiscoverySelector> elements) {
		if (elements != null) {
			this.selectors.addAll(elements);
		}
		return this;
	}

	public TestDiscoveryRequestBuilder filter(Filter<?>... filters) {
		if (filters != null) {
			Arrays.stream(filters).forEach(this::storeFilter);
		}
		return this;
	}

	private void storeFilter(Filter<?> filter) {
		if (filter instanceof EngineIdFilter) {
			this.engineIdFilters.add((EngineIdFilter) filter);
		}
		else if (filter instanceof PostDiscoveryFilter) {
			this.postDiscoveryFilters.add((PostDiscoveryFilter) filter);
		}
		else if (filter instanceof DiscoveryFilter<?>) {
			this.discoveryFilters.add((DiscoveryFilter<?>) filter);
		}
		else {
			throw new PreconditionViolationException("Filter must implement " + EngineIdFilter.class.getSimpleName()
					+ ", " + PostDiscoveryFilter.class.getSimpleName() //
					+ ", or " + DiscoveryFilter.class.getSimpleName() + ".");
		}
	}

	public TestDiscoveryRequest build() {
		DiscoveryRequest discoveryRequest = new DiscoveryRequest();
		discoveryRequest.addSelectors(this.selectors);
		discoveryRequest.addEngineIdFilters(this.engineIdFilters);
		discoveryRequest.addFilters(this.discoveryFilters);
		discoveryRequest.addPostFilters(this.postDiscoveryFilters);
		return discoveryRequest;
	}

}
