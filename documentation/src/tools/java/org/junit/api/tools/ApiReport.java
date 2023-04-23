/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.api.tools;

import java.util.List;
import java.util.Map;

import org.apiguardian.api.API.Status;

/**
 * @since 1.0
 */
class ApiReport {

	private final List<Class<?>> types;

	private final Map<Status, List<Class<?>>> declarationsMap;

	ApiReport(List<Class<?>> types, Map<Status, List<Class<?>>> declarationsMap) {
		this.types = types;
		this.declarationsMap = declarationsMap;
	}

	List<Class<?>> getTypes() {
		return this.types;
	}

	Map<Status, List<Class<?>>> getDeclarationsMap() {
		return this.declarationsMap;
	}

}
