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

import static java.util.stream.Collectors.*;
import static org.junit.gen5.commons.util.AnnotationUtils.*;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.gen5.api.Name;
import org.junit.gen5.api.Tag;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestTag;

/**
 * @author Sam Brannen
 * @since 5.0
 */
abstract class JUnit5TestDescriptor extends AbstractTestDescriptor {

	protected JUnit5TestDescriptor(String uniqueId) {
		super(uniqueId);
	}

	protected Set<TestTag> getTags(AnnotatedElement element) {
		// @formatter:off
		return findRepeatableAnnotations(element, Tag.class).stream()
				.map(Tag::value)
				.filter(StringUtils::isNotBlank)
				.map(TestTag::new)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	protected String determineDisplayName(AnnotatedElement element, String defaultName) {
		// @formatter:off
		return findAnnotation(element, Name.class)
				.map(Name::value)
				.filter(StringUtils::isNotBlank)
				.orElse(defaultName);
		// @formatter:on
	}

}
