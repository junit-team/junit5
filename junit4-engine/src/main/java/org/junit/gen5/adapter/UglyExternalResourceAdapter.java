/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.adapter;

import java.lang.reflect.Method;

import org.junit.gen5.api.extension.*;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.rules.ExternalResource;

// very early thoughts - please do not polish yet :)

public class UglyExternalResourceAdapter implements BeforeEachExtensionPoint, AfterEachExtensionPoint {

	ExternalResource externalResource;

	@Override
	public void beforeEach(TestExtensionContext context) throws Exception {
		this.findAndInvokeMethod("before");
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		this.findAndInvokeMethod("after");
	}




	//exception handling?
	private void findAndInvokeMethod(String name) throws NoSuchMethodException {
		Method method = this.findMethod(name);
		method.setAccessible(true);
		ReflectionUtils.invokeMethod(method, this.externalResource);
	}

	private Method findMethod(String name) throws NoSuchMethodException {
		return this.externalResource.getClass().getMethod(name);
	}


}
