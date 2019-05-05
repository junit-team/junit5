/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;

/**
 * The {@code LauncherDiscoveryRequestBuilder} provides a light-weight DSL for
 * generating a {@link LauncherDiscoveryRequest}.
 *
 * <h4>Example</h4>
 *
 * <pre class="code">
 * import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
 * import static org.junit.platform.engine.discovery.ClassNameFilter.*;
 * import static org.junit.platform.launcher.EngineFilter.*;
 * import static org.junit.platform.launcher.TagFilter.*;
 *
 * // ...
 *
 *   LauncherDiscoveryRequestBuilder.request()
 *     .selectors(
 *        selectPackage("org.example.user"),
 *        selectClass("org.example.payment.PaymentTests"),
 *        selectClass(ShippingTests.class),
 *        selectMethod("org.example.order.OrderTests#test1"),
 *        selectMethod("org.example.order.OrderTests#test2()"),
 *        selectMethod("org.example.order.OrderTests#test3(java.lang.String)"),
 *        selectMethod("org.example.order.OrderTests", "test4"),
 *        selectMethod(OrderTests.class, "test5"),
 *        selectMethod(OrderTests.class, testMethod),
 *        selectClasspathRoots(Collections.singleton(new File("/my/local/path1"))),
 *        selectUniqueId("unique-id-1"),
 *        selectUniqueId("unique-id-2")
 *     )
 *     .filters(
 *        includeEngines("junit-jupiter", "spek"),
 *        // excludeEngines("junit-vintage"),
 *        includeTags("fast"),
 *        // excludeTags("slow"),
 *        includeClassNamePatterns(".*Test[s]?")
 *        // includeClassNamePatterns("org\.example\.tests.*")
 *     )
 *     .configurationParameter("key1", "value1")
 *     .configurationParameters(configParameterMap)
 *     .build();
 * </pre>
 *
 * @since 1.0
 * @see org.junit.platform.engine.discovery.DiscoverySelectors
 * @see org.junit.platform.engine.discovery.ClassNameFilter
 * @see org.junit.platform.launcher.EngineFilter
 * @see org.junit.platform.launcher.TagFilter
 */
@API(status = STABLE, since = "1.0")
public final class LauncherDiscoveryRequestBuilder {

	private List<DiscoverySelector> selectors = new ArrayList<>();
	private List<EngineFilter> engineFilters = new ArrayList<>();
	private List<DiscoveryFilter<?>> discoveryFilters = new ArrayList<>();
	private List<PostDiscoveryFilter> postDiscoveryFilters = new ArrayList<>();
	private Map<String, String> configurationParameters = new HashMap<>();

	/**
	 * Create a new {@code LauncherDiscoveryRequestBuilder}.
	 *
	 * @return a new builder
	 */
	public static LauncherDiscoveryRequestBuilder request() {
		return new LauncherDiscoveryRequestBuilder();
	}

	/**
	 * Add all of the supplied {@code selectors} to the request.
	 *
	 * @param selectors the {@code DiscoverySelectors} to add; never {@code null}
	 * @return this builder for method chaining
	 */
	public LauncherDiscoveryRequestBuilder selectors(DiscoverySelector... selectors) {
		Preconditions.notNull(selectors, "selectors array must not be null");
		selectors(Arrays.asList(selectors));
		return this;
	}

	/**
	 * Add all of the supplied {@code selectors} to the request.
	 *
	 * @param selectors the {@code DiscoverySelectors} to add; never {@code null}
	 * @return this builder for method chaining
	 */
	public LauncherDiscoveryRequestBuilder selectors(List<? extends DiscoverySelector> selectors) {
		Preconditions.notNull(selectors, "selectors list must not be null");
		Preconditions.containsNoNullElements(selectors, "individual selectors must not be null");
		this.selectors.addAll(selectors);
		return this;
	}

	/**
	 * Add all of the supplied {@code filters} to the request.
	 *
	 * <p>The {@code filters} are combined using AND semantics, i.e. all of them
	 * have to include a resource for it to end up in the test plan.
	 *
	 * <p><strong>Warning</strong>: be cautious when registering multiple competing
	 * {@link EngineFilter#includeEngines include} {@code EngineFilters} or multiple
	 * competing {@link EngineFilter#excludeEngines exclude} {@code EngineFilters}
	 * for the same discovery request since doing so will likely lead to
	 * undesirable results (i.e., zero engines being active).
	 *
	 * @param filters the {@code Filter}s to add; never {@code null}
	 * @return this builder for method chaining
	 */
	public LauncherDiscoveryRequestBuilder filters(Filter<?>... filters) {
		Preconditions.notNull(filters, "filters array must not be null");
		Preconditions.containsNoNullElements(filters, "individual filters must not be null");
		Arrays.stream(filters).forEach(this::storeFilter);
		return this;
	}

	/**
	 * Add the supplied <em>configuration parameter</em> to the request.
	 *
	 * @param key the configuration parameter key under which to store the
	 * value; never {@code null} or blank
	 * @param value the value to store
	 * @return this builder for method chaining
	 */
	public LauncherDiscoveryRequestBuilder configurationParameter(String key, String value) {
		Preconditions.notBlank(key, "configuration parameter key must not be null or blank");
		this.configurationParameters.put(key, value);
		return this;
	}

	/**
	 * Add all of the supplied configuration parameters to the request.
	 *
	 * @param configurationParameters the map of configuration parameters to add;
	 * never {@code null}
	 * @return this builder for method chaining
	 * @see #configurationParameter(String, String)
	 */
	public LauncherDiscoveryRequestBuilder configurationParameters(Map<String, String> configurationParameters) {
		Preconditions.notNull(configurationParameters, "configuration parameters map must not be null");
		configurationParameters.forEach(this::configurationParameter);
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
	 * Build the {@link LauncherDiscoveryRequest} that has been configured via
	 * this builder.
	 */
	public LauncherDiscoveryRequest build() {
		LauncherConfigurationParameters launcherConfigurationParameters = new LauncherConfigurationParameters(
			this.configurationParameters);
		return new DefaultDiscoveryRequest(this.selectors, this.engineFilters, this.discoveryFilters,
			this.postDiscoveryFilters, launcherConfigurationParameters);
	}

}
