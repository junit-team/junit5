/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.descriptor;

import static java.util.Arrays.stream;
import static org.junit.gen5.engine.junit4.descriptor.JavaSourceExtractor.toJavaSource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.experimental.categories.Category;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;
import org.junit.runner.Description;

/**
 * @since 5.0
 */
public class JUnit4TestDescriptor extends AbstractTestDescriptor {

	private final Description description;

	public JUnit4TestDescriptor(TestDescriptor parent, char separator, String uniqueIdSuffix, Description description) {
		this(parent, separator, uniqueIdSuffix, description, toJavaSource(description));
	}

	JUnit4TestDescriptor(TestDescriptor parent, char separator, String uniqueIdSuffix, Description description,
			Optional<? extends TestSource> source) {
		super(parent.getUniqueId() + separator + uniqueIdSuffix);
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
				.map(TestTag::new)
				.forEachOrdered(result::add)
		);
		// @formatter:on
		return result;
	}

	private Optional<Class<?>[]> getDeclaredCategories() {
		Category annotation = description.getAnnotation(Category.class);
		return Optional.ofNullable(annotation).map(Category::value);
	}

}
