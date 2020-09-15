/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.CombineTestTemplates;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.mockito.Answers;

/**
 * Unit tests for {@link TestTemplateTestDescriptor}.
 *
 * @since 5.0
 */
class TestTemplateTestDescriptorTests {
	private JupiterConfiguration jupiterConfiguration = mock(JupiterConfiguration.class);

	@Test
	void inheritsTagsFromParent() throws Exception {
		UniqueId rootUniqueId = UniqueId.root("segment", "template");
		UniqueId parentUniqueId = rootUniqueId.append("class", "myClass");
		AbstractTestDescriptor parent = containerTestDescriptorWithTags(parentUniqueId,
			singleton(TestTag.create("foo")));

		when(jupiterConfiguration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());

		TestTemplateTestDescriptor testDescriptor = new TestTemplateTestDescriptor(
			parentUniqueId.append("tmp", "testTemplate()"), MyTestCase.class,
			MyTestCase.class.getDeclaredMethod("testTemplate"), jupiterConfiguration);
		parent.addChild(testDescriptor);

		assertThat(testDescriptor.getTags()).containsExactlyInAnyOrder(TestTag.create("foo"), TestTag.create("bar"),
			TestTag.create("baz"));
	}

	@Test
	void shouldUseCustomDisplayNameGeneratorIfPresentFromConfiguration() throws Exception {
		UniqueId rootUniqueId = UniqueId.root("segment", "template");
		UniqueId parentUniqueId = rootUniqueId.append("class", "myClass");
		AbstractTestDescriptor parent = containerTestDescriptorWithTags(parentUniqueId,
			singleton(TestTag.create("foo")));

		when(jupiterConfiguration.getDefaultDisplayNameGenerator()).thenReturn(new CustomDisplayNameGenerator());

		TestTemplateTestDescriptor testDescriptor = new TestTemplateTestDescriptor(
			parentUniqueId.append("tmp", "testTemplate()"), MyTestCase.class,
			MyTestCase.class.getDeclaredMethod("testTemplate"), jupiterConfiguration);
		parent.addChild(testDescriptor);

		assertThat(testDescriptor.getDisplayName()).isEqualTo("method-display-name");
	}

	@Test
	void shouldUseStandardDisplayNameGeneratorIfConfigurationNotPresent() throws Exception {
		UniqueId rootUniqueId = UniqueId.root("segment", "template");
		UniqueId parentUniqueId = rootUniqueId.append("class", "myClass");
		AbstractTestDescriptor parent = containerTestDescriptorWithTags(parentUniqueId,
			singleton(TestTag.create("foo")));

		when(jupiterConfiguration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());

		TestTemplateTestDescriptor testDescriptor = new TestTemplateTestDescriptor(
			parentUniqueId.append("tmp", "testTemplate()"), MyTestCase.class,
			MyTestCase.class.getDeclaredMethod("testTemplate"), jupiterConfiguration);
		parent.addChild(testDescriptor);

		assertThat(testDescriptor.getDisplayName()).isEqualTo("testTemplate()");
	}

	@Test
	public void standardTestExecutionModeExecutesTheTestInTheContextOfTheTestTemplate() throws Exception {
		UniqueId rootUniqueId = UniqueId.root("segment", "template");
		UniqueId parentUniqueId = rootUniqueId.append("class", "myClass");
		AbstractTestDescriptor parent = containerTestDescriptorWithTags(parentUniqueId,
			singleton(TestTag.create("foo")));

		when(jupiterConfiguration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());

		Method testMethod = MyTestCase.class.getDeclaredMethod("testTemplate");
		TestTemplateTestDescriptor testDescriptor = new TestTemplateTestDescriptor(
			parentUniqueId.append("tmp", "testTemplate()"), MyTestCase.class, testMethod, jupiterConfiguration);
		parent.addChild(testDescriptor);

		ExtensionContext extensionContext = mock(ExtensionContext.class, Answers.RETURNS_MOCKS);
		when(extensionContext.getElement()).thenReturn(Optional.of(testMethod));
		MutableExtensionRegistry extensionRegistry = MutableExtensionRegistry.createRegistryWithDefaultExtensions(
			mock(JupiterConfiguration.class));
		extensionRegistry.registerExtension(new MyTestTemplateProvider(), new Object());
		extensionRegistry.registerExtension(new MyOtherTestTemplateProvider(), new Object());
		JupiterEngineExecutionContext context = new JupiterEngineExecutionContext(mock(EngineExecutionListener.class),
			mock(JupiterConfiguration.class)).extend().withExtensionContext(extensionContext).withExtensionRegistry(
				extensionRegistry).build();

		Node.DynamicTestExecutor executor = mock(Node.DynamicTestExecutor.class);
		List<MutableExtensionRegistry> registriedForTests = new ArrayList<>();
		doAnswer(invocation -> {
			MutableExtensionRegistry registry = invocation.getArgument(0,
				TestTemplateInvocationTestDescriptor.class).populateNewExtensionRegistry(context);
			registriedForTests.add(registry);
			return registry;
		}).when(executor).execute(any());

		testDescriptor.execute(context, executor);

		List<Extension> extensions = registriedForTests.get(0).getExtensions(Extension.class);
		assertThat(extensions).contains(MyTestTemplateProvider.EXTENSION);

		List<Extension> extensions2 = registriedForTests.get(1).getExtensions(Extension.class);
		assertThat(extensions2).contains(MyOtherTestTemplateProvider.EXTENSION);
	}

