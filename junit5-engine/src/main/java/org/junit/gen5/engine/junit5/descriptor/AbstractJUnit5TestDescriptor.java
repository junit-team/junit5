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

import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.Name;
import org.junit.gen5.api.Tag;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestTag;

public abstract class AbstractJUnit5TestDescriptor extends AbstractTestDescriptor {

	protected AbstractJUnit5TestDescriptor(String uniqueId) {
		super(uniqueId);
	}

	protected Set<TestTag> getTags(AnnotatedElement taggedElement) {
		Set<TestTag> tags = new HashSet<>();
		// @formatter:off
		List<Tag> tagAnnotations = AnnotationUtils.findAllAnnotations(taggedElement, Tag.class);
		tagAnnotations.stream()
				.map(Tag::value)
				.filter(name -> !StringUtils.isBlank(name))
				.forEach( tagName -> tags.add(new TestTag(tagName)));
		// @formatter:on

		return tags;
	}

	protected String determineDisplayName(AnnotatedElement namedElement, String defaultName) {
		// @formatter:off
		return AnnotationUtils.findAnnotation(namedElement, Name.class)
				.map(Name::value)
				.filter(name -> !StringUtils.isBlank(name))
				.orElse(defaultName);
		// @formatter:on
	}

}
