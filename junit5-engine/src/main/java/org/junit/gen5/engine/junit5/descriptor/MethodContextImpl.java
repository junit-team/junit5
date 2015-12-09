/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.lang.reflect.Method;

import org.junit.gen5.api.extension.MethodContext;

public class MethodContextImpl implements MethodContext {

	private final Object instance;
	private final Method method;

	public static MethodContext methodContext(Object instance, Method method) {
		return new MethodContextImpl(instance, method);
	}

	private MethodContextImpl(Object instance, Method method) {
		this.instance = instance;
		this.method = method;
	}

	@Override
	public Object getInstance() {
		return instance;
	}

	@Override
	public Method getMethod() {
		return method;
	}

}
