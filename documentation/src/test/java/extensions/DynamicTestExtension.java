/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package extensions;

import static org.junit.gen5.commons.util.ReflectionUtils.invokeMethod;

import java.util.stream.Stream;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.Executable;
import org.junit.gen5.api.extension.DynamicTestCreator;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.commons.JUnitException;

public class DynamicTestExtension implements DynamicTestCreator {
	@Override
	public boolean supports(MethodInvocationContext methodInvocationContext, ExtensionContext extensionContext)
			throws JUnitException {
		Class<?>[] parameterTypes = methodInvocationContext.getMethod().getParameterTypes();
		return parameterTypes.length == 1 && parameterTypes[0] == Boolean.TYPE;
	}

	@Override
	public Stream<DynamicTest> replace(MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) throws JUnitException {
		return Stream.of(new DynamicTest("extensionTest", testExecutable(methodInvocationContext, true)),
			new DynamicTest("extensionTest", testExecutable(methodInvocationContext, false)));
	}

	private Executable testExecutable(MethodInvocationContext context, boolean parameter) {
		return () -> invokeMethod(context.getMethod(), context.getInstance(), parameter);
	}
}
