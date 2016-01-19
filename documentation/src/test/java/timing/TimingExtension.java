/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package timing;

import static org.junit.gen5.api.extension.ExtensionPointRegistry.Position.INNERMOST;

import java.lang.reflect.Method;

import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext.Namespace;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.TestExtensionContext;

/**
 * Simple extension that <em>times</em> the execution of test methods and
 * prints the results to {@link System#out}.
 *
 * @since 5.0
 */
public class TimingExtension implements ExtensionRegistrar {

	@Override
	public void registerExtensions(ExtensionPointRegistry registry) {
		registry.register(new TestMethodInvocationWrapper(), INNERMOST);
	}

	private static class TestMethodInvocationWrapper implements BeforeEachExtensionPoint, AfterEachExtensionPoint {

		private final Namespace namespace = Namespace.of(getClass());

		@Override
		public void beforeEach(TestExtensionContext context) throws Exception {
			ExtensionContext.Store times = context.getStore(getNamespace(context));
			times.put(context.getTestMethod(), System.currentTimeMillis());
		}

		private Namespace getNamespace(TestExtensionContext context) {
			return Namespace.of(getClass(), context);
		}

		@Override
		public void afterEach(TestExtensionContext context) throws Exception {
			ExtensionContext.Store times = context.getStore(getNamespace(context));
			Method testMethod = context.getTestMethod();
			long start = (long) times.remove(testMethod);
			long duration = System.currentTimeMillis() - start;

			System.out.println(String.format("Method [%s] took %s ms.", testMethod, duration));
		}

	}

}
