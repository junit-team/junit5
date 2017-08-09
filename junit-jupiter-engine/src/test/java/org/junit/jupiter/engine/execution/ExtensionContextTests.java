/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.engine.descriptor.ClassExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineExtensionContext;
import org.junit.jupiter.engine.descriptor.MethodExtensionContext;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Unit tests for concrete implementations of {@link ExtensionContext}:
 * {@link JupiterEngineExtensionContext}, {@link ClassExtensionContext}, and
 * {@link MethodExtensionContext}.
 *
 * @since 5.0
 * @see ExtensionValuesStoreTests
 */
class ExtensionContextTests {

	@Test
	void fromJupiterEngineDescriptor() {
		JupiterEngineDescriptor engineTestDescriptor = new JupiterEngineDescriptor(
			UniqueId.root("engine", "junit-jupiter"));

		JupiterEngineExtensionContext engineContext = new JupiterEngineExtensionContext(null, engineTestDescriptor);

		// @formatter:off
		assertAll("engineContext",
			() -> assertThat(engineContext.getElement()).isEmpty(),
			() -> assertThat(engineContext.getTestClass()).isEmpty(),
			() -> assertThat(engineContext.getTestInstance()).isEmpty(),
			() -> assertThat(engineContext.getTestMethod()).isEmpty(),
			() -> assertThrows(PreconditionViolationException.class, () -> engineContext.getRequiredTestClass()),
			() -> assertThrows(PreconditionViolationException.class, () -> engineContext.getRequiredTestInstance()),
			() -> assertThrows(PreconditionViolationException.class, () -> engineContext.getRequiredTestMethod()),
			() -> assertThat(engineContext.getDisplayName()).isEqualTo(engineTestDescriptor.getDisplayName()),
			() -> assertThat(engineContext.getParent()).isEmpty(),
			() -> assertThat(engineContext.getRoot()).isSameAs(engineContext)
		);
		// @formatter:on
	}

	@Test
	void fromClassTestDescriptor() {
		ClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);

		ClassExtensionContext outerExtensionContext = new ClassExtensionContext(null, null, outerClassDescriptor, null);

		// @formatter:off
		assertAll("outerContext",
			() -> assertThat(outerExtensionContext.getElement()).contains(OuterClass.class),
			() -> assertThat(outerExtensionContext.getTestClass()).contains(OuterClass.class),
			() -> assertThat(outerExtensionContext.getTestInstance()).isEmpty(),
			() -> assertThat(outerExtensionContext.getTestMethod()).isEmpty(),
			() -> assertThat(outerExtensionContext.getRequiredTestClass()).isEqualTo(OuterClass.class),
			() -> assertThrows(PreconditionViolationException.class, () -> outerExtensionContext.getRequiredTestInstance()),
			() -> assertThrows(PreconditionViolationException.class, () -> outerExtensionContext.getRequiredTestMethod()),
			() -> assertThat(outerExtensionContext.getDisplayName()).isEqualTo(outerClassDescriptor.getDisplayName()),
			() -> assertThat(outerExtensionContext.getParent()).isEmpty()
		);
		// @formatter:on

