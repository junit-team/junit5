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

import java.lang.reflect.Method;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.AnnotationUtils;

/**
 * @author Johannes Link
 * @author Sam Brannen
 * @since 5.0
 */
class TestMethodTester {

	boolean accept(Method testMethodCandidate) {
		return AnnotationUtils.findAnnotation(testMethodCandidate, Test.class).isPresent();
	}

}
