/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.discovery.JUnit5EngineDescriptor;

/**
 * @since 5.0
 */
public abstract class TestDescriptorBuilder {

	List<TestDescriptorBuilder> children = new ArrayList<>();

	public static TestDescriptorBuilder engineDescriptor() {
		return new JUnit5EngineDescriptorBuilder();
	}

	public static TestDescriptorBuilder classTestDescriptor(String uniqueId, Class<?> testClass) {
		return new ClassTestDescriptorBuilder(uniqueId, testClass);
	}

	public static TestDescriptorBuilder nestedClassTestDescriptor(String uniqueId, Class<?> testClass) {
		return new NestedClassTestDescriptorBuilder(uniqueId, testClass);
	}

	public TestDescriptor build() {
		TestDescriptor testDescriptor = buildDescriptor();
		children.forEach(builder -> testDescriptor.addChild(builder.build()));
		return testDescriptor;
	}

	public TestDescriptorBuilder with(TestDescriptorBuilder... children) {
		this.children.addAll(Arrays.asList(children));
		return this;
	}

	abstract TestDescriptor buildDescriptor();

	static class JUnit5EngineDescriptorBuilder extends TestDescriptorBuilder {

		@Override
		TestDescriptor buildDescriptor() {
			return new JUnit5EngineDescriptor(UniqueId.forEngine("junit5"));
		}
	}

	static class ClassTestDescriptorBuilder extends TestDescriptorBuilder {

		protected final String uniqueId;
		protected final Class<?> testClass;

		public ClassTestDescriptorBuilder(String uniqueId, Class<?> testClass) {
			this.uniqueId = uniqueId;
			this.testClass = testClass;
		}

		@Override
		TestDescriptor buildDescriptor() {
			return new ClassTestDescriptor(UniqueId.root("class", uniqueId), testClass);
		}
	}

	static class NestedClassTestDescriptorBuilder extends ClassTestDescriptorBuilder {

		public NestedClassTestDescriptorBuilder(String uniqueId, Class<?> testClass) {
			super(uniqueId, testClass);
		}

		@Override
		TestDescriptor buildDescriptor() {
			return new NestedClassTestDescriptor(UniqueId.root("nested-class", uniqueId), testClass);
		}
	}
}
