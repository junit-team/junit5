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

import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;

/**
 * Utility for sorting {@linkplain RegisteredExtension extensions}
 * according to their {@link Position}:
 * {@link Position#OUTERMOST OUTERMOST} &raquo;
 * {@link Position#OUTSIDE_DEFAULT OUTSIDE_DEFAULT} &raquo;
 * {@link Position#DEFAULT DEFAULT} &raquo;
 * {@link Position#INSIDE_DEFAULT INSIDE_DEFAULT} &raquo;
 * {@link Position#INNERMOST INNERMOST}.
 *
 * @since 5.0
 */
class ExtensionSorter {

	/**
	 * Sort the list of extension according to their specified {@link Position}.
	 *
	 * <p>Note: the supplied list instance will be resorted.
	 *
	 * @param registeredExtensions list of extension in order of registration
	 * @param <T> concrete subtype of {@link Extension}
	 */
	public <T extends Extension> void sort(List<RegisteredExtension<T>> registeredExtensions) {
		checkPositionUnique(registeredExtensions, Position.INNERMOST);
		checkPositionUnique(registeredExtensions, Position.OUTERMOST);
		registeredExtensions.sort(null);
	}

	private <T extends Extension> void checkPositionUnique(List<RegisteredExtension<T>> registeredExtensions,
			Position positionType) {

		if (countPosition(registeredExtensions, positionType) > 1) {
			List<String> conflictingExtensions = conflictingExtensions(registeredExtensions, positionType);
			String exceptionMessage = String.format("Conflicting extensions: %s", conflictingExtensions);
			throw new ExtensionConfigurationException(exceptionMessage);
		}
	}

	private <T extends Extension> long countPosition(List<RegisteredExtension<T>> registeredExtensions,
			Position positionToCount) {

		return registeredExtensions.stream().filter(extension -> extension.getPosition() == positionToCount).count();
	}

	private <T extends Extension> List<String> conflictingExtensions(List<RegisteredExtension<T>> registeredExtensions,
			Position positionToFind) {

		// @formatter:off
		return registeredExtensions.stream()
				.filter(extension -> extension.getPosition() == positionToFind)
				.map(RegisteredExtension::getSource)
				.map(Object::toString)
				.collect(Collectors.toList());
		// @formatter:on
	}

}
