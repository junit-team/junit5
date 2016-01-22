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

import static org.junit.gen5.api.Assertions.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.commons.reporting.ReportEntry;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ClassBasedContainerExtensionContext;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodBasedTestExtensionContext;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.mockito.Mockito;

/**
 * Microtests for implementors of {@linkplain ExtensionContext}: {@linkplain ClassBasedContainerExtensionContext} and
 * {@linkplain MethodBasedTestExtensionContext}
 */
public class ExtensionContextTests {

	@Test
	public void fromClassTestDescriptor() {
		ClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);

		ClassBasedContainerExtensionContext outerExtensionContext = new ClassBasedContainerExtensionContext(null, null,
			outerClassDescriptor);
		Assertions.assertAll("outerContext", //
			() -> assertEquals(OuterClass.class, outerExtensionContext.getTestClass()), //
			() -> assertEquals(outerClassDescriptor.getDisplayName(), outerExtensionContext.getDisplayName()), //
			() -> assertEquals(Optional.empty(), outerExtensionContext.getParent()) //
		);

		ClassBasedContainerExtensionContext nestedExtensionContext = new ClassBasedContainerExtensionContext(
			outerExtensionContext, null, nestedClassDescriptor);
		Assertions.assertSame(outerExtensionContext, nestedExtensionContext.getParent().get());
	}

	@Test
	public void fromMethodTestDescriptor() {
		MethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);

		ClassBasedContainerExtensionContext classExtensionContext = new ClassBasedContainerExtensionContext(null, null,
			classTestDescriptor);
		MethodBasedTestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(
			classExtensionContext, null, methodTestDescriptor, new OuterClass());
		Assertions.assertAll("methodContext", //
			() -> assertEquals(OuterClass.class, testExtensionContext.getTestClass()), //
			() -> assertEquals(methodTestDescriptor.getDisplayName(), testExtensionContext.getDisplayName()), //
			() -> assertEquals(classExtensionContext, testExtensionContext.getParent().get()), //
			() -> assertEquals(OuterClass.class, testExtensionContext.getTestInstance().getClass()) //
		);
	}

	@Test
	public void reportEntriesArePublishedToExecutionContext() {
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(null);

		EngineExecutionListener engineExecutionListener = Mockito.spy(EngineExecutionListener.class);

		ExtensionContext extensionContext = new ClassBasedContainerExtensionContext(null, engineExecutionListener,
			classTestDescriptor);

		ReportEntry entry1 = ReportEntry.from(Collections.emptyMap());
		ReportEntry entry2 = ReportEntry.from(Collections.singletonMap("key", "value"));

		extensionContext.publishReportEntry(entry1);
		extensionContext.publishReportEntry(entry2);

		Mockito.verify(engineExecutionListener, Mockito.times(1)).reportingEntryPublished(classTestDescriptor, entry1);
		Mockito.verify(engineExecutionListener, Mockito.times(1)).reportingEntryPublished(classTestDescriptor, entry2);
	}

	@Test
	public void usingStore() {
		MethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);
		ExtensionContext parentContext = new ClassBasedContainerExtensionContext(null, null, classTestDescriptor);
		MethodBasedTestExtensionContext childContext = new MethodBasedTestExtensionContext(parentContext, null,
			methodTestDescriptor, new OuterClass());

		ExtensionContext.Store childStore = childContext.getStore();
		ExtensionContext.Store parentStore = parentContext.getStore();

		final Object key1 = "key 1";
		final String value1 = "a value";
		childStore.put(key1, value1);
		assertEquals(value1, childStore.get(key1));
		assertEquals(value1, childStore.remove(key1));
		assertNull(childStore.get(key1));

		final Object key2 = "key 2";
		final String value2 = "other value";
		assertEquals(value2, childStore.getOrComputeIfAbsent(key2, key -> value2));
		assertEquals(value2, childStore.get(key2));

		final Object parentKey = "parent key";
		final String parentValue = "parent value";
		parentStore.put(parentKey, parentValue);
		assertEquals(parentValue, childStore.get(parentKey));
	}

	private ClassTestDescriptor nestedClassDescriptor() {
		return new ClassTestDescriptor("NestedClass", OuterClass.NestedClass.class);
	}

	private ClassTestDescriptor outerClassDescriptor(TestDescriptor child) {
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor("OuterClass", OuterClass.class);
		if (child != null)
			classTestDescriptor.addChild(child);
		return classTestDescriptor;
	}

	private MethodTestDescriptor methodDescriptor() {
		try {
			return new MethodTestDescriptor("aMethod", OuterClass.class, OuterClass.class.getDeclaredMethod("aMethod"));
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	static class OuterClass {

		class NestedClass {

		}

		void aMethod() {

		}
	}

}
