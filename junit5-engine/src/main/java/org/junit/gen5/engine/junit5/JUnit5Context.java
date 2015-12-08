/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.engine.Context;

public class JUnit5Context implements Context {

	private final Map<String, Object> map;

	public JUnit5Context() {
		this(new HashMap<>());
	}

	private JUnit5Context(Map<String, Object> map) {
		this.map = map;
	}

	public JUnit5Context withTestInstanceProvider(TestInstanceProvider testInstanceProvider) {
		return with(TestInstanceProvider.class.getName(), testInstanceProvider);
	}

	public TestInstanceProvider getTestInstanceProvider() {
		return get(TestInstanceProvider.class.getName(), TestInstanceProvider.class);
	}

	private JUnit5Context with(String key, Object value) {
		Map<String, Object> newMap = new HashMap<>(map);
		newMap.put(key, value);
		return new JUnit5Context(newMap);
	}

	private <T> T get(String key, Class<T> clazz) {
		return clazz.cast(map.get(key));
	}

}
