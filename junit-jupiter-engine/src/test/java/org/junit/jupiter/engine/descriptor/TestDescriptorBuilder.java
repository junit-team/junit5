/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.0
 */
public abstract class TestDescriptorBuilder<T extends TestDescriptor> {

	private static final ConfigurationParameters configParams = mock(ConfigurationParameters.class);
	final List<TestDescriptorBuilder<?>> children = new ArrayList<>();

	public static JupiterEngineDescriptorBuilder engineDescriptor() {
		return new JupiterEngineDescriptorBuilder();
	}

	public static ClassTestDescriptorBuilder classTestDescriptor(String uniqueId, Class<?> testClass) {
		return new ClassTestDescriptorBuilder(uniqueId, testClass);
	}

	public static NestedClassTestDescriptorBuilder nestedClassTestDescriptor(String uniqueId, Class<?> testClass) {
		return new NestedClassTestDescriptorBuilder(uniqueId, testClass);
	}

	public T build() {
		T testDescriptor = buildDescriptor();
		children.forEach(builder -> testDescriptor.addChild(builder.build()));
		return testDescriptor;
	}

	public TestDescriptorBuilder<?> with(TestDescriptorBuilder<?>... children) {
		Collections.addAll(this.children, children);
		return this;
	}

	abstract T buildDescriptor();

	public static class JupiterEngineDescriptorBuilder extends TestDescriptorBuilder<JupiterEngineDescriptor> {

		@Override
		JupiterEngineDescriptor buildDescriptor() {
			return new JupiterEngineDescriptor(UniqueId.forEngine("junit-jupiter"));
		}
	}

	public static class ClassTestDescriptorBuilder extends TestDescriptorBuilder<ClassTestDescriptor> {

		protected final String uniqueId;
		protected final Class<?> testClass;

		public ClassTestDescriptorBuilder(String uniqueId, Class<?> testClass) {
			this.uniqueId = uniqueId;
			this.testClass = testClass;
		}

		@Override
		ClassTestDescriptor buildDescriptor() {
			return new ClassTestDescriptor(UniqueId.root("class", uniqueId), testClass, configParams);
		}
	}

	public static class NestedClassTestDescriptorBuilder extends ClassTestDescriptorBuilder {

		public NestedClassTestDescriptorBuilder(String uniqueId, Class<?> testClass) {
			super(uniqueId, testClass);
		}

		@Override
		NestedClassTestDescriptor buildDescriptor() {
			return new NestedClassTestDescriptor(UniqueId.root("nested-class", uniqueId), testClass, configParams);
		}
	}

}
