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

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineExtensionContext;
import org.junit.jupiter.engine.descriptor.MethodExtensionContext;
import org.junit.jupiter.engine.descriptor.MethodTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Microtests for implementors of {@linkplain ExtensionContext}:
 * {@linkplain JupiterEngineExtensionContext},
 * {@linkplain ClassExtensionContext}, and
 * {@linkplain MethodExtensionContext}.
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

		assertAll("engineContext", //
			() -> assertThat(engineContext.getTestClass()).isEmpty(), //
			() -> assertThat(engineContext.getTestMethod()).isEmpty(), //
			() -> assertThat(engineContext.getElement()).isEmpty(), //
			() -> assertThat(engineContext.getDisplayName()).isEqualTo(engineTestDescriptor.getDisplayName()), //
			() -> assertThat(engineContext.getParent()).isEmpty() //
		);
	}

	@Test
	void fromClassTestDescriptor() {
		ClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);

		ClassExtensionContext outerExtensionContext = new ClassExtensionContext(null, null, outerClassDescriptor, null);
		assertAll("outerContext", //
			() -> assertThat(outerExtensionContext.getTestClass()).contains(OuterClass.class), //
			() -> assertThat(outerExtensionContext.getDisplayName()).isEqualTo(outerClassDescriptor.getDisplayName()), //
			() -> assertThat(outerExtensionContext.getParent()).isEmpty() //
		);

		ClassExtensionContext nestedExtensionContext = new ClassExtensionContext(outerExtensionContext, null,
			nestedClassDescriptor, null);
		assertThat(nestedExtensionContext.getParent()).containsSame(outerExtensionContext);
	}

	@Test
	void tagsCanBeRetrievedInExtensionContext() {
		ClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);
		MethodTestDescriptor methodTestDescriptor = methodDescriptor();
		outerClassDescriptor.addChild(methodTestDescriptor);

		ClassExtensionContext outerExtensionContext = new ClassExtensionContext(null, null, outerClassDescriptor, null);

		assertThat(outerExtensionContext.getTags()).containsExactly("outer-tag");

		ClassExtensionContext nestedExtensionContext = new ClassExtensionContext(outerExtensionContext, null,
			nestedClassDescriptor, null);
		assertThat(nestedExtensionContext.getTags()).containsExactlyInAnyOrder("outer-tag", "nested-tag");

		MethodExtensionContext methodExtensionContext = new MethodExtensionContext(outerExtensionContext, null,
			methodTestDescriptor, new ThrowableCollector());
		assertThat(methodExtensionContext.getTags()).containsExactlyInAnyOrder("outer-tag", "method-tag");
	}

	@Test
	void fromMethodTestDescriptor() {
		MethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);

		ClassExtensionContext classExtensionContext = new ClassExtensionContext(null, null, classTestDescriptor, null);
		MethodExtensionContext methodExtensionContext = new MethodExtensionContext(classExtensionContext, null,
			methodTestDescriptor, new ThrowableCollector());
		methodExtensionContext.setTestInstance(new OuterClass());
		assertAll("methodContext", //
			() -> assertThat(methodExtensionContext.getTestClass()).contains(OuterClass.class), //
			() -> assertThat(methodExtensionContext.getDisplayName()).isEqualTo(methodTestDescriptor.getDisplayName()), //
			() -> assertThat(methodExtensionContext.getParent()).contains(classExtensionContext), //
			() -> assertThat(methodExtensionContext.getTestInstance().get()).isExactlyInstanceOf(OuterClass.class) //
		);
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

		ArgumentCaptor<ReportEntry> entryCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		Mockito.verify(engineExecutionListener, Mockito.times(2)).reportingEntryPublished(
			Mockito.eq(classTestDescriptor), entryCaptor.capture());

		ReportEntry reportEntry1 = entryCaptor.getAllValues().get(0);
		ReportEntry reportEntry2 = entryCaptor.getAllValues().get(1);

		assertEquals(map1, reportEntry1.getKeyValuePairs());
		assertEquals(map2, reportEntry2.getKeyValuePairs());
	}

	@Test
	void usingStore() {
		MethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);
		AbstractExtensionContext<?> parentContext = new ClassExtensionContext(null, null, classTestDescriptor, null);
		MethodExtensionContext childContext = new MethodExtensionContext(parentContext, null, methodTestDescriptor,
			new ThrowableCollector());

		ExtensionContext.Store childStore = childContext.getStore();
		ExtensionContext.Store parentStore = parentContext.getStore();

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

	private MethodTestDescriptor methodDescriptor() {
		try {
			return new MethodTestDescriptor(UniqueId.root("method", "aMethod"), OuterClass.class,
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
