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
 * This class extends the {@link EngineDiscoveryRequest}
 * by providing access to filters which are applied by the
 * {@link Launcher} itself
 *
 * <p>Moreover, the add*-methods can be used by external clients
 * that do not want to use the
 * {@link org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder}.
 *
 * @since 5.0
 */
@API(Experimental)
public interface TestDiscoveryRequest extends EngineDiscoveryRequest {

	void addSelector(DiscoverySelector selector);

	void addSelectors(Collection<DiscoverySelector> selectors);

	void addEngineIdFilter(EngineIdFilter engineIdFilter);

	void addEngineIdFilters(Collection<EngineIdFilter> engineIdFilters);

	void addFilter(DiscoveryFilter<?> discoveryFilter);

	void addFilters(Collection<DiscoveryFilter<?>> discoveryFilters);

	void addPostFilter(PostDiscoveryFilter postDiscoveryFilter);

	void addPostFilters(Collection<PostDiscoveryFilter> postDiscoveryFilters);

	void addLaunchParameters(Map<String, String> launchParameters);

	List<EngineIdFilter> getEngineIdFilters();

	List<PostDiscoveryFilter> getPostDiscoveryFilters();

}
