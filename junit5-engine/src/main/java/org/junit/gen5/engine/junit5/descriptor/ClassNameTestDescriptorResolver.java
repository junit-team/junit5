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
		implements TestDescriptorResolver<ClassNameSpecification, JavaClassTestDescriptor> {

	@Override
	public JavaClassTestDescriptor resolve(TestDescriptor parent, ClassNameSpecification element) {
		Class<?> clazz = loadClass(element.getClassName());
		return new JavaClassTestDescriptor(clazz, parent);
	}

	@Override
	public List<TestDescriptor> resolveChildren(JavaClassTestDescriptor parent, ClassNameSpecification element) {
		if (parent.getUniqueId().endsWith(element.getClassName())) {
			// TODO fetch children resolvers from according to type
			List<TestDescriptor> children = new LinkedList<>();

			children.addAll(Arrays.stream(parent.getTestClass().getDeclaredMethods()).filter(
				method -> method.isAnnotationPresent(Test.class)).map(
					method -> new JavaMethodTestDescriptor(method, parent)).collect(toList()));

			List<JavaClassTestDescriptor> groups = Arrays.stream(parent.getTestClass().getDeclaredClasses()).filter(
				Class::isMemberClass).map(clazz -> new JavaClassTestDescriptor(clazz, parent)).collect(toList());
			children.addAll(groups);

			groups.forEach(group -> children.addAll(resolveChildren(group, element)));

			return children;
		}
		else {
			JavaClassTestDescriptor child = resolve(parent, element);
			List<TestDescriptor> children = resolveChildren(child, element);
			List<TestDescriptor> result = new LinkedList<>();
			result.add(child);
			result.addAll(children);
			return result;
		}
	}

}
