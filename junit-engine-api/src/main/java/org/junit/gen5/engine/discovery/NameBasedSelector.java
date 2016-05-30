/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.engine.discovery.MethodSelector.selectMethod;
import static org.junit.gen5.engine.discovery.PackageSelector.selectPackage;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
@API(Experimental)
public class NameBasedSelector {

	public static DiscoverySelector selectName(String name) {
		Preconditions.notBlank(name, "name must not be null or empty");

		Optional<Class<?>> classOptional = ReflectionUtils.loadClass(name);
		if (classOptional.isPresent()) {
			return selectClass(classOptional.get());
		}

		Optional<Method> methodOptional = ReflectionUtils.loadMethod(name);
		if (methodOptional.isPresent()) {
			Method method = methodOptional.get();
			return selectMethod(method.getDeclaringClass(), method);
		}

		if (ReflectionUtils.isPackage(name)) {
			return selectPackage(name);
		}

		throw new PreconditionViolationException(
			String.format("'%s' specifies neither a class, a method, nor a package.", name));
	}

	public static List<DiscoverySelector> selectNames(String... classNames) {
		if (classNames != null) {
			return selectNames(Arrays.asList(classNames));
		}
		return emptyList();
	}

	public static List<DiscoverySelector> selectNames(Collection<String> classNames) {
		return classNames.stream().map(NameBasedSelector::selectName).collect(toList());
	}

}
