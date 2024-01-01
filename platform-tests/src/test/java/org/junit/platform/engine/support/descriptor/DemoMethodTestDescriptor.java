/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * @since 1.0
 */
public class DemoMethodTestDescriptor extends AbstractTestDescriptor {

	private static final Logger logger = LoggerFactory.getLogger(DemoMethodTestDescriptor.class);

	private final Class<?> testClass;
	private final Method testMethod;

	public DemoMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId,
			String.format("%s(%s)", Preconditions.notNull(testMethod, "Method must not be null").getName(),
				ClassUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes())),
			MethodSource.from(testMethod));

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = testMethod;
	}

	@Override
	public Set<TestTag> getTags() {
		// Copied from org.junit.jupiter.engine.descriptor.JupiterTestDescriptor.getTags(AnnotatedElement)
		// @formatter:off
		Set<TestTag> methodTags = findRepeatableAnnotations(this.testMethod, Tag.class).stream()
				.map(Tag::value)
				.filter(tag -> {
					var isValid = TestTag.isValid(tag);
					if (!isValid) {
						// TODO [#242] Replace logging with precondition check once we have a proper mechanism for
						// handling validation exceptions during the TestEngine discovery phase.
						//
						// As an alternative to a precondition check here, we could catch any
						// PreconditionViolationException thrown by TestTag::create.
						logger.warn(() -> String.format(
							"Configuration error: invalid tag syntax in @Tag(\"%s\") declaration on [%s]. Tag will be ignored.",
							tag, this.testMethod));
					}
					return isValid;
				})
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
