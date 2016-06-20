/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.List;

import org.junit.platform.commons.meta.API;

/**
 * Provides {@link TestEngine TestEngines} access to the information necessary
 * to discover {@link TestDescriptor TestDescriptors}.
 *
 * <p>A request is comprised of {@linkplain DiscoverySelector selectors} and
 * {@linkplain DiscoveryFilter filters}. While the former specify which tests
 * are to be <em>selected</em>, the latter specify how they are to be
 * <em>filtered</em>.
 *
 * <p>In addition, the supplied {@linkplain ConfigurationParameters
 * configuration parameters} may be used to influence the discovery process.
 *
 * @see TestEngine
 * @see DiscoverySelector
 * @see DiscoveryFilter
 * @since 1.0
 */
@API(Experimental)
public interface EngineDiscoveryRequest {

	/**
	 * Get the {@link DiscoverySelector DiscoverySelectors} of this request.
	 */
	List<DiscoverySelector> getSelectors();

	/**
	 * Get the {@link DiscoverySelector DiscoverySelectors} of this request,
	 * filtered by a particular type.
	 *
	 * @param selectorType the type of {@link DiscoverySelector} to filter by
	 * @return all selectors of this request that are instances of {@code selectorType}
	 */
	<T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType);

	/**
	 * Get the {@link DiscoveryFilter DiscoveryFilters} of this request, filtered
	 * by a particular type.
	 *
	 * @param filterType the type of {@link DiscoveryFilter} to filter by
	 * @return all filters of this request that are instances of {@code filterType}
	 */
	<T extends DiscoveryFilter<?>> List<T> getDiscoveryFiltersByType(Class<T> filterType);

	/**
	 * Get the {@link ConfigurationParameters} of this request.
	 */
	ConfigurationParameters getConfigurationParameters();

}
