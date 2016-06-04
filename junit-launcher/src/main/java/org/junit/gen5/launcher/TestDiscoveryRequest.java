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
	 */
	void addSelector(DiscoverySelector selector);

	/**
	 * Add all of the supplied {@code selectors} to this request.
	 */
	void addSelectors(Collection<DiscoverySelector> selectors);

	/**
	 * Add the supplied {@code engineFilter} to this request.
	 *
	 * <p><strong>Warning</strong>: be cautious when registering multiple competing
	 * {@link EngineFilter#requireEngines require} {@code EngineFilters} or multiple
	 * competing {@link EngineFilter#excludeEngines exclude} {@code EngineFilters}
	 * for the same discovery request since doing so will likely lead to
	 * undesirable results (i.e., zero engines being active).
	 */
	void addEngineFilter(EngineFilter engineFilter);

	/**
	 * Add all of the supplied {@code engineFilters} to this request.
	 *
	 * <p><strong>Warning</strong>: be cautious when registering multiple competing
	 * {@link EngineFilter#requireEngines require} {@code EngineFilters} or multiple
	 * competing {@link EngineFilter#excludeEngines exclude} {@code EngineFilters}
	 * for the same discovery request since doing so will likely lead to
	 * undesirable results (i.e., zero engines being active).
	 */
	void addEngineFilters(Collection<EngineFilter> engineFilters);

	/**
	 * Add the supplied {@code discoveryFilter} to this request.
	 */
	void addFilter(DiscoveryFilter<?> discoveryFilter);

	/**
	 * Add all of the supplied {@code discoveryFilters} to this request.
	 */
	void addFilters(Collection<DiscoveryFilter<?>> discoveryFilters);

	/**
	 * Add the supplied {@code postDiscoveryFilter} to this request.
	 */
	void addPostFilter(PostDiscoveryFilter postDiscoveryFilter);

	/**
	 * Add all of the supplied {@code postDiscoveryFilters} to this request.
	 */
	void addPostFilters(Collection<PostDiscoveryFilter> postDiscoveryFilters);

	/**
	 * Add the supplied {@code configurationParameters} to this request.
	 */
	void addConfigurationParameters(Map<String, String> configurationParameters);

	/**
	 * Get the {@code engineFilters} that have been added to this request.
	 */
	List<EngineFilter> getEngineFilters();

	/**
	 * Get the {@code postDiscoveryFilters} that have been added to this request.
	 */
	List<PostDiscoveryFilter> getPostDiscoveryFilters();

}
