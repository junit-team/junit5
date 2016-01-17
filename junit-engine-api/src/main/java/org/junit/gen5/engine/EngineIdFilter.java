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

import static org.junit.gen5.engine.FilterResult.accepted;
import static org.junit.gen5.engine.FilterResult.filtered;

public class EngineIdFilter implements GenericFilter<String> {
	private final String engineId;

	public EngineIdFilter(String engineId) {
		this.engineId = engineId;
	}

	@Override
	public FilterResult filter(String engineId) {
		if (this.engineId.equals(engineId)) {
			return accepted("EngineId matches");
		}
		else {
			return filtered("EngineId matches");
		}
	}
}