	@Test
	public void combinedTestExecutionModeExecutesTheTestInTheContextOfTheTestTemplate() throws Exception {
		UniqueId rootUniqueId = UniqueId.root("segment", "template");
		UniqueId parentUniqueId = rootUniqueId.append("class", "myClass");
		AbstractTestDescriptor parent = containerTestDescriptorWithTags(parentUniqueId,
			singleton(TestTag.create("foo")));

		when(jupiterConfiguration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());

		Method testMethod = MyCombinedTestCase.class.getDeclaredMethod("testTemplate");
		TestTemplateTestDescriptor testDescriptor = new TestTemplateTestDescriptor(
			parentUniqueId.append("tmp", "testTemplate()"), MyCombinedTestCase.class, testMethod, jupiterConfiguration);
		parent.addChild(testDescriptor);

		ExtensionContext extensionContext = mock(ExtensionContext.class, Answers.RETURNS_MOCKS);
		when(extensionContext.getElement()).thenReturn(Optional.of(testMethod));
		MutableExtensionRegistry extensionRegistry = MutableExtensionRegistry.createRegistryWithDefaultExtensions(
			mock(JupiterConfiguration.class));
		extensionRegistry.registerExtension(new MyTestTemplateProvider(), new Object());
		extensionRegistry.registerExtension(new MyOtherTestTemplateProvider(), new Object());
		JupiterEngineExecutionContext context = new JupiterEngineExecutionContext(mock(EngineExecutionListener.class),
			mock(JupiterConfiguration.class)).extend().withExtensionContext(extensionContext).withExtensionRegistry(
				extensionRegistry).build();

		Node.DynamicTestExecutor executor = mock(Node.DynamicTestExecutor.class);
		List<MutableExtensionRegistry> registriedForTests = new ArrayList<>();
		doAnswer(invocation -> {
			MutableExtensionRegistry registry = invocation.getArgument(0,
				TestTemplateInvocationTestDescriptor.class).populateNewExtensionRegistry(context);
			registriedForTests.add(registry);
			return registry;
		}).when(executor).execute(any());

		testDescriptor.execute(context, executor);

		List<Extension> extensions = registriedForTests.get(0).getExtensions(Extension.class);
		assertThat(extensions).contains(MyTestTemplateProvider.EXTENSION, MyOtherTestTemplateProvider.EXTENSION);
	}

	private AbstractTestDescriptor containerTestDescriptorWithTags(UniqueId uniqueId, Set<TestTag> tags) {
		return new AbstractTestDescriptor(uniqueId, "testDescriptor with tags") {

			@Override
			public Type getType() {
				return Type.CONTAINER;
			}

			@Override
			public Set<TestTag> getTags() {
				return tags;
			}
		};
	}

	static class MyTestCase {

		@Tag("bar")
		@Tag("baz")
		@TestTemplate
		@ExtendWith(MyTestTemplateProvider.class)
		@ExtendWith(MyOtherTestTemplateProvider.class)
		void testTemplate() {
		}
	}

	static class MyCombinedTestCase {
		@Tag("bar")
		@Tag("baz")
		@TestTemplate
		@ExtendWith(MyTestTemplateProvider.class)
		@ExtendWith(MyOtherTestTemplateProvider.class)
		@CombineTestTemplates
		void testTemplate() {
		}
	}

	static class MyTestTemplateProvider implements TestTemplateInvocationContextProvider {
		public static final Extension EXTENSION = new Extension() {
		};

		@Override
		public boolean supportsTestTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
			return Stream.of(new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return "Test";
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return Collections.singletonList(EXTENSION);
				}
			});
		}
	}

	static class MyOtherTestTemplateProvider implements TestTemplateInvocationContextProvider {
		public static final Extension EXTENSION = new Extension() {
		};

		@Override
		public boolean supportsTestTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
			return Stream.of(new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return "Test2";
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return Collections.singletonList(EXTENSION);
				}
			});
		}
	}
}
