/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import org.junit.platform.engine.ExecutionRequest;

/**
 * @since 1.0
 */
public class DemoEngineExecutionContext implements EngineExecutionContext {

	public final ExecutionRequest request;

	public DemoEngineExecutionContext(ExecutionRequest request) {
		this.request = request;
	}

}
