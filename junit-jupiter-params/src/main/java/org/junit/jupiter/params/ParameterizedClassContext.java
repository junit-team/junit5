/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static java.util.Collections.emptyList;
import static java.util.Collections.reverse;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedMethods;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;
import static org.junit.platform.commons.util.ReflectionUtils.isRecordClass;
import static org.junit.platform.commons.util.ReflectionUtils.returnsPrimitiveVoid;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.ReflectionUtils;

class ParameterizedClassContext implements ParameterizedDeclarationContext<ClassTemplateInvocationContext> {

	private final Class<?> clazz;
	private final ParameterizedClass annotation;
	private final TestInstance.Lifecycle testInstanceLifecycle;
	private final ResolverFacade resolverFacade;
	private final InjectionType injectionType;
	private final List<ArgumentSetLifecycleMethod> beforeMethods;
	private final List<ArgumentSetLifecycleMethod> afterMethods;

	ParameterizedClassContext(Class<?> clazz, ParameterizedClass annotation,
			TestInstance.Lifecycle testInstanceLifecycle) {
		this.clazz = clazz;
		this.annotation = annotation;
		this.testInstanceLifecycle = testInstanceLifecycle;

		List<Field> fields = findParameterAnnotatedFields(clazz);
		if (fields.isEmpty()) {
			this.resolverFacade = ResolverFacade.create(ReflectionUtils.getDeclaredConstructor(clazz), annotation);
			this.injectionType = InjectionType.CONSTRUCTOR;
		}
		else {
			this.resolverFacade = ResolverFacade.create(clazz, fields);
			this.injectionType = InjectionType.FIELDS;
		}

		this.beforeMethods = findLifecycleMethodsAndAssertStaticAndNonPrivate(clazz, testInstanceLifecycle, TOP_DOWN,
			BeforeParameterizedClassInvocation.class, BeforeParameterizedClassInvocation::injectArguments,
			this.resolverFacade);

		// Make a local copy since findAnnotatedMethods() returns an immutable list.
		this.afterMethods = new ArrayList<>(findLifecycleMethodsAndAssertStaticAndNonPrivate(clazz,
			testInstanceLifecycle, BOTTOM_UP, AfterParameterizedClassInvocation.class,
			AfterParameterizedClassInvocation::injectArguments, this.resolverFacade));

		// Since the bottom-up ordering of afterMethods will later be reversed when the
		// AfterParameterizedClassInvocationMethodInvoker extensions are executed within
		// ClassTemplateInvocationTestDescriptor, we have to reverse them to put them
		// in top-down order before we register them as extensions.
		reverse(afterMethods);
	}

	private static List<Field> findParameterAnnotatedFields(Class<?> clazz) {
		if (isRecordClass(clazz)) {
			return emptyList();
		}
		return findFields(clazz, it -> isAnnotated(it, Parameter.class), BOTTOM_UP);
	}

	@Override
	public ParameterizedClass getAnnotation() {
		return this.annotation;
	}

	@Override
	public Class<?> getAnnotatedElement() {
		return this.clazz;
	}

	@Override
	public String getDisplayNamePattern() {
		return this.annotation.name();
	}

	@Override
	public boolean isAutoClosingArguments() {
		return this.annotation.autoCloseArguments();
	}

	@Override
	public boolean isAllowingZeroInvocations() {
		return this.annotation.allowZeroInvocations();
	}

	@Override
	public ArgumentCountValidationMode getArgumentCountValidationMode() {
		return this.annotation.argumentCountValidation();
	}

	@Override
	public ResolverFacade getResolverFacade() {
		return this.resolverFacade;
	}

	@Override
	public ClassTemplateInvocationContext createInvocationContext(ParameterizedInvocationNameFormatter formatter,
			Arguments arguments, int invocationIndex) {
		return new ParameterizedClassInvocationContext(this, formatter, arguments, invocationIndex);
	}

	TestInstance.Lifecycle getTestInstanceLifecycle() {
		return testInstanceLifecycle;
	}

	InjectionType getInjectionType() {
		return injectionType;
	}

	List<ArgumentSetLifecycleMethod> getBeforeMethods() {
		return beforeMethods;
	}

	List<ArgumentSetLifecycleMethod> getAfterMethods() {
		return afterMethods;
	}

	private static <A extends Annotation> List<ArgumentSetLifecycleMethod> findLifecycleMethodsAndAssertStaticAndNonPrivate(
			Class<?> testClass, TestInstance.Lifecycle testInstanceLifecycle, HierarchyTraversalMode traversalMode,
			Class<A> annotationType, Predicate<A> injectArgumentsPredicate, ResolverFacade resolverFacade) {

		List<Method> methods = findMethodsAndCheckVoidReturnTypeAndNonPrivate(testClass, annotationType, traversalMode);

		return methods.stream() //
				.peek(method -> {
					if (testInstanceLifecycle != PER_CLASS) {
						assertStatic(annotationType, method);
					}
				}) //
				.map(method -> {
					A annotation = getAnnotation(method, annotationType);
					if (injectArgumentsPredicate.test(annotation)) {
						return new ArgumentSetLifecycleMethod(method,
							resolverFacade.createLifecycleMethodParameterResolver(method, annotation));
					}
					return new ArgumentSetLifecycleMethod(method);
				}) //
				.collect(toUnmodifiableList());
	}

	private static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
		return AnnotationSupport.findAnnotation(method, annotationType) //
				.orElseThrow(() -> new JUnitException("Method not annotated with @" + annotationType.getSimpleName()));
	}

	private static List<Method> findMethodsAndCheckVoidReturnTypeAndNonPrivate(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode) {

		List<Method> methods = findAnnotatedMethods(testClass, annotationType, traversalMode);
		methods.forEach(method -> {
			assertVoid(annotationType, method);
			assertNonPrivate(annotationType, method);
		});
		return methods;
	}

	private static void assertStatic(Class<? extends Annotation> annotationType, Method method) {
		if (ModifierSupport.isNotStatic(method)) {
			throw new JUnitException(String.format(
				"@%s method '%s' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS).",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	private static void assertVoid(Class<? extends Annotation> annotationType, Method method) {
		if (!returnsPrimitiveVoid(method)) {
			throw new JUnitException(String.format("@%s method '%s' must not return a value.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	private static void assertNonPrivate(Class<? extends Annotation> annotationType, Method method) {
		if (ModifierSupport.isPrivate(method)) {
			throw new JUnitException(String.format("@%s method '%s' must not be private.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	enum InjectionType {
		CONSTRUCTOR, FIELDS
	}
}
