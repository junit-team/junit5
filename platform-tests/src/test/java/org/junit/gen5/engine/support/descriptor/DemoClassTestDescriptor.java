/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static java.util.stream.Collectors.toCollection;
import static org.junit.gen5.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.jupiter.api.Tag;

/**
 * @since 5.0
 */
public class DemoClassTestDescriptor extends AbstractTestDescriptor {

	private final String displayName;
	private final Class<?> testClass;

	public DemoClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
		super(uniqueId);
		setSource(new JavaClassSource(testClass));
		this.displayName = testClass.getSimpleName();
		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
	}

	@Override
	public Set<TestTag> getTags() {
		// @formatter:off
		return findRepeatableAnnotations(this.testClass, Tag.class).stream()
				.map(Tag::value)
				.filter(StringUtils::isNotBlank)
				.map(TestTag::of)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	@Override
	public final boolean isTest() {
		return false;
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

}
