/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.testkit.engine.EventConditions.reportEntry;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EventType;
import org.junit.platform.testkit.engine.Events;

public class CloseableResourceIntegrationTests extends AbstractJupiterTestEngineTests {

	@Test
	void closesCloseableResourcesInReverseInsertOrder() {
		executeTestsForClass(TestCase.class).allEvents().reportingEntryPublished() //
				.assertEventsMatchExactly( //
					reportEntry(Map.of("3", "closed")), //
					reportEntry(Map.of("2", "closed")), //
					reportEntry(Map.of("1", "closed")));
	}

	@ExtendWith(ExtensionContextParameterResolver.class)
	static class TestCase {
		@Test
		void closesCloseableResourcesInExtensionContext(ExtensionContext extensionContext) {
			ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
			store.put("foo", reportEntryOnClose(extensionContext, "1"));
			store.put("bar", reportEntryOnClose(extensionContext, "2"));
			store.put("baz", reportEntryOnClose(extensionContext, "3"));
		}

		private ExtensionContext.Store.CloseableResource reportEntryOnClose(ExtensionContext extensionContext,
				String key) {
			return () -> extensionContext.publishReportEntry(Map.of(key, "closed"));
		}
	}


	@Test
	void testExceptionInOnCloseableResource() {
		executeTestsForClass(ExceptionInCloseableResourceTestCase.class).testEvents()
				.assertThatEvents()
				.anySatisfy(e -> {
					assertThat(e.getType()).isEqualTo(EventType.FINISHED);
					TestExecutionResult result = e.getPayload(TestExecutionResult.class).get();
					assertThat(result.getStatus()).isEqualTo(TestExecutionResult.Status.FAILED);
					assertThat(result.getThrowable()).isPresent()
							.hasValueSatisfying(t -> assertThat(t)
									.hasMessageContaining("Exception in Test.")
									.isInstanceOf(RuntimeException.class)
							);
					assertThat(result.getThrowable().get().getSuppressed())
							.hasSize(1)
							.anySatisfy(t -> assertThat(t).hasMessageContaining("Exception in onClose"));
				});
	}


	@ExtendWith(ThrowingOnCloseExtension.class)
	static class ExceptionInCloseableResourceTestCase {

		@Test
		void test() {
			throw new RuntimeException("Exception in Test.");
		}

	}


	static class ThrowingOnCloseExtension implements BeforeEachCallback {

		private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ThrowingOnCloseExtension.class);

		@Override
		public void beforeEach(ExtensionContext context) {
			context.getStore(NAMESPACE).put("throwingResource", (ExtensionContext.Store.CloseableResource) () -> {
				throw new RuntimeException("Exception in onClose");
			});
		}
	}

}
