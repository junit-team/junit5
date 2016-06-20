/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestDiscoveryRequest;

/**
 * The {@code TestDiscoveryRequestBuilder} provides a light-weight DSL for
 * generating a {@link TestDiscoveryRequest}.
 *
 * <h4>Example</h4>
 *
 * <pre style="code">
 *   TestDiscoveryRequestBuilder.request()
 *     .selectors(
 *        selectPackage("org.example.user"),
 *        selectClass("org.example.payment.PaymentTests"),
 *        selectClass(ShippingTests.class),
 *        selectMethod("org.example.order.OrderTests", "test1"),
 *        selectMethod(OrderTests.class, "test2"),
 *        selectMethod(OrderTests.class, testMethod),
 *        selectClasspathRoots("/my/local/path1"),
 *        selectClasspathRoots("/my/local/path2"),
 *        selectUniqueId("unique-id-1"),
 *        selectUniqueId("unique-id-2")
 *     )
 *     .filters(
 *        includeEngines("junit-jupiter", "kotlin"),
 *        // excludeEngines("junit-vintage"),
 *        includeTags("fast"),
 *        // excludeTags("slow"),
 *        includeClassNamePattern(".*Test[s]?")
 *        // includeClassNamePattern("org\.example\.tests.*")
 *     )
 *     .configurationParameter("key1", "value1")
 *     .configurationParameters(configParameterMap)
 *     .build();
 * </pre>
 *
 * @since 1.0
 */
@API(Experimental)
public final class TestDiscoveryRequestBuilder {

	private List<DiscoverySelector> selectors = new LinkedList<>();
	private List<EngineFilter> engineFilters = new LinkedList<>();
	private List<DiscoveryFilter<?>> discoveryFilters = new LinkedList<>();
	private List<PostDiscoveryFilter> postDiscoveryFilters = new LinkedList<>();
	private Map<String, String> configurationParameters = new HashMap<>();

	/**
	 * Create a new {@code TestDiscoveryRequestBuilder}.
	 */
	public static TestDiscoveryRequestBuilder request() {
		return new TestDiscoveryRequestBuilder();
	}

	/**
	 * Add all of the supplied {@code selectors} to the request.
	 */
	public TestDiscoveryRequestBuilder selectors(DiscoverySelector... selectors) {
		if (selectors != null) {
			selectors(Arrays.asList(selectors));
		}
		return this;
	}

	/**
	 * Add all of the supplied {@code selectors} to the request.
	 */
	public TestDiscoveryRequestBuilder selectors(List<DiscoverySelector> selectors) {
		if (selectors != null) {
			this.selectors.addAll(selectors);
		}
		return this;
	}

	/**
	 * Add all of the supplied {@code filters} to the request.
	 */
	public TestDiscoveryRequestBuilder filters(Filter<?>... filters) {
		if (filters != null) {
			Arrays.stream(filters).forEach(this::storeFilter);
		}
		return this;
	}

	/**
	 * Add the supplied <em>configuration parameter</em> to the request.
	 */
	public TestDiscoveryRequestBuilder configurationParameter(String key, String value) {
		Preconditions.notBlank(key, "configuration parameter key must not be null or blank");
		this.configurationParameters.put(key, value);
		return this;
	}

	/**
	 * Add all of the supplied {@code configurationParameters} to the request.
	 */
	public TestDiscoveryRequestBuilder configurationParameters(Map<String, String> configurationParameters) {
		if (configurationParameters != null) {
			configurationParameters.forEach(this::configurationParameter);
		}
		return this;
	}

	private void storeFilter(Filter<?> filter) {
		if (filter instanceof EngineFilter) {
			this.engineFilters.add((EngineFilter) filter);
		}
		else if (filter instanceof PostDiscoveryFilter) {
			this.postDiscoveryFilters.add((PostDiscoveryFilter) filter);
		}
		else if (filter instanceof DiscoveryFilter<?>) {
			this.discoveryFilters.add((DiscoveryFilter<?>) filter);
		}
		else {
			throw new PreconditionViolationException(
				String.format("Filter [%s] must implement %s, %s, or %s.", filter, EngineFilter.class.getSimpleName(),
					PostDiscoveryFilter.class.getSimpleName(), DiscoveryFilter.class.getSimpleName()));
		}
	}

	/**
	 * Build the {@link TestDiscoveryRequest} that has been configured via
	 * this builder.
	 */
	public TestDiscoveryRequest build() {
		DiscoveryRequest discoveryRequest = new DiscoveryRequest();
		discoveryRequest.addSelectors(this.selectors);
		discoveryRequest.addEngineFilters(this.engineFilters);
		discoveryRequest.addFilters(this.discoveryFilters);
		discoveryRequest.addPostFilters(this.postDiscoveryFilters);
		discoveryRequest.addConfigurationParameters(this.configurationParameters);
		return discoveryRequest;
	}

}
