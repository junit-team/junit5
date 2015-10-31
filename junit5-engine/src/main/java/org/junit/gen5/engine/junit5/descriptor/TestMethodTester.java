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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.gen5.api.Test;

class TestMethodTester extends ReflectionObjectTester {

	boolean accept(Method testMethodCandidate) {
		if (isPrivate(testMethodCandidate))
			return false;
		return hasAnnotation(testMethodCandidate, Test.class);
	}

}
