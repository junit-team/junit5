/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * Utility for sorting {@linkplain RegisteredExtensionPoint extension points}
 * according to their {@link Position}:
 * {@link Position#OUTERMOST OUTERMOST} &raquo;
 * {@link Position#OUTSIDE_DEFAULT OUTSIDE_DEFAULT} &raquo;
 * {@link Position#DEFAULT DEFAULT} &raquo;
 * {@link Position#INSIDE_DEFAULT INSIDE_DEFAULT} &raquo;
 * {@link Position#INNERMOST INNERMOST}.
 *
 * @since 5.0
 */
class ExtensionPointSorter {

	/**
	 * Sort the list of extension points according to their specified {@link Position}.
	 *
	 * <p>Note: the supplied list instance will be resorted.
	 *
	 * @param extensionPointType type of extension point to sort
	 * @param registeredExtensionPoints list of extension points in order of registration
	 * @param <T> concrete subtype of {@link ExtensionPoint}
	 */
	public <T extends ExtensionPoint> void sort(Class<T> extensionPointType,
			List<RegisteredExtensionPoint<T>> registeredExtensionPoints) {
		List<Position> uniquePositions = getUniquePositions(extensionPointType);
		uniquePositions.stream().forEach(position -> checkPositionUnique(registeredExtensionPoints, position));
		registeredExtensionPoints.sort(new DefaultComparator());
	}

	private <T extends ExtensionPoint> List<Position> getUniquePositions(Class<T> extensionPointType) {
		Optional<Field> allowedPositionsField = ReflectionUtils.findField(extensionPointType, "ALLOWED_POSITIONS");

		if (allowedPositionsField.isPresent()) {
			//TODO: Check for correct type of field etc.
			Position[] positions = (Position[]) ReflectionUtils.getFieldValue(allowedPositionsField.get(), null);
			return Arrays.stream(positions).filter(position -> position.shouldBeUnique()).collect(Collectors.toList());
		}

		return Arrays.asList();
	}

	private <T extends ExtensionPoint> void checkPositionUnique(
			List<RegisteredExtensionPoint<T>> registeredExtensionPoints, Position positionType) {

		if (countPosition(registeredExtensionPoints, positionType) > 1) {
			List<String> conflictingExtensions = conflictingExtensions(registeredExtensionPoints, positionType);
			String exceptionMessage = String.format("Conflicting extensions: %s", conflictingExtensions);
			throw new ExtensionConfigurationException(exceptionMessage);
		}
	}

	private <T extends ExtensionPoint> long countPosition(List<RegisteredExtensionPoint<T>> registeredExtensionPoints,
			Position positionToCount) {

		return registeredExtensionPoints.stream().filter(point -> point.getPosition() == positionToCount).count();
	}

	private <T extends ExtensionPoint> List<String> conflictingExtensions(
			List<RegisteredExtensionPoint<T>> registeredExtensionPoints, Position positionToFind) {

		// @formatter:off
		return registeredExtensionPoints.stream()
				.filter(point -> point.getPosition() == positionToFind)
				.map(RegisteredExtensionPoint::getSource)
				.map(Object::toString)
				.collect(Collectors.toList());
		// @formatter:on
	}

	private static class DefaultComparator implements Comparator<RegisteredExtensionPoint<?>> {

		@Override
		public int compare(RegisteredExtensionPoint<?> first, RegisteredExtensionPoint<?> second) {
			return Integer.compare(first.getPosition().ordinal(), second.getPosition().ordinal());
		}

	}

}
