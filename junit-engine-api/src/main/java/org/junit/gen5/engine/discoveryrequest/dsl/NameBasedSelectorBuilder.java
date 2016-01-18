/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discoveryrequest.dsl;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.engine.discoveryrequest.dsl.ClassSelectorBuilder.forClass;
import static org.junit.gen5.engine.discoveryrequest.dsl.MethodSelectorBuilder.byMethod;
import static org.junit.gen5.engine.discoveryrequest.dsl.PackageSelectorBuilder.byPackageName;

import java.lang.reflect.Method;
import java.util.*;

import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
public class NameBasedSelectorBuilder {
	public static DiscoverySelector byName(String anyName) {
		Optional<Class<?>> testClassOptional = ReflectionUtils.loadClass(anyName);
		if (testClassOptional.isPresent()) {
			return forClass(testClassOptional.get());
		}

		Optional<Method> testMethodOptional = ReflectionUtils.loadMethod(anyName);
		if (testMethodOptional.isPresent()) {
			Method testMethod = testMethodOptional.get();
			return byMethod(testMethod.getDeclaringClass(), testMethod);
		}

		if (ReflectionUtils.isPackage(anyName)) {
			return byPackageName(anyName);
		}

		throw new PreconditionViolationException(
			String.format("'%s' specifies neither a class, a method, nor a package.", anyName));
	}

	public static List<DiscoverySelector> byNames(String... classNames) {
		if (classNames != null) {
			return byNames(Arrays.asList(classNames));
		}
		else {
			return Collections.emptyList();
		}
	}

	public static List<DiscoverySelector> byNames(Collection<String> classNames) {
		return classNames.stream().map(NameBasedSelectorBuilder::byName).collect(toList());
	}
}
