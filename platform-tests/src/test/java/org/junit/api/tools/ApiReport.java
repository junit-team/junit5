/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.api.tools;

import java.util.List;
import java.util.Map;

import org.junit.platform.commons.meta.API.Usage;

/**
 * @since 1.0
 */
class ApiReport {

	private final List<Class<?>> types;

	private final Map<Usage, List<Class<?>>> declarationsMap;

	ApiReport(List<Class<?>> types, Map<Usage, List<Class<?>>> declarationsMap) {
		this.types = types;
		this.declarationsMap = declarationsMap;
	}

	List<Class<?>> getTypes() {
		return this.types;
	}

	Map<Usage, List<Class<?>>> getDeclarationsMap() {
		return this.declarationsMap;
	}

}
