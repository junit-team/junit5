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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class ClassNameSpecificationResolver implements SpecificationResolver<ClassNameSpecification, ClassTestGroup> {

	@Override
	public ClassTestGroup resolve(TestDescriptor parent, ClassNameSpecification element) {
		Class<?> clazz = ReflectionUtils.loadClass(element.getClassName());
		if (parent instanceof EngineTestGroup) {
			while (clazz.isMemberClass()) {
				clazz = clazz.getDeclaringClass();
			}
			return new ClassTestGroup(parent, clazz);
		}
		else if (parent instanceof ClassTestGroup) {
			ClassTestGroup group = (ClassTestGroup) parent;

			if (clazz != group.getTestClass()) {
				Class<?> memberClazz;
				do {
					memberClazz = clazz;
					clazz = clazz.getDeclaringClass();
				} while (clazz != group.getTestClass());
				return new ClassTestGroup(parent, memberClazz);
			}
		}
		throw new IllegalStateException(
			String.format("Given class name '%s' could not be completely resolved!", element.getClassName()));
	}

	@Override
	public List<TestDescriptor> resolveChildren(ClassTestGroup parent, ClassNameSpecification element) {
		if (parent.getUniqueId().endsWith(element.getClassName())) {
			// TODO fetch children resolvers from according to type
			List<TestDescriptor> children = new LinkedList<>();

			children.addAll(Arrays.stream(parent.getTestClass().getDeclaredMethods()).filter(
				method -> method.isAnnotationPresent(Test.class)).map(method -> new MethodTest(parent, method)).collect(
					toList()));

			List<ClassTestGroup> groups = Arrays.stream(parent.getTestClass().getDeclaredClasses()).filter(
				Class::isMemberClass).map(clazz -> new ClassTestGroup(parent, clazz)).collect(toList());
			children.addAll(groups);

			groups.forEach(group -> children.addAll(resolveChildren(group, element)));

			return children;
		}
		else {
			ClassTestGroup child = resolve(parent, element);
			List<TestDescriptor> children = resolveChildren(child, element);
			List<TestDescriptor> result = new LinkedList<>();
			result.add(child);
			result.addAll(children);
			return result;
		}
	}
}
