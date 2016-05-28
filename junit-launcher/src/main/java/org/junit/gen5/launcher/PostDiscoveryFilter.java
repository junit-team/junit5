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

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.Filter;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

/**
 * A {@code PostDiscoveryFilter} is applied to {@link TestDescriptor TestDescriptors}
 * after test discovery.
 *
 * <p>{@link TestEngine TestEngines} must <strong>not</strong> apply
 * {@code PostDiscoveryFilters} during the test discovery phase.
 *
 * @since 5.0
 * @see TestDiscoveryRequest
 * @see TestEngine
 */
@API(Experimental)
public interface PostDiscoveryFilter extends Filter<TestDescriptor> {
}
