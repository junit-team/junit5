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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueIdSpecification;

/**
 * <p>The pattern of the unique ID takes the form of
 * <code>{fully qualified class name}#{method name}({comma separated list
 * of method parameter types})</code>, where each method parameter type is
 * a fully qualified class name or a primitive type. For example,
 * {@code org.example.MyTests#test()} references the {@code test()} method
 * in the {@code org.example.MyTests} class that does not accept parameters.
 * Similarly, {@code org.example.MyTests#test(java.lang.String, java.math.BigDecimal)}
 * references the {@code test()} method in the {@code org.example.MyTests}
 * class that requires a {@code String} and {@code BigDecimal} as parameters.
 *
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
public class UniqueIdTestDescriptorResolver implements TestDescriptorResolver<UniqueIdSpecification, TestDescriptor> {

	// The following pattern only supports descriptors for test methods.
	// TODO Support descriptors for test classes.
	// TODO Decide if we want to support descriptors for packages.
	private static final Pattern UID_PATTERN = Pattern.compile("^(?:([^:]+):)?(?:([^#]+)#)?(?:([^(]+)\\(([^)]*)\\))?$");

	@Override
	public TestDescriptor resolve(TestDescriptor parent, UniqueIdSpecification element) {
		String uid = element.getUniqueId();
		Preconditions.notEmpty(uid, "UniqueID must not be empty");
		Preconditions.condition(uid.startsWith(parent.getUniqueId()),
			String.format("UniqueID '%s' must start with UniqueID of parent '%s'!", uid, parent.getUniqueId()));

		Matcher matcher = UID_PATTERN.matcher(uid);
		Preconditions.condition(matcher.matches(), () -> String.format("Given UniqueID '%s' was not recognised!", uid));

		String className = matcher.group(2);
		String methodName = matcher.group(3);
		String methodParameters = matcher.group(4);

		Class<?> clazz = ReflectionUtils.loadClass(className);
		if (parent instanceof EngineDescriptor) {
			return new JavaClassTestDescriptor(clazz, parent);
		}
		else if (parent instanceof JavaClassTestDescriptor) {
			JavaClassTestDescriptor group = (JavaClassTestDescriptor) parent;

			try {
				List<Class<?>> paramTypeList = new ArrayList<>();
				for (String type : methodParameters.split(",")) {
					type = type.trim();
					if (!type.isEmpty()) {
						paramTypeList.add(ReflectionUtils.loadClass(type));
					}
				}

				Class<?>[] parameterTypes = paramTypeList.toArray(new Class<?>[paramTypeList.size()]);
				JavaClassTestDescriptor testClassGroup = group;
				Method method = testClassGroup.getTestClass().getDeclaredMethod(methodName, parameterTypes);
				return new JavaMethodTestDescriptor(method, testClassGroup);
			}
			catch (NoSuchMethodException e) {
				throw new IllegalStateException("Failed to get method with name '" + methodName + "'.", e);
			}
		}
		else {
			throw new IllegalStateException(
				String.format("Given UniqueID '%s' could not be completely resolved!", uid));
		}
	}

	@Override
	public List<TestDescriptor> resolveChildren(TestDescriptor parent, UniqueIdSpecification element) {
		if (element.getUniqueId().equals(parent.getUniqueId())) {
			return Collections.emptyList();
		}
		else {
			TestDescriptor child = resolve(parent, element);
			List<TestDescriptor> children = resolveChildren(child, element);

			List<TestDescriptor> result = new LinkedList<>();
			result.add(child);
			result.addAll(children);
			return result;
		}
	}
}