/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.stream.Collectors.toCollection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * @since 5.0
 */
abstract class AbstractExtensionContext<T extends TestDescriptor> implements ExtensionContext, ExtensionContext.Util {

	private final ExtensionContext parent;
	private final EngineExecutionListener engineExecutionListener;
	private final T testDescriptor;
	private final ExtensionValuesStore valuesStore;

	AbstractExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener,
			T testDescriptor) {
		this.parent = parent;
		this.engineExecutionListener = engineExecutionListener;
		this.testDescriptor = testDescriptor;
		this.valuesStore = createStore(parent);
	}

	private ExtensionValuesStore createStore(ExtensionContext parent) {
		ExtensionValuesStore parentStore = null;
		if (parent != null) {
			parentStore = ((AbstractExtensionContext<?>) parent).valuesStore;
		}
		return new ExtensionValuesStore(parentStore);
	}

	@Override
	public void publishReportEntry(Map<String, String> values) {
		engineExecutionListener.reportingEntryPublished(this.testDescriptor, ReportEntry.from(values));
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	protected T getTestDescriptor() {
		return testDescriptor;
	}

	@Override
	public Store getStore(Namespace namespace) {
		Preconditions.notNull(namespace, "Namespace must not be null");
		return new NamespaceAwareStore(valuesStore, namespace);
	}

	@Override
	public Set<String> getTags() {
		return testDescriptor.getTags().stream().map(TestTag::getName).collect(toCollection(LinkedHashSet::new));
	}

	@Override
	public Util getUtil() {
		// The current implementation is done "in same instance".
		// If more methods are added, consider creating a dedicated Util class.
		return this;
	}

	@Override
	public boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return AnnotationUtils.isAnnotated(element, annotationType);
	}

	@Override
	public <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		return AnnotationUtils.findAnnotation(element, annotationType);
	}

	@Override
	public <A extends Annotation> List<A> findRepeatableAnnotations(AnnotatedElement element, Class<A> annotationType) {
		return AnnotationUtils.findRepeatableAnnotations(element, annotationType);
	}

	@Override
	public List<Field> findPublicAnnotatedFields(Class<?> clazz, Class<?> fieldType,
			Class<? extends Annotation> annotationType) {
		return AnnotationUtils.findPublicAnnotatedFields(clazz, fieldType, annotationType);
	}

	@Override
	public List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType,
			boolean sortOrderHierarchyDown) {
		MethodSortOrder order = sortOrderHierarchyDown ? MethodSortOrder.HierarchyDown : MethodSortOrder.HierarchyUp;
		return AnnotationUtils.findAnnotatedMethods(clazz, annotationType, order);
	}

}
