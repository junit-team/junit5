/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import org.junit.jupiter.api.parallel.ResourceAccessMode;

/**
 * TODO
 */
// TODO: @API(...)
// TODO: Another name to prevent conflict with junit-platform-engine's ExclusiveResource?
public interface ExclusiveResource {

	String getKey();

	ResourceAccessMode getAccessMode();

	static ExclusiveResource of(String key) {
		return null;
	}

	static ExclusiveResource of(String key, ResourceAccessMode accessMode) {
		return null;
	}
}
