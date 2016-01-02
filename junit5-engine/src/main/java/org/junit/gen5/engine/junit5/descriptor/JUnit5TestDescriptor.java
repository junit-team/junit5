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
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.gen5.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.Executable;
import org.junit.gen5.api.Name;
import org.junit.gen5.api.Tag;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;

/**
 * @since 5.0
 */
public abstract class JUnit5TestDescriptor extends AbstractTestDescriptor {

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

	protected TestExtensionRegistry populateNewTestExtensionRegistryFromExtendWith(AnnotatedElement annotatedElement,
			TestExtensionRegistry existingTestExtensionRegistry) {
		// @formatter:off
		List<Class<? extends TestExtension>> extensionClasses = findRepeatableAnnotations(annotatedElement, ExtendWith.class).stream()
				.map(ExtendWith::value)
				.flatMap(Arrays::stream)
				.collect(toList());
		// @formatter:on
		return TestExtensionRegistry.newRegistryFrom(existingTestExtensionRegistry, extensionClasses);
	}

	/**
	 * Execute the supplied {@link Executable} and <em>mask</em> any
	 * exception thrown as a {@link RuntimeException}.
	 *
	 * <p>Exceptions will not be wrapped. Rather, they will be thrown
	 * as-is via a reflective hack that tricks the Java compiler into
	 * believing that the thrown exception is an unchecked exception.
	 */
	protected void executeAndMaskThrowable(Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable throwable) {
			ReflectionUtils.throwAsRuntimeException(throwable);
		}
	}

}
