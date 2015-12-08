/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.Parent;

public class JUnit5ClassDescriptor extends AbstractTestDescriptor implements Parent<JUnit5Context> {

	private final Class<?> testClass;

	protected JUnit5ClassDescriptor(String engineId, Class<?> testClass) {
		super(engineId + ":" + testClass.getName());
		this.testClass = testClass;
	}

	@Override
	public String getDisplayName() {
		return testClass.getName();
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public JUnit5Context beforeAll(JUnit5Context context) {
		return context.withTestInstanceProvider(testInstanceProvider());
	}

	@Override
	public JUnit5Context afterAll(JUnit5Context context) {
		return context;
	}

	private TestInstanceProvider testInstanceProvider() {
		return () -> ReflectionUtils.newInstance(testClass);
	}

}
