/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

/**
 * @since 5.0
 */
@API(Internal)
public abstract class JupiterTestDescriptor extends AbstractTestDescriptor
		implements Node<JupiterEngineExecutionContext> {

	JupiterTestDescriptor(UniqueId uniqueId, String displayName) {
		super(uniqueId, displayName);
	}

	// --- TestDescriptor ------------------------------------------------------

	protected Set<TestTag> getTags(AnnotatedElement element) {
		// @formatter:off
		return findRepeatableAnnotations(element, Tag.class).stream()
				.map(Tag::value)
				.filter(StringUtils::isNotBlank)
				.map(TestTag::create)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	protected static <E extends AnnotatedElement> String determineDisplayName(E element,
			Function<E, String> defaultDisplayNameGenerator) {
		// @formatter:off
		return findAnnotation(element, DisplayName.class)
				.map(DisplayName::value)
				.filter(StringUtils::isNotBlank)
				.orElseGet(() -> defaultDisplayNameGenerator.apply(element));
		// @formatter:on
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public boolean isLeaf() {
		return !isContainer();
	}

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
