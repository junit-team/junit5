/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.DefaultTestInstances;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Unit tests for concrete implementations of {@link ExtensionContext}:
 * {@link JupiterEngineExtensionContext}, {@link ClassExtensionContext}, and
 * {@link MethodExtensionContext}.
 *
 * @since 5.0
 * @see org.junit.jupiter.engine.execution.ExtensionValuesStoreTests
 */
public class ExtensionContextTests {

	private final JupiterConfiguration configuration = mock();

	@BeforeEach
	void setUp() {
		when(configuration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());
		when(configuration.getDefaultExecutionMode()).thenReturn(ExecutionMode.SAME_THREAD);
		when(configuration.getDefaultClassesExecutionMode()).thenReturn(ExecutionMode.SAME_THREAD);
	}

	@Test
	void fromJupiterEngineDescriptor() {
		JupiterEngineDescriptor engineTestDescriptor = new JupiterEngineDescriptor(
			UniqueId.root("engine", "junit-jupiter"), configuration);

		JupiterEngineExtensionContext engineContext = new JupiterEngineExtensionContext(null, engineTestDescriptor,
			configuration, null);

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
			() -> assertThat(engineContext.getRoot()).isSameAs(engineContext),
			() -> assertThat(engineContext.getExecutionMode()).isEqualTo(ExecutionMode.SAME_THREAD)
		);
		// @formatter:on
	}

	@Test
	@SuppressWarnings("resource")
	void fromClassTestDescriptor() {
		NestedClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);

		ClassExtensionContext outerExtensionContext = new ClassExtensionContext(null, null, outerClassDescriptor,
			configuration, null, null);

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
			() -> assertThat(outerExtensionContext.getParent()).isEmpty(),
			() -> assertThat(outerExtensionContext.getExecutionMode()).isEqualTo(ExecutionMode.SAME_THREAD)
		);
		// @formatter:on

		ClassExtensionContext nestedExtensionContext = new ClassExtensionContext(outerExtensionContext, null,
			nestedClassDescriptor, configuration, null, null);
		assertThat(nestedExtensionContext.getParent()).containsSame(outerExtensionContext);
	}

	@Test
	@SuppressWarnings("resource")
	void tagsCanBeRetrievedInExtensionContext() {
		NestedClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);
		TestMethodTestDescriptor methodTestDescriptor = methodDescriptor();
		outerClassDescriptor.addChild(methodTestDescriptor);

		ClassExtensionContext outerExtensionContext = new ClassExtensionContext(null, null, outerClassDescriptor,
			configuration, null, null);

		assertThat(outerExtensionContext.getTags()).containsExactly("outer-tag");
		assertThat(outerExtensionContext.getRoot()).isSameAs(outerExtensionContext);

		ClassExtensionContext nestedExtensionContext = new ClassExtensionContext(outerExtensionContext, null,
			nestedClassDescriptor, configuration, null, null);
		assertThat(nestedExtensionContext.getTags()).containsExactlyInAnyOrder("outer-tag", "nested-tag");
		assertThat(nestedExtensionContext.getRoot()).isSameAs(outerExtensionContext);

		MethodExtensionContext methodExtensionContext = new MethodExtensionContext(outerExtensionContext, null,
			methodTestDescriptor, configuration, new OpenTest4JAwareThrowableCollector(), null);
		methodExtensionContext.setTestInstances(DefaultTestInstances.of(new OuterClass()));
		assertThat(methodExtensionContext.getTags()).containsExactlyInAnyOrder("outer-tag", "method-tag");
		assertThat(methodExtensionContext.getRoot()).isSameAs(outerExtensionContext);
	}

	@Test
	@SuppressWarnings("resource")
	void fromMethodTestDescriptor() {
		TestMethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);
		JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(UniqueId.forEngine("junit-jupiter"),
			configuration);
		engineDescriptor.addChild(classTestDescriptor);

		Object testInstance = new OuterClass();
		Method testMethod = methodTestDescriptor.getTestMethod();

		JupiterEngineExtensionContext engineExtensionContext = new JupiterEngineExtensionContext(null, engineDescriptor,
			configuration, null);
		ClassExtensionContext classExtensionContext = new ClassExtensionContext(engineExtensionContext, null,
			classTestDescriptor, configuration, null, null);
		MethodExtensionContext methodExtensionContext = new MethodExtensionContext(classExtensionContext, null,
			methodTestDescriptor, configuration, new OpenTest4JAwareThrowableCollector(), null);
		methodExtensionContext.setTestInstances(DefaultTestInstances.of(testInstance));

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
			() -> assertThat(methodExtensionContext.getRoot()).isSameAs(engineExtensionContext),
			() -> assertThat(methodExtensionContext.getExecutionMode()).isEqualTo(ExecutionMode.SAME_THREAD)
		);
		// @formatter:on
	}

	@Test
	@SuppressWarnings("resource")
	void reportEntriesArePublishedToExecutionContext() {
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(null);
		EngineExecutionListener engineExecutionListener = Mockito.spy(EngineExecutionListener.class);
		ExtensionContext extensionContext = new ClassExtensionContext(null, engineExecutionListener,
			classTestDescriptor, configuration, null, null);

		Map<String, String> map1 = Collections.singletonMap("key", "value");
		Map<String, String> map2 = Collections.singletonMap("other key", "other value");

		extensionContext.publishReportEntry(map1);
		extensionContext.publishReportEntry(map2);
		extensionContext.publishReportEntry("3rd key", "third value");
		extensionContext.publishReportEntry("status message");

		ArgumentCaptor<ReportEntry> entryCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		Mockito.verify(engineExecutionListener, Mockito.times(4)).reportingEntryPublished(
			ArgumentMatchers.eq(classTestDescriptor), entryCaptor.capture());

		ReportEntry reportEntry1 = entryCaptor.getAllValues().get(0);
		ReportEntry reportEntry2 = entryCaptor.getAllValues().get(1);
		ReportEntry reportEntry3 = entryCaptor.getAllValues().get(2);
		ReportEntry reportEntry4 = entryCaptor.getAllValues().get(3);

		assertEquals(map1, reportEntry1.getKeyValuePairs());
		assertEquals(map2, reportEntry2.getKeyValuePairs());
		assertEquals("third value", reportEntry3.getKeyValuePairs().get("3rd key"));
		assertEquals("status message", reportEntry4.getKeyValuePairs().get("value"));
	}

	@Test
	@SuppressWarnings("resource")
	void usingStore() {
		TestMethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);
		ExtensionContext parentContext = new ClassExtensionContext(null, null, classTestDescriptor, configuration, null,
			null);
		MethodExtensionContext childContext = new MethodExtensionContext(parentContext, null, methodTestDescriptor,
			configuration, new OpenTest4JAwareThrowableCollector(), null);
		childContext.setTestInstances(DefaultTestInstances.of(new OuterClass()));

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

	@TestFactory
	Stream<DynamicTest> configurationParameter() throws Exception {
		JupiterConfiguration echo = new DefaultJupiterConfiguration(new EchoParameters());
		String key = "123";
		Optional<String> expected = Optional.of(key);

		UniqueId engineUniqueId = UniqueId.parse("[engine:junit-jupiter]");
		JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(engineUniqueId, configuration);

		UniqueId classUniqueId = UniqueId.parse("[engine:junit-jupiter]/[class:MyClass]");
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(classUniqueId, getClass(), configuration);

		Method method = getClass().getDeclaredMethod("configurationParameter");
		UniqueId methodUniqueId = UniqueId.parse("[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]");
		TestMethodTestDescriptor methodTestDescriptor = new TestMethodTestDescriptor(methodUniqueId, getClass(), method,
			configuration);

		return Stream.of( //
			(ExtensionContext) new JupiterEngineExtensionContext(null, engineDescriptor, echo, null), //
			new ClassExtensionContext(null, null, classTestDescriptor, echo, null, null), //
			new MethodExtensionContext(null, null, methodTestDescriptor, echo, null, null) //
		).map(context -> dynamicTest(context.getClass().getSimpleName(),
			() -> assertEquals(expected, context.getConfigurationParameter(key))));
	}

	private NestedClassTestDescriptor nestedClassDescriptor() {
		return new NestedClassTestDescriptor(UniqueId.root("nested-class", "NestedClass"), OuterClass.NestedClass.class,
			configuration);
	}

	private ClassTestDescriptor outerClassDescriptor(TestDescriptor child) {
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(UniqueId.root("class", "OuterClass"),
			OuterClass.class, configuration);
		if (child != null) {
			classTestDescriptor.addChild(child);
		}
		return classTestDescriptor;
	}

	private TestMethodTestDescriptor methodDescriptor() {
		try {
			return new TestMethodTestDescriptor(UniqueId.root("method", "aMethod"), OuterClass.class,
				OuterClass.class.getDeclaredMethod("aMethod"), configuration);
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

	private static class EchoParameters implements ConfigurationParameters {

		@Override
		public Optional<String> get(String key) {
			return Optional.of(key);
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			throw new UnsupportedOperationException("getBoolean(String) should not be called");
		}

		@Override
		@SuppressWarnings("deprecation")
		public int size() {
			throw new UnsupportedOperationException("size() should not be called");
		}

		@Override
		public Set<String> keySet() {
			throw new UnsupportedOperationException("keySet() should not be called");

		}
	}

}
