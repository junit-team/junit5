/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.ForAll;
import org.junit.gen5.api.extension.AfterAllCallbacks;
import org.junit.gen5.api.extension.BeforeAllCallbacks;
import org.junit.gen5.api.extension.ContextScope;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ReflectionUtils;

public class ForAllExtension implements MethodParameterResolver, BeforeAllCallbacks, AfterAllCallbacks {

	final ContextScope<Class<?>, Object> forAllObjects;

	ForAllExtension() {
		Function<Class<?>, Object> createForAllObject = clazz -> ReflectionUtils.newInstance(clazz);
		forAllObjects = new ContextScope<>(createForAllObject, ContextScope.Inheritance.Yes);
	}

	@Override
	public boolean supports(Parameter parameter, TestExecutionContext testExecutionContext) {
		List<Object> objectsInScope = getObjectsInScope(testExecutionContext);
		return objectsInScope.stream().anyMatch(o -> o.getClass() == parameter.getType());
	}

	@Override
	public Object resolve(Parameter parameter, TestExecutionContext testExecutionContext)
			throws ParameterResolutionException {
		List<Object> objectsInScope = getObjectsInScope(testExecutionContext);
		return objectsInScope.stream().filter(o -> o.getClass() == parameter.getType()).findFirst().orElse(null);
	}

	@Override
	public void preBeforeAll(TestExecutionContext testExecutionContext) throws Exception {
		List<Object> objectsInScope = getObjectsInScope(testExecutionContext);

		objectsInScope.forEach(o -> {
			List<Method> beforeAllMethods = AnnotationUtils.findAnnotatedMethods(o.getClass(), BeforeAll.class,
				ReflectionUtils.MethodSortOrder.HierarchyDown);
			beforeAllMethods.stream().forEach(method -> ReflectionUtils.invokeMethod(method, o));
		});
	}

	private List<Object> getObjectsInScope(TestExecutionContext testExecutionContext) {
		Predicate<Class<?>> forAllFilter = clazz -> AnnotationUtils.isAnnotated(clazz, ForAll.class);
		List<Class<?>> forAllClasses = ReflectionUtils.findInnerClasses(testExecutionContext.getTestClass().get(),
			forAllFilter);
		return forAllClasses.stream().map(clazz -> forAllObjects.get(testExecutionContext, clazz)).collect(
			Collectors.toList());
	}

	@Override
	public void postAfterAll(TestExecutionContext testExecutionContext) throws Exception {
		List<Object> objectsInScope = getObjectsInScope(testExecutionContext);
		objectsInScope.forEach(o -> {
			List<Method> afterAllMethods = AnnotationUtils.findAnnotatedMethods(o.getClass(), AfterAll.class,
				ReflectionUtils.MethodSortOrder.HierarchyUp);
			afterAllMethods.stream().forEach(method -> ReflectionUtils.invokeMethod(method, o));
		});

	}
}
