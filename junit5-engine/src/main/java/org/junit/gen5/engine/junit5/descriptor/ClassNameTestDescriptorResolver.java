/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ReflectionUtils.loadClass;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class ClassNameTestDescriptorResolver
		implements TestDescriptorResolver<ClassNameSpecification, ClassTestDescriptor> {

	@Override
	public ClassTestDescriptor resolve(TestDescriptor parent, ClassNameSpecification element) {
		Class<?> clazz = loadClass(element.getClassName());
		return new ClassTestDescriptor(clazz, parent);
	}

	@Override
	public List<TestDescriptor> resolveChildren(ClassTestDescriptor parent, ClassNameSpecification element) {
		if (parent.getUniqueId().endsWith(element.getClassName())) {
			// TODO Retrieve children resolvers according to type
			List<TestDescriptor> children = new LinkedList<>();

			// @formatter:off
			children.addAll(Arrays.stream(parent.getTestClass().getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(Test.class))
				.map(method -> new MethodTestDescriptor(method, parent))
				.collect(toList()));
			// @formatter:on

			// @formatter:off
			List<ClassTestDescriptor> groups = Arrays.stream(parent.getTestClass().getDeclaredClasses())
				.filter(Class::isMemberClass)
				.map(clazz -> new ClassTestDescriptor(clazz, parent))
				.collect(toList());
			children.addAll(groups);
			// @formatter:on

			groups.forEach(group -> children.addAll(resolveChildren(group, element)));

			return children;
		}
		else {
			ClassTestDescriptor child = resolve(parent, element);
			List<TestDescriptor> children = resolveChildren(child, element);
			List<TestDescriptor> result = new LinkedList<>();
			result.add(child);
			result.addAll(children);
			return result;
		}
	}

}
