/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

class ExtensionContextExecutionTests extends AbstractJupiterTestEngineTests {

	@Test
	@ExtendWith(ExtensionContextParameterResolver.class)
	void extensionContextHierarchy(ExtensionContext testExtensionContext) {
		assertThat(testExtensionContext).isInstanceOf(TestExtensionContext.class);

		Optional<ExtensionContext> classExtensionContext = testExtensionContext.getParent();
		assertThat(classExtensionContext).containsInstanceOf(ContainerExtensionContext.class);

		Optional<ExtensionContext> engineExtensionContext = classExtensionContext.orElse(null).getParent();
		assertThat(engineExtensionContext).containsInstanceOf(ContainerExtensionContext.class);

		assertThat(engineExtensionContext.orElse(null).getParent()).isEmpty();
	}

	static class ExtensionContextParameterResolver implements ParameterResolver {
		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return ExtensionContext.class.equals(parameterContext.getParameter().getType());
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return extensionContext;
		}
	}

	@Test
	void twoTestClassesCanShareStateViaEngineExtensionContext() {
		Parent.counter.set(0);

		ExecutionEventRecorder eventRecorder = executeTests(
			request().selectors(selectClass(A.class), selectClass(B.class)).build());

		assertThat(eventRecorder.getTestFinishedCount()).isEqualTo(2);
		assertThat(Parent.counter).hasValue(1);
	}

	@ExtendWith(OnlyIncrementCounterOnce.class)
	static class Parent {
		static final AtomicInteger counter = new AtomicInteger(0);

		@Test
		void test() {
		}
	}

	static class A extends Parent {
	}

	static class B extends Parent {
	}

	static class OnlyIncrementCounterOnce implements BeforeAllCallback {
		@Override
		public void beforeAll(ContainerExtensionContext context) throws Exception {
			getRoot(context).getStore().getOrComputeIfAbsent("counter", key -> Parent.counter.incrementAndGet());
		}

		private ExtensionContext getRoot(ExtensionContext context) {
			return context.getParent().map(this::getRoot).orElse(context);
		}
	}

}
