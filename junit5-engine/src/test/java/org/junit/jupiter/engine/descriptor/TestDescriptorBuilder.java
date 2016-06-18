/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;

/**
 * @since 5.0
 */
public abstract class TestDescriptorBuilder<T extends TestDescriptor> {

	final List<TestDescriptorBuilder<?>> children = new ArrayList<>();

	public static JUnit5EngineDescriptorBuilder engineDescriptor() {
		return new JUnit5EngineDescriptorBuilder();
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
		this.children.addAll(Arrays.asList(children));
		return this;
	}

	abstract T buildDescriptor();

	public static class JUnit5EngineDescriptorBuilder extends TestDescriptorBuilder<JUnit5EngineDescriptor> {

		@Override
		JUnit5EngineDescriptor buildDescriptor() {
			return new JUnit5EngineDescriptor(UniqueId.forEngine("junit5"));
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
			return new ClassTestDescriptor(UniqueId.root("class", uniqueId), testClass);
		}
	}

	public static class NestedClassTestDescriptorBuilder extends ClassTestDescriptorBuilder {

		public NestedClassTestDescriptorBuilder(String uniqueId, Class<?> testClass) {
			super(uniqueId, testClass);
		}

		@Override
		NestedClassTestDescriptor buildDescriptor() {
			return new NestedClassTestDescriptor(UniqueId.root("nested-class", uniqueId), testClass);
		}
	}

}
