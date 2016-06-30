/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.List;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;

/**
 * {@code TestDiscoveryRequest} extends the {@link EngineDiscoveryRequest} API
 * with additional filters that are applied by the {@link Launcher} itself.
 *
 * <p>Specifically, a {@code TestDiscoveryRequest} contains the following.
 *
 * <ul>
 * <li>{@linkplain EngineFilter Engine Filters}: filters that are applied before
 * each {@code TestEngine} is executed</li>
 * <li>{@linkplain ConfigurationParameters Configuration Parameters}: configuration
 * parameters that can be used to influence the discovery process</li>
 * <li>{@linkplain DiscoverySelector Discovery Selectors}: components that select
 * resources that a {@code TestEngine} can use to discover tests</li>
 * <li>{@linkplain DiscoveryFilter Discovery Filters}: filters that should be applied
 * by {@code TestEngines} during test discovery</li>
 * <li>{@linkplain PostDiscoveryFilter Post-Discovery Filters}: filters that will be
 * applied by the {@code Launcher} after {@code TestEngines} have performed test
 * discovery</li>
 * </ul>
 *
 * @since 1.0
 * @see EngineDiscoveryRequest
 * @see EngineFilter
 * @see ConfigurationParameters
 * @see DiscoverySelector
 * @see DiscoveryFilter
 * @see PostDiscoveryFilter
 * @see #getEngineFilters()
 * @see #getPostDiscoveryFilters()
 */
@API(Experimental)
public interface TestDiscoveryRequest extends EngineDiscoveryRequest {

	/**
	 * Get the {@code EngineFilters} for this request.
	 *
	 * @return the list of {@code EngineFilters} for this request; never
	 * {@code null} but potentially empty
	 */
	List<EngineFilter> getEngineFilters();

	/**
	 * Get the {@code PostDiscoveryFilters} for this request.
	 *
	 * @return the list of {@code PostDiscoveryFilters} for this request; never
	 * {@code null} but potentially empty
	 */
	List<PostDiscoveryFilter> getPostDiscoveryFilters();

}
