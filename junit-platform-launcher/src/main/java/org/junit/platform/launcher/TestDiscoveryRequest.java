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
import org.junit.platform.engine.EngineDiscoveryRequest;

/**
 * {@code TestDiscoveryRequest} is an extension of {@link EngineDiscoveryRequest}
 * that provides access to filters which are applied by the {@link Launcher} itself.
 *
 * @since 1.0
 */
@API(Experimental)
public interface TestDiscoveryRequest extends EngineDiscoveryRequest {

	/**
	 * Get the {@code EngineFilters} that have been added to this request.
	 *
	 * @return the list of {@code EngineFilters} that have been added to this
	 * request; never {@code null} but potentially empty
	 */
	List<EngineFilter> getEngineFilters();

	/**
	 * Get the {@code PostDiscoveryFilters} that have been added to this request.
	 *
	 * @return the list of {@code PostDiscoveryFilters} that have been added to
	 * this request; never {@code null} but potentially empty
	 */
	List<PostDiscoveryFilter> getPostDiscoveryFilters();

}
