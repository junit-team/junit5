/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.Requirement;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

/**
 * Collection of utilities for working with the {@link Requirement} annotation.
 *
 * @see org.junit.jupiter.api.Requirement
 */
final class RequirementUtils {

	private static final Logger logger = LoggerFactory.getLogger(RequirementUtils.class);

	/**
	 * Checks of the element is annotated with the {@link Requirement} annotation and returns its value when not blank.
	 * If the element is not properly annotated then NULL is returned as it's unclear if the element was written
	 * to ensure the correct implementation of a specific requirement.
	 *
	 * @param element Element to check for annotation
	 * @return id of the requirement. NULL if annotation is blank or not set.
	 */
	static String determineRequirementId(AnnotatedElement element) {
		Preconditions.notNull(element, "Annotated element must not be null");
		Optional<Requirement> requirementIdAnnotation = findAnnotation(element, Requirement.class);
		if (requirementIdAnnotation.isPresent()) {
			String requirementId = requirementIdAnnotation.get().id().trim();

			// When the annotation was used the value must be not blank.
			if (StringUtils.isBlank(requirementId)) {
				logger.warn(() -> String.format(
					"Configuration error: @Requirement on [%s] must be declared with a non-empty requirement id."
							+ "Annotation is ignored.",
					element));
			}
			else {
				return requirementId;
			}
		}
		return null;
	}

}
