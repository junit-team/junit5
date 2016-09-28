/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestFactoryExtension;
import org.junit.platform.commons.meta.API;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.function.Predicate;

import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;

/**
 * Test whether an element is extended with {@link TestFactoryExtension}.
 *
 * @since 5.0
 */
@API(Internal)
class IsTestFactoryExtensionElement implements Predicate<AnnotatedElement> {

	@Override
	@SuppressWarnings("unchecked")
	public boolean test(AnnotatedElement candidate) {
		return TestFactoryExtension.streamTestFactoryExtensions(candidate).anyMatch(ignored -> true);
	}

}
