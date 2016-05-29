/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.gen5.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Executable;
import org.junit.gen5.api.Tag;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;
import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;

/**
 * @since 5.0
 */
@API(Internal)
public abstract class JUnit5TestDescriptor extends AbstractTestDescriptor {

	protected JUnit5TestDescriptor(UniqueId uniqueId) {
		super(uniqueId);
	}

	protected Set<TestTag> getTags(AnnotatedElement element) {
		// @formatter:off
		return findRepeatableAnnotations(element, Tag.class).stream()
				.map(Tag::value)
				.filter(StringUtils::isNotBlank)
				.map(TestTag::of)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	protected String determineDisplayName(AnnotatedElement element) {
		// @formatter:off
		return findAnnotation(element, DisplayName.class)
				.map(DisplayName::value)
				.filter(StringUtils::isNotBlank)
				.orElse(generateDefaultDisplayName());
		// @formatter:on
	}

	protected abstract String generateDefaultDisplayName();

	protected ExtensionRegistry populateNewExtensionRegistryFromExtendWith(AnnotatedElement annotatedElement,
			ExtensionRegistry existingExtensionRegistry) {
		// @formatter:off
		List<Class<? extends Extension>> extensionTypes = findRepeatableAnnotations(annotatedElement, ExtendWith.class).stream()
				.map(ExtendWith::value)
				.flatMap(Arrays::stream)
				.collect(toList());
		// @formatter:on
		return ExtensionRegistry.createRegistryFrom(existingExtensionRegistry, extensionTypes);
	}

	/**
	 * Execute the supplied {@link Executable} and
	 * {@linkplain ExceptionUtils#throwAsUncheckedException mask} any
	 * exception thrown as an unchecked exception.
	 *
	 * @param executable the {@code Executable} to execute
	 * @see ExceptionUtils#throwAsUncheckedException(Throwable)
	 */
	protected void executeAndMaskThrowable(Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable throwable) {
			ExceptionUtils.throwAsUncheckedException(throwable);
		}
	}

}
