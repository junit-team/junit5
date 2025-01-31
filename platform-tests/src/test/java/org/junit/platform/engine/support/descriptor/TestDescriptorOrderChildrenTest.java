/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

public interface TestDescriptorOrderChildrenTest {

	/**
	 * @return a test descriptor without any children.
	 */
	TestDescriptor createEmptyTestDescriptor();

	default TestDescriptor createTestDescriptorWithChildren() {
		var testDescriptor = createEmptyTestDescriptor();
		testDescriptor.addChild(new StubTestDescriptor(UniqueId.root("child", "0")));
		testDescriptor.addChild(new StubTestDescriptor(UniqueId.root("child", "1")));
		testDescriptor.addChild(new StubTestDescriptor(UniqueId.root("child", "2")));
		return testDescriptor;
	}

	@Test
	default void orderChildrenInReverseOrder() {
		var testDescriptor = createTestDescriptorWithChildren();
		var childrenInOriginalOrder = new ArrayList<>(testDescriptor.getChildren());
		testDescriptor.orderChildren(children -> {
			children.sort(comparing((TestDescriptor o) -> childrenInOriginalOrder.indexOf(o)).reversed());
			return children;
		});
		List<TestDescriptor> children = new ArrayList<>(testDescriptor.getChildren());
		assertThat(children).isEqualTo(childrenInOriginalOrder.reversed());
	}

	@Test
	default void orderChildrenEmptyList() {
		var testDescriptor = createTestDescriptorWithChildren();
		var childrenInOriginalOrder = new ArrayList<>(testDescriptor.getChildren());
		testDescriptor.orderChildren(children -> emptyList());
		List<TestDescriptor> children = new ArrayList<>(testDescriptor.getChildren());
		assertThat(children).isEqualTo(childrenInOriginalOrder);
	}

	@Test
	default void orderChildrenInSameOrder() {
		var testDescriptor = createTestDescriptorWithChildren();
		var childrenInOriginalOrder = new ArrayList<>(testDescriptor.getChildren());
		testDescriptor.orderChildren(children -> {
			children.sort(comparing(childrenInOriginalOrder::indexOf));
			return children;
		});
		List<TestDescriptor> children = new ArrayList<>(testDescriptor.getChildren());
		assertThat(children).isEqualTo(childrenInOriginalOrder);
	}

	@Test
	default void orderChildrenRemovesDescriptor() {
		var testDescriptor = createTestDescriptorWithChildren();
		var childrenInOriginalOrder = new ArrayList<>(testDescriptor.getChildren());
		testDescriptor.orderChildren(children -> {
			children.remove(1);
			return children;
		});
		List<TestDescriptor> children = new ArrayList<>(testDescriptor.getChildren());
		assertThat(children).isEqualTo(List.of(//
			childrenInOriginalOrder.get(1), //
			childrenInOriginalOrder.get(0), //
			childrenInOriginalOrder.get(2))//
		);
	}

	@Test
	default void orderChildrenAddsDescriptor() {
		var testDescriptor = createTestDescriptorWithChildren();
		var childrenInOriginalOrder = new ArrayList<>(testDescriptor.getChildren());
		testDescriptor.orderChildren(children -> {
			children.add(1, new StubTestDescriptor(UniqueId.root("extra", "extra1")));
			return children;
		});
		List<TestDescriptor> children = new ArrayList<>(testDescriptor.getChildren());
		assertThat(children).isEqualTo(childrenInOriginalOrder);
	}

	@Test
	default void orderChildrenReplacesDescriptor() {
		var testDescriptor = createTestDescriptorWithChildren();
		var childrenInOriginalOrder = new ArrayList<>(testDescriptor.getChildren());

		testDescriptor.orderChildren(children -> {
			children.set(1, new StubTestDescriptor(UniqueId.root("replaced", "replaced1")));
			return children;
		});
		List<TestDescriptor> children = new ArrayList<>(testDescriptor.getChildren());
		assertThat(children).isEqualTo(List.of(//
			childrenInOriginalOrder.get(1), //
			childrenInOriginalOrder.get(0), //
			childrenInOriginalOrder.get(2))//
		);
	}

	@Test
	default void orderChildrenDuplicatesDescriptor() {
		var testDescriptor = createTestDescriptorWithChildren();
		var childrenInOriginalOrder = new ArrayList<>(testDescriptor.getChildren());
		testDescriptor.orderChildren(children -> {
			children.add(1, children.getLast());
			return children;
		});
		List<TestDescriptor> children = new ArrayList<>(testDescriptor.getChildren());
		assertThat(children).isEqualTo(List.of(//
			childrenInOriginalOrder.get(0), //
			childrenInOriginalOrder.get(2), //
			childrenInOriginalOrder.get(1))//
		);
	}

	@Test
	default void orderChildrenOrdererReturnsNull() {
		var testDescriptor = createTestDescriptorWithChildren();
		var exception = assertThrows(PreconditionViolationException.class,
			() -> testDescriptor.orderChildren(children -> null));
		assertThat(exception).hasMessage("orderer may not return null");
	}

	@Test
	default void orderChildrenProvidedChildrenAreModifiable() {
		var testDescriptor = createTestDescriptorWithChildren();
		AtomicReference<List<TestDescriptor>> childrenRef = new AtomicReference<>();
		testDescriptor.orderChildren(children -> {
			childrenRef.set(children);
			return children;
		});
		assertThat(childrenRef.get()).isInstanceOf(ArrayList.class);
	}
}

class StubTestDescriptor extends AbstractTestDescriptor {

	StubTestDescriptor(UniqueId uniqueId) {
		super(uniqueId, "stub: " + uniqueId);
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

}
