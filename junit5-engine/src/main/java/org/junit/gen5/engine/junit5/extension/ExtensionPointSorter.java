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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;

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
	 * @param registeredExtensionPoints list of extension points in order of registration
	 * @param <T> concrete subtype of {@link ExtensionPoint}
	 */
	public <T extends ExtensionPoint> void sort(List<RegisteredExtensionPoint<T>> registeredExtensionPoints) {
		checkPositionUnique(registeredExtensionPoints, Position.INNERMOST);
		checkPositionUnique(registeredExtensionPoints, Position.OUTERMOST);
		registeredExtensionPoints.sort(null);
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

}
