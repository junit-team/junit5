/*
 * Copyright 2015-2016 the original author or authors.
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

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * @since 1.0
 */
public class DemoClassTestDescriptor extends AbstractTestDescriptor {

	private final Class<?> testClass;

	public DemoClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
		super(uniqueId, Preconditions.notNull(testClass, "Class must not be null").getSimpleName());
		setSource(new JavaClassSource(testClass));
		this.testClass = testClass;
	}

	@Override
	public Set<TestTag> getTags() {
		// @formatter:off
		return findRepeatableAnnotations(this.testClass, Tag.class).stream()
				.map(Tag::value)
				.filter(StringUtils::isNotBlank)
				.map(TestTag::create)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
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
