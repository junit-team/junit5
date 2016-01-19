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

import java.util.*;

import org.junit.gen5.engine.*;

/**
 *
 *
 * @since 5.0
 */
public interface TestDiscoveryRequest extends EngineDiscoveryRequest {

	void addSelector(DiscoverySelector selector);

	void addSelectors(Collection<DiscoverySelector> selectors);

	void addEngineIdFilter(EngineIdFilter engineIdFilter);

	void addEngineIdFilters(Collection<EngineIdFilter> engineIdFilters);

	void addFilter(DiscoveryFilter<?> discoveryFilter);

	void addFilters(Collection<DiscoveryFilter<?>> discoveryFilters);

	void addPostFilter(PostDiscoveryFilter postDiscoveryFilter);

	void addPostFilters(Collection<PostDiscoveryFilter> postDiscoveryFilters);

	List<EngineIdFilter> getEngineIdFilters();

	List<PostDiscoveryFilter> getPostDiscoveryFilters();

	boolean acceptDescriptor(TestDescriptor testDescriptor);
}
