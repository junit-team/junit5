/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.timing;

import static org.junit.gen5.api.extension.ExtensionPointRegistry.Position.INNERMOST;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.junit.gen5.api.extension.AfterEachCallback;
import org.junit.gen5.api.extension.BeforeEachCallback;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext.Namespace;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.TestExtensionContext;

/**
 * Simple extension that <em>times</em> the execution of test methods and
 * logs the results at {@code INFO} level.
 *
 * @since 5.0
 */
public class TimingExtension implements ExtensionRegistrar {

	private static final Logger LOG = Logger.getLogger(TimingExtension.class.getName());

	@Override
	public void registerExtensions(ExtensionPointRegistry registry) {
		registry.register(new TestMethodInvocationWrapper(), INNERMOST);
	}

	private static class TestMethodInvocationWrapper implements BeforeEachCallback, AfterEachCallback {

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

			LOG.info(() -> String.format("Method [%s] took %s ms.", testMethod, duration));
		}

	}

}