		ClassExtensionContext nestedExtensionContext = new ClassExtensionContext(outerExtensionContext, null,
			nestedClassDescriptor, null);
		assertThat(nestedExtensionContext.getParent()).containsSame(outerExtensionContext);
	}

	@Test
	void tagsCanBeRetrievedInExtensionContext() {
		ClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);
		TestMethodTestDescriptor methodTestDescriptor = methodDescriptor();
		outerClassDescriptor.addChild(methodTestDescriptor);

		ClassExtensionContext outerExtensionContext = new ClassExtensionContext(null, null, outerClassDescriptor, null);

		assertThat(outerExtensionContext.getTags()).containsExactly("outer-tag");
		assertThat(outerExtensionContext.getRoot()).isSameAs(outerExtensionContext);

		ClassExtensionContext nestedExtensionContext = new ClassExtensionContext(outerExtensionContext, null,
			nestedClassDescriptor, null);
		assertThat(nestedExtensionContext.getTags()).containsExactlyInAnyOrder("outer-tag", "nested-tag");
		assertThat(nestedExtensionContext.getRoot()).isSameAs(outerExtensionContext);

		MethodExtensionContext methodExtensionContext = new MethodExtensionContext(outerExtensionContext, null,
			methodTestDescriptor, new OuterClass(), new ThrowableCollector());
		assertThat(methodExtensionContext.getTags()).containsExactlyInAnyOrder("outer-tag", "method-tag");
		assertThat(methodExtensionContext.getRoot()).isSameAs(outerExtensionContext);
	}

	@Test
	void fromMethodTestDescriptor() {
		TestMethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);

		Object testInstance = new OuterClass();
		Method testMethod = methodTestDescriptor.getTestMethod();

		ClassExtensionContext classExtensionContext = new ClassExtensionContext(null, null, classTestDescriptor, null);
		MethodExtensionContext methodExtensionContext = new MethodExtensionContext(classExtensionContext, null,
			methodTestDescriptor, testInstance, new ThrowableCollector());

		// @formatter:off
		assertAll("methodContext",
			() -> assertThat(methodExtensionContext.getElement()).contains(testMethod),
			() -> assertThat(methodExtensionContext.getTestClass()).contains(OuterClass.class),
			() -> assertThat(methodExtensionContext.getTestInstance()).contains(testInstance),
			() -> assertThat(methodExtensionContext.getTestMethod()).contains(testMethod),
			() -> assertThat(methodExtensionContext.getRequiredTestClass()).isEqualTo(OuterClass.class),
			() -> assertThat(methodExtensionContext.getRequiredTestInstance()).isEqualTo(testInstance),
			() -> assertThat(methodExtensionContext.getRequiredTestMethod()).isEqualTo(testMethod),
			() -> assertThat(methodExtensionContext.getDisplayName()).isEqualTo(methodTestDescriptor.getDisplayName()),
			() -> assertThat(methodExtensionContext.getParent()).contains(classExtensionContext),
			() -> assertThat(methodExtensionContext.getRoot()).isSameAs(classExtensionContext)
		);
		// @formatter:on
	}

	@Test
	void reportEntriesArePublishedToExecutionContext() {
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(null);
		EngineExecutionListener engineExecutionListener = Mockito.spy(EngineExecutionListener.class);
		ExtensionContext extensionContext = new ClassExtensionContext(null, engineExecutionListener,
			classTestDescriptor, null);

		Map<String, String> map1 = Collections.singletonMap("key", "value");
		Map<String, String> map2 = Collections.singletonMap("other key", "other value");

		extensionContext.publishReportEntry(map1);
		extensionContext.publishReportEntry(map2);
		extensionContext.publishReportEntry("3rd key", "third value");

		ArgumentCaptor<ReportEntry> entryCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		Mockito.verify(engineExecutionListener, Mockito.times(3)).reportingEntryPublished(
			Mockito.eq(classTestDescriptor), entryCaptor.capture());

		ReportEntry reportEntry1 = entryCaptor.getAllValues().get(0);
		ReportEntry reportEntry2 = entryCaptor.getAllValues().get(1);
		ReportEntry reportEntry3 = entryCaptor.getAllValues().get(2);

		assertEquals(map1, reportEntry1.getKeyValuePairs());
		assertEquals(map2, reportEntry2.getKeyValuePairs());
		assertEquals("third value", reportEntry3.getKeyValuePairs().get("3rd key"));
	}

	@Test
	void usingStore() {
		TestMethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);
		ExtensionContext parentContext = new ClassExtensionContext(null, null, classTestDescriptor, null);
		MethodExtensionContext childContext = new MethodExtensionContext(parentContext, null, methodTestDescriptor,
			new OuterClass(), new ThrowableCollector());

		ExtensionContext.Store childStore = childContext.getStore(Namespace.GLOBAL);
		ExtensionContext.Store parentStore = parentContext.getStore(Namespace.GLOBAL);

		final Object key1 = "key 1";
		final String value1 = "a value";
		childStore.put(key1, value1);
		assertEquals(value1, childStore.get(key1));
		assertEquals(value1, childStore.remove(key1));
		assertNull(childStore.get(key1));

		childStore.put(key1, value1);
		assertEquals(value1, childStore.get(key1));
		assertEquals(value1, childStore.remove(key1, String.class));
		assertNull(childStore.get(key1));

		final Object key2 = "key 2";
		final String value2 = "other value";
		assertEquals(value2, childStore.getOrComputeIfAbsent(key2, key -> value2));
		assertEquals(value2, childStore.getOrComputeIfAbsent(key2, key -> value2, String.class));
		assertEquals(value2, childStore.get(key2));
		assertEquals(value2, childStore.get(key2, String.class));

		final Object parentKey = "parent key";
		final String parentValue = "parent value";
		parentStore.put(parentKey, parentValue);
		assertEquals(parentValue, childStore.getOrComputeIfAbsent(parentKey, k -> "a different value"));
		assertEquals(parentValue, childStore.get(parentKey));
	}

	private ClassTestDescriptor nestedClassDescriptor() {
		return new NestedClassTestDescriptor(UniqueId.root("nested-class", "NestedClass"),
			OuterClass.NestedClass.class);
	}

	private ClassTestDescriptor outerClassDescriptor(TestDescriptor child) {
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(UniqueId.root("class", "OuterClass"),
			OuterClass.class);
		if (child != null) {
			classTestDescriptor.addChild(child);
		}
		return classTestDescriptor;
	}

	private TestMethodTestDescriptor methodDescriptor() {
		try {
			return new TestMethodTestDescriptor(UniqueId.root("method", "aMethod"), OuterClass.class,
				OuterClass.class.getDeclaredMethod("aMethod"));
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Tag("outer-tag")
	static class OuterClass {

		@Tag("nested-tag")
		class NestedClass {
		}

		@Tag("method-tag")
		void aMethod() {
		}
	}

}
