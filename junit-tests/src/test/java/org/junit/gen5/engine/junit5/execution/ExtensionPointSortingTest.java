/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPoint.Position;

/**
 * Unit tests for {@link ExtensionPointSorter}.
 *
 * @since 5.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExtensionPointSortingTest {

	private ExtensionPointSorter sorter;
	private List<RegisteredExtensionPoint<LocalExtensionPoint>> pointsToSort;
	private int pointIndex = 0;

	@Before
	public void init() {
		sorter = new ExtensionPointSorter();
		pointsToSort = new ArrayList<>();
	}

	@Test
	public void positionDEFAULT_RemainInOrder() {
		RegisteredExtensionPoint point1 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point2 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point3 = createExtensionPoint(Position.DEFAULT);

		pointsToSort.add(point1);
		pointsToSort.add(point2);
		pointsToSort.add(point3);

		assertSorting(point1, point2, point3);
	}

	@Test
	public void positionOUTERMOST_AreSortedFirst() {
		RegisteredExtensionPoint point1 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point2 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point3 = createExtensionPoint(Position.OUTERMOST);

		pointsToSort.add(point1);
		pointsToSort.add(point2);
		pointsToSort.add(point3);

		assertSorting(point3, point1, point2);
	}

	@Test
	public void positionINNERMOST_AreSortedLast() {
		RegisteredExtensionPoint point1 = createExtensionPoint(Position.INNERMOST);
		RegisteredExtensionPoint point2 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point3 = createExtensionPoint(Position.DEFAULT);

		pointsToSort.add(point1);
		pointsToSort.add(point2);
		pointsToSort.add(point3);

		assertSorting(point2, point3, point1);
	}

	@Test
	public void positionOUTSIDEDEFAULT_AreSortedBetweenOutermostAndDefault() {
		RegisteredExtensionPoint point1 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point2 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point3 = createExtensionPoint(Position.OUTSIDE_DEFAULT);
		RegisteredExtensionPoint point4 = createExtensionPoint(Position.OUTERMOST);
		RegisteredExtensionPoint point5 = createExtensionPoint(Position.OUTSIDE_DEFAULT);

		pointsToSort.add(point1);
		pointsToSort.add(point2);
		pointsToSort.add(point3);
		pointsToSort.add(point4);
		pointsToSort.add(point5);

		assertSorting(point4, point3, point5, point1, point2);
	}

	@Test
	public void positionINSIDEDEFAULT_AreSortedBetweenDefaultAndInnermost() {
		RegisteredExtensionPoint point1 = createExtensionPoint(Position.DEFAULT);
		RegisteredExtensionPoint point2 = createExtensionPoint(Position.INNERMOST);
		RegisteredExtensionPoint point3 = createExtensionPoint(Position.INSIDE_DEFAULT);
		RegisteredExtensionPoint point4 = createExtensionPoint(Position.INSIDE_DEFAULT);
		RegisteredExtensionPoint point5 = createExtensionPoint(Position.DEFAULT);

		pointsToSort.add(point1);
		pointsToSort.add(point2);
		pointsToSort.add(point3);
		pointsToSort.add(point4);
		pointsToSort.add(point5);

		assertSorting(point1, point5, point3, point4, point2);
	}

	@Test
	public void twoExtensions_withPositionINNERMOST_throwException() {
		RegisteredExtensionPoint point1 = createExtensionPoint(Position.INNERMOST);
		RegisteredExtensionPoint point2 = createExtensionPoint(Position.INNERMOST);

		pointsToSort.add(point1);
		pointsToSort.add(point2);

		expectThrows(ExtensionConfigurationException.class, () -> sorter.sort(pointsToSort));
	}

	@Test
	public void twoExtensions_withPositionOUTERMOST_throwException() {
		RegisteredExtensionPoint point1 = createExtensionPoint(Position.OUTERMOST);
		RegisteredExtensionPoint point2 = createExtensionPoint(Position.OUTERMOST);

		pointsToSort.add(point1);
		pointsToSort.add(point2);

		expectThrows(ExtensionConfigurationException.class, () -> sorter.sort(pointsToSort));
	}

	protected void assertSorting(RegisteredExtensionPoint... points) {
		sorter.sort(pointsToSort);

		String failureMessage = String.format("Expected %s but was %s", Arrays.asList(points), pointsToSort);

		assertEquals(points.length, pointsToSort.size(), failureMessage);
		for (int i = 0; i < points.length; i++) {
			assertTrue(points[i] == pointsToSort.get(i), failureMessage);
		}
	}

	protected RegisteredExtensionPoint<LocalExtensionPoint> createExtensionPoint(Position position) {
		return new RegisteredExtensionPoint<>(() -> {
		}, position, "" + ++pointIndex);
	}

	@FunctionalInterface
	interface LocalExtensionPoint extends ExtensionPoint {
		void doSomething();
	}

}
