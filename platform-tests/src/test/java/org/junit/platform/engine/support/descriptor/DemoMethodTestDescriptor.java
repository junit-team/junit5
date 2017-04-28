/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * @since 1.0
 */
public class DemoMethodTestDescriptor extends AbstractTestDescriptor {

	private final Class<?> testClass;
	private final Method testMethod;

	public DemoMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId, String.format("%s(%s)", Preconditions.notNull(testMethod, "Method must not be null").getName(),
			ClassUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes())));

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = testMethod;

		setSource(new MethodSource(testMethod));
	}

	@Override
	public Set<TestTag> getTags() {
		// @formatter:off
		Set<TestTag> methodTags =  findRepeatableAnnotations(this.testClass, Tag.class).stream()
				.map(Tag::value)
				.filter(StringUtils::isNotBlank)
				.map(TestTag::create)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on

		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

}
