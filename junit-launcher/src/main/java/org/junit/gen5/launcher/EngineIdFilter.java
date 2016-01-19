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

import static org.junit.gen5.engine.FilterResult.excluded;
import static org.junit.gen5.engine.FilterResult.included;

import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.GenericFilter;
import org.junit.gen5.engine.TestEngine;

/**
 * A special filter that is applied before a {@link TestEngine} is executed.
 * It allows to include the given engine id within the test discovery and
 * execution.
 *
 * @since 5.0
 * @see DiscoveryRequest
 */
public class EngineIdFilter implements GenericFilter<String> {
	public static EngineIdFilter byEngineId(String engineId) {
		return new EngineIdFilter(engineId);
	}

	private final String engineId;

	private EngineIdFilter(String engineId) {
		this.engineId = engineId;
	}

	@Override
	public FilterResult filter(String engineId) {
		if (this.engineId.equals(engineId)) {
			return included("Engine ID matches");
		}
		else {
			return excluded("Engine ID does not match");
		}
	}
}
