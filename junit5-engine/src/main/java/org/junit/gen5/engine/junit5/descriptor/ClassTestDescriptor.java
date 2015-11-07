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

import org.junit.gen5.api.Name;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * <p>The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name}</code>.
 *
 * @since 5.0
 */
public class ClassTestDescriptor extends AbstractTestDescriptor {

	private final String displayName;
	private final Class<?> testClass;

	public ClassTestDescriptor(String uniqueId, Class<?> testClass) {
		super(uniqueId);
		Preconditions.notNull(testClass, "testClass must not be null");

		this.testClass = testClass;
		this.displayName = determineDisplayName();
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public String getDisplayName() {
		return displayName;
	}

	private String determineDisplayName() {
		// @formatter:off
		return AnnotationUtils.findAnnotation(this.testClass, Name.class)
				.map(Name::value)
				.filter(name -> !StringUtils.isBlank(name))
				.orElse(this.testClass.getName());
		// @formatter:on
	}

	@Override
	public final boolean isTest() {
		return false;
	}

}
