/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection;

import java.util.*;

//TODO: this class will have to be filled by the extension mechanism and handed to the resolver engine from outside
public class PrimitiveMethodArgumentResolverRegistry implements MethodArgumentResolverRegistry {

	private List<MethodArgumentResolver> methodArgumentResolvers = Arrays.asList(
		new SimpleTypeBasedMethodArgumentResolver(), new SimpleAnnotationBasedMethodArgumentResolver(),
		new TestNameArgumentResolver());

	@Override
	public List<MethodArgumentResolver> getMethodArgumentResolvers() {
		return this.methodArgumentResolvers;
	}

}
