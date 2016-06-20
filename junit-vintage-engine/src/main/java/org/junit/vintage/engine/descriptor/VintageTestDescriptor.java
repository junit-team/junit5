/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.descriptor;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.isEqual;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.experimental.categories.Category;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.JavaClassSource;
import org.junit.platform.engine.support.descriptor.JavaMethodSource;
import org.junit.platform.engine.support.descriptor.JavaSource;
import org.junit.runner.Description;

/**
 * @since 4.12
 */
@API(Internal)
public class VintageTestDescriptor extends AbstractTestDescriptor {

	public static final String ENGINE_ID = "junit-vintage";
	public static final String SEGMENT_TYPE_RUNNER = "runner";
	public static final String SEGMENT_TYPE_TEST = "test";

	private final Description description;

	public VintageTestDescriptor(TestDescriptor parent, String segmentType, String segmentValue,
			Description description) {
		this(parent, segmentType, segmentValue, description, toJavaSource(description));
	}

	VintageTestDescriptor(TestDescriptor parent, String segmentType, String segmentValue, Description description,
			Optional<? extends TestSource> source) {
		super(parent.getUniqueId().append(segmentType, segmentValue));

		this.description = description;
		source.ifPresent(this::setSource);
	}

	public Description getDescription() {
		return description;
	}

	@Override
	public String getDisplayName() {
		String methodName = description.getMethodName();
		return methodName != null ? methodName : description.getDisplayName();
	}

	@Override
	public boolean isTest() {
		return description.isTest();
	}

	@Override
	public boolean isContainer() {
		return description.isSuite();
	}

	@Override
	public Set<TestTag> getTags() {
		Set<TestTag> result = new LinkedHashSet<>();
		getParent().ifPresent(parent -> result.addAll(parent.getTags()));
		// @formatter:off
		getDeclaredCategories().ifPresent(categoryClasses ->
			stream(categoryClasses)
				.map(ReflectionUtils::getAllAssignmentCompatibleClasses)
				.flatMap(Collection::stream)
				.distinct()
				.map(Class::getName)
				.map(TestTag::of)
				.forEachOrdered(result::add)
		);
		// @formatter:on
		return result;
	}

	private Optional<Class<?>[]> getDeclaredCategories() {
		Category annotation = description.getAnnotation(Category.class);
		return Optional.ofNullable(annotation).map(Category::value);
	}

	private static Optional<JavaSource> toJavaSource(Description description) {
		Class<?> testClass = description.getTestClass();
		if (testClass != null) {
			String methodName = description.getMethodName();
			if (methodName != null) {
				JavaMethodSource javaMethodSource = toJavaMethodSource(testClass, methodName);
				if (javaMethodSource != null) {
					return Optional.of(javaMethodSource);
				}
			}
			return Optional.of(new JavaClassSource(testClass));
		}
		return Optional.empty();
	}

	private static JavaMethodSource toJavaMethodSource(Class<?> testClass, String methodName) {
		if (methodName.contains("[") && methodName.endsWith("]")) {
			// special case for parameterized tests
			return toJavaMethodSource(testClass, methodName.substring(0, methodName.indexOf("[")));
		}
		else {
			List<Method> methods = findMethods(testClass, where(Method::getName, isEqual(methodName)));
			return (methods.size() == 1) ? new JavaMethodSource(getOnlyElement(methods)) : null;
		}
	}

}
