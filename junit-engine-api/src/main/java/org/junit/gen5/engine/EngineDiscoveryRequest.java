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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;

/**
 * An {@code EngineDiscoveryRequest} gives {@link TestEngine}s
 * access to the information necessary to discover {@link TestDescriptor}s.
 *
 * @since 5.0
 */
@API(Experimental)
public interface EngineDiscoveryRequest {

	List<DiscoverySelector> getSelectors();

	<T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType);

	<T extends DiscoveryFilter<?>> List<T> getDiscoveryFiltersByType(Class<T> filterType);

	Map<String, String> getLaunchParameters();

	Optional<String> getLaunchParameter(String key);
}
