/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import java.lang.reflect.Constructor;

/**
 * Unit tests for {@link DefaultExecutableInvoker}.
 *
 * @since 5.9
 */
class DefaultExecutableInvokerTests extends AbstractExecutableInvokerTests {

	@Override
	void invokeMethod() {
		newInvoker().invoke(this.method, this.instance);
	}

	@Override
	<T> T invokeConstructor(Constructor<T> constructor, Object outerInstance) {
		return newInvoker().invoke(constructor, outerInstance);
	}

	private DefaultExecutableInvoker newInvoker() {
		return new DefaultExecutableInvoker(this.extensionContext, this.extensionRegistry);
	}

}
