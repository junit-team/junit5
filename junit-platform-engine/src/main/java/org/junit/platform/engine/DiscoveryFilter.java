/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;

/**
 * A {@link DiscoveryFilter} is applied during test discovery to determine if
 * a given container or test should be included in the test plan.
 *
 * <p>{@link TestEngine TestEngines} should apply {@code DiscoveryFilters}
 * during the test discovery phase.
 *
 * @since 1.0
 * @see EngineDiscoveryRequest
 * @see TestEngine
 */
@API(Experimental)
public interface DiscoveryFilter<T> extends Filter<T> {
}
