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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.DiscoveryFilter;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;

/**
 * {@code TestDiscoveryRequest} is an extension of {@link EngineDiscoveryRequest}
 * that provides access to filters which are applied by the {@link Launcher} itself.
 *
 * <p>Moreover, the {@code add*()} methods can be used by external clients that do
 * not want to use the {@link org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder}.
 *
 * @since 5.0
 */
@API(Experimental)
public interface TestDiscoveryRequest extends EngineDiscoveryRequest {

	/**
	 * Add the supplied {@code selector} to this request.
	 *
	 * @param selector the {@code DiscoverySelector} to add; never {@code null}
	 */
	void addSelector(DiscoverySelector selector);

	/**
	 * Add all of the supplied {@code selectors} to this request.
	 *
	 * @param selectors the {@code DiscoverySelectors} to add; never {@code null}
	 */
	void addSelectors(Collection<DiscoverySelector> selectors);

	/**
	 * Add the supplied {@code engineFilter} to this request.
	 *
	 * <p><strong>Warning</strong>: be cautious when registering multiple competing
	 * {@link EngineFilter#includeEngines include} {@code EngineFilters} or multiple
	 * competing {@link EngineFilter#excludeEngines exclude} {@code EngineFilters}
	 * for the same discovery request since doing so will likely lead to
	 * undesirable results (i.e., zero engines being active).
	 *
	 * @param engineFilter the {@code EngineFilter} to add; never {@code null}
	 */
	void addEngineFilter(EngineFilter engineFilter);

	/**
	 * Add all of the supplied {@code engineFilters} to this request.
	 *
	 * <p><strong>Warning</strong>: be cautious when registering multiple competing
	 * {@link EngineFilter#includeEngines include} {@code EngineFilters} or multiple
	 * competing {@link EngineFilter#excludeEngines exclude} {@code EngineFilters}
	 * for the same discovery request since doing so will likely lead to
	 * undesirable results (i.e., zero engines being active).
	 *
	 * @param engineFilters the {@code EngineFilters} to add; never {@code null}
	 */
	void addEngineFilters(Collection<EngineFilter> engineFilters);

	/**
	 * Add the supplied {@code discoveryFilter} to this request.
	 *
	 * @param discoveryFilter the {@code DiscoveryFilter} to add;
	 * never {@code null}
	 */
	void addFilter(DiscoveryFilter<?> discoveryFilter);

	/**
	 * Add all of the supplied {@code discoveryFilters} to this request.
	 *
	 * @param discoveryFilters the {@code DiscoveryFilters} to add;
	 * never {@code null}
	 */
	void addFilters(Collection<DiscoveryFilter<?>> discoveryFilters);

	/**
	 * Add the supplied {@code postDiscoveryFilter} to this request.
	 *
	 * @param postDiscoveryFilter the {@code PostDiscoveryFilter} to add;
	 * never {@code null}
	 */
	void addPostFilter(PostDiscoveryFilter postDiscoveryFilter);

	/**
	 * Add all of the supplied {@code postDiscoveryFilters} to this request.
	 *
	 * @param postDiscoveryFilters the {@code PostDiscoveryFilters} to add;
	 * never {@code null}
	 */
	void addPostFilters(Collection<PostDiscoveryFilter> postDiscoveryFilters);

	/**
	 * Add the supplied configuration parameters to this request.
	 *
	 * @param configurationParameters the map of configuration parameters to add;
	 * never {@code null}
	 */
	void addConfigurationParameters(Map<String, String> configurationParameters);

	/**
	 * Get the {@code EngineFilters} that have been added to this request.
	 *
	 * @return the list of {@code EngineFilters} that have been added to this
	 * request; never {@code null} but potentially empty
	 */
	List<EngineFilter> getEngineFilters();

	/**
	 * Get the {@code PostDiscoveryFilters} that have been added to this request.
	 *
	 * @return the list of {@code PostDiscoveryFilters} that have been added to
	 * this request; never {@code null} but potentially empty
	 */
	List<PostDiscoveryFilter> getPostDiscoveryFilters();

}
