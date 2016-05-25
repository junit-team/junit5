/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;

import org.junit.gen5.commons.meta.API;

/**
 * Factory for {@link MethodInvocationContext} instances.
 *
 * @since 5.0
 */
@API(Internal)
public class MethodInvocationContextFactory {

	/**
	 * Create a new {@link MethodInvocationContext} based on the supplied
	 * {@code instance} and {@code method}.
	 */
	public static MethodInvocationContext methodInvocationContext(Object instance, Method method) {
		return new DefaultMethodInvocationContext(instance, method);
	}

	private static class DefaultMethodInvocationContext implements MethodInvocationContext {

		private final Object instance;

		private final Method method;

		private DefaultMethodInvocationContext(Object instance, Method method) {
			this.instance = instance;
			this.method = method;
		}

		@Override
		public Object getInstance() {
			return this.instance;
		}

		@Override
		public Method getMethod() {
			return this.method;
		}

	}

}
