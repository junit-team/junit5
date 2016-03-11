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

import java.util.stream.Stream;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.Executable;
import org.junit.gen5.api.extension.DynamicTestCreator;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.commons.util.ReflectionUtils;

// @formatter:off
// tag::user_guide[]
public class DynamicTestExtension implements DynamicTestCreator {
	@Override
	public boolean supports(MethodInvocationContext context, ExtensionContext extension) {
		Class<?>[] parameterTypes = context.getMethod().getParameterTypes();
		return parameterTypes.length == 1 && parameterTypes[0] == Boolean.TYPE;
	}

	@Override
	public Stream<DynamicTest> replace(MethodInvocationContext context, ExtensionContext extension) {
		return Stream.of(
				new DynamicTest("extensionTest", testExecutable(context, true)),
				new DynamicTest("extensionTest", testExecutable(context, false)));
	}

	private Executable testExecutable(MethodInvocationContext context, boolean parameter) {
		return () -> ReflectionUtils.invokeMethod(context.getMethod(), context.getInstance(), parameter);
	}
}
// end::user_guide[]
// @formatter:on
