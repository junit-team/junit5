/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder.forClass;
import static org.junit.gen5.engine.dsl.TestPlanSpecificationBuilder.testPlanSpecification;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.gen5.commons.util.Preconditions;

/**
 * @since 5.0
 */
public final class TestPlanSpecification {
	private final List<TestPlanSpecificationElement> elements = new LinkedList<>();

	// Descriptor Filters are evaluated by the launcher itself after engines have done their discovery.
	// Begin predicate chain with a predicate that always evaluates to true.
	private final List<Predicate<TestDescriptor>> descriptorFilters = new LinkedList<>();

	// Engine filters are handed through to all test engines to be applied during discovery
	private final List<EngineFilter> engineFilters = new LinkedList<>();

	public void addElement(TestPlanSpecificationElement element) {
		this.elements.add(element);
	}

	public void addElements(Collection<TestPlanSpecificationElement> elements) {
		elements.forEach(this::addElement);
	}

	public void addEngineFilter(EngineFilter engineFilter) {
		this.engineFilters.add(engineFilter);
	}

	public void addEngineFilters(Collection<EngineFilter> engineFilters) {
		this.engineFilters.addAll(engineFilters);
	}

	public void addDescriptorFilter(Predicate<TestDescriptor> desciptorFilter) {
		this.descriptorFilters.add(desciptorFilter);
	}

	public void addDescriptorFilters(Collection<Predicate<TestDescriptor>> desciptorFilters) {
		this.descriptorFilters.addAll(desciptorFilters);
	}

	public List<TestPlanSpecificationElement> getElements() {
		return unmodifiableList(this.elements);
	}

	public <T extends TestPlanSpecificationElement> List<T> getElementsByType(Class<T> filterType) {
		return this.elements.stream().filter(filterType::isInstance).map(filterType::cast).collect(toList());
	}

	public List<EngineFilter> getEngineFilters() {
		return unmodifiableList(this.engineFilters);
	}

	public <T extends EngineFilter> List<T> getEngineFiltersByType(Class<T> filterType) {
		return this.engineFilters.stream().filter(filterType::isInstance).map(filterType::cast).collect(toList());
	}

	public List<Predicate<TestDescriptor>> getDescriptorFilters() {
		return unmodifiableList(this.descriptorFilters);
	}

	public ClassFilter getClassFilter() {
		return ClassFilters.allOf(getEngineFiltersByType(ClassFilter.class));
	}

	public boolean acceptDescriptor(TestDescriptor testDescriptor) {
		Preconditions.notNull(testDescriptor, "testDescriptor must not be null");
		return this.getDescriptorFilters().stream().allMatch(filter -> filter.test(testDescriptor));
	}

	public void accept(TestPlanSpecificationElementVisitor visitor) {
		this.getElements().forEach(element -> element.accept(visitor));
	}
}
