/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.List;

public interface EngineDiscoveryRequest {

	List<DiscoverySelector> getSelectors();

	<T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType);

	List<DiscoveryFilter<?>> getDiscoveryFilters();

	<T extends DiscoveryFilter<?>> List<T> getDiscoveryFiltersByType(Class<T> filterType);

	void accept(DiscoverySelectorVisitor visitor);

}
