/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discoveryNEW;

import java.lang.reflect.AnnotatedElement;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;

public interface ElementResolver {

	boolean willResolve(AnnotatedElement element, TestDescriptor parent);

	UniqueId createUniqueId(AnnotatedElement element, TestDescriptor parent);

	TestDescriptor resolve(AnnotatedElement element, TestDescriptor parent, UniqueId parentId);
}
