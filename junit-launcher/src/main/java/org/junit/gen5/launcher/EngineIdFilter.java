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
import static org.junit.gen5.engine.FilterResult.includedIf;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.Filter;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestEngine;

/**
 * An {@code EngineIdFilter} is applied to engine IDs before
 * {@link TestEngine TestEngines} are executed.
 *
 * @since 5.0
 * @see TestDiscoveryRequest
 */
@API(Experimental)
public class EngineIdFilter implements Filter<String> {

	/**
	 * Create a new <em>include</em> {@code EngineIdFilter} based on the
	 * supplied engine ID.
	 *
	 * <p>A {@code TestEngine} with a matching engine ID will be
	 * <em>included</em> within the test discovery and execution.
	 *
	 * @param engineId the engine ID to match against; never {@code null} or empty
	 */
	public static EngineIdFilter includeEngineId(String engineId) {
		Preconditions.notBlank(engineId, "engine ID must not be null or empty");
		return new EngineIdFilter(engineId.trim());
	}

	private final String engineId;

	private EngineIdFilter(String engineId) {
		this.engineId = engineId;
	}

	/**
	 * Get the engine ID that this filter matches against.
	 */
	public final String getEngineId() {
		return this.engineId;
	}

	@Override
	public FilterResult apply(String engineId) {
		return includedIf(this.engineId.equals(engineId), //
			() -> "Engine ID matches", //
			() -> "Engine ID does not match");
	}

	@Override
	public String toString() {
		return "Include engines with ID: " + engineId;
	}

}
