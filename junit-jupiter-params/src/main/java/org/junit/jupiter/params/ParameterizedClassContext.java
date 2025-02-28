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
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;
import static org.junit.platform.commons.util.ReflectionUtils.isRecordClass;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.util.ReflectionUtils;

class ParameterizedClassContext implements ParameterizedDeclarationContext<ContainerTemplateInvocationContext> {

	private final Class<?> clazz;
	private final ParameterizedClass annotation;
	private final TestInstance.Lifecycle testInstanceLifecycle;
	private final ResolverFacade resolverFacade;
	private final InjectionType injectionType;

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
	public ContainerTemplateInvocationContext createInvocationContext(ParameterizedInvocationNameFormatter formatter,
			Arguments arguments, int invocationIndex) {
		return new ParameterizedClassInvocationContext(this, formatter, arguments, invocationIndex);
	}

	TestInstance.Lifecycle getTestInstanceLifecycle() {
		return testInstanceLifecycle;
	}

	InjectionType getInjectionType() {
		return injectionType;
	}

	enum InjectionType {
		CONSTRUCTOR, FIELDS
	}
}
