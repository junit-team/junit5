/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution.injection.sample;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.extension.TypeBasedParameterResolver;

/**
 * @since 5.6
 */
public class TypeBasedMapOfListsParameterResolver extends TypeBasedParameterResolver<Map<String, List<Integer>>> {
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Map<String, List<Integer>> map = new TreeMap<>();
		map.put("ids", asList(1, 42));
		return map;
	}
}
