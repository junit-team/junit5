/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import org.junit.gen5.commons.util.AnnotationUtils;

class ReflectionObjectTester {

	boolean hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return AnnotationUtils.findAnnotation(element, annotationType).isPresent();
	}

	boolean hasModifier(Object candidate, int modifier) {
		int modifiers = 0;
		if (candidate instanceof Class) {
			modifiers = ((Class<?>) candidate).getModifiers();
		}
		if (candidate instanceof Member) {
			modifiers = ((Member) candidate).getModifiers();
		}
		return (modifiers & modifier) != 0;
	}

	boolean isPrivate(Object candidate) {
		return hasModifier(candidate, Modifier.PRIVATE);
	}

	boolean isAbstract(Object candidate) {
		return hasModifier(candidate, Modifier.ABSTRACT);
	}

	boolean isStatic(Object candidate) {
		return hasModifier(candidate, Modifier.STATIC);
	}
}
