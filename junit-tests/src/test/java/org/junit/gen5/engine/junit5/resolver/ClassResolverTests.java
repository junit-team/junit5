/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static org.junit.gen5.engine.TestPlanSpecification.build;

import org.junit.Test;
import org.junit.gen5.engine.DummyTestEngine;
import org.junit.gen5.engine.EngineDescriptor;

public class ClassResolverTests {
	private ClassResolver resolver = new ClassResolver();

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalParent_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(null, build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalSpecification_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(new EngineDescriptor(new DummyTestEngine()), null);
	}
}
