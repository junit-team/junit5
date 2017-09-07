/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import static org.junit.platform.commons.meta.API.Usage.Stable;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestEngine;

/**
 * A {@code PostDiscoveryFilter} is applied to {@link TestIdentifier TestIdentifiers}
 * after test discovery.
 *
 * <p>A {@code PostDiscoveryFilter} must <strong>not</strong> modify the
 * {@link TestDescriptor TestDescriptors} it is applied to in any way.
 *
 * <p>{@link TestEngine TestEngines} must <strong>not</strong> apply
 * {@code PostDiscoveryFilters} during the test discovery phase.
 *
 * @since 1.0
 * @see LauncherDiscoveryRequest
 * @see TestEngine
 */
@API(Stable)
public interface PostDiscoveryFilter extends Filter<TestIdentifier> {
}
