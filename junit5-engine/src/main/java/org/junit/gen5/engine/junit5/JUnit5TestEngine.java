/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElementVisitor;
import org.junit.gen5.engine.TreeBasedTestEngine;

public class JUnit5TestEngine extends TreeBasedTestEngine {

	private static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return ENGINE_ID;
	}

	@Override
	public JUnit5EngineDescriptor discoverTests(TestPlanSpecification specification) {
		Preconditions.notNull(specification, "specification must not be null");
		JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(this);
		specification.accept(new TestPlanSpecificationElementVisitor() {

			@Override
			public void visitClass(Class<?> testClass) {
				JUnit5ClassDescriptor classDescriptor = new JUnit5ClassDescriptor(ENGINE_ID, testClass);
				List<Method> methods = AnnotationUtils.findAnnotatedMethods(testClass, Test.class,
					MethodSortOrder.HierarchyDown);
				for (Method method : methods) {
					classDescriptor.addChild(
						new JUnit5MethodDescriptor(classDescriptor.getUniqueId(), testClass, method));
				}
				engineDescriptor.addChild(classDescriptor);
			}
		});
		return engineDescriptor;
	}
}