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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * Unit tests for {@link ExtensionSorter}.
 *
 * @since 5.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExtensionSortingTests {

	private ExtensionSorter sorter;
	private List<RegisteredExtension<LocalExtension>> extensionsToSort;

	@BeforeEach
	void init() {
		sorter = new ExtensionSorter();
		extensionsToSort = new ArrayList<>();
	}

	@Test
	void positionDEFAULT_RemainInOrder() {
		RegisteredExtension extension1 = createExtension(Position.DEFAULT);
		RegisteredExtension extension2 = createExtension(Position.DEFAULT);
		RegisteredExtension extension3 = createExtension(Position.DEFAULT);

		extensionsToSort.add(extension1);
		extensionsToSort.add(extension2);
		extensionsToSort.add(extension3);

		assertSorting(extension1, extension2, extension3);
	}

	@Test
	void positionOUTERMOST_AreSortedFirst() {
		RegisteredExtension extension1 = createExtension(Position.DEFAULT);
		RegisteredExtension extension2 = createExtension(Position.DEFAULT);
		RegisteredExtension extension3 = createExtension(Position.OUTERMOST);

		extensionsToSort.add(extension1);
		extensionsToSort.add(extension2);
		extensionsToSort.add(extension3);

		assertSorting(extension3, extension1, extension2);
	}

	@Test
	void positionINNERMOST_AreSortedLast() {
		RegisteredExtension extension1 = createExtension(Position.INNERMOST);
		RegisteredExtension extension2 = createExtension(Position.DEFAULT);
		RegisteredExtension extension3 = createExtension(Position.DEFAULT);

		extensionsToSort.add(extension1);
		extensionsToSort.add(extension2);
		extensionsToSort.add(extension3);

		assertSorting(extension2, extension3, extension1);
	}

	@Test
	void positionOUTSIDEDEFAULT_AreSortedBetweenOutermostAndDefault() {
		RegisteredExtension extension1 = createExtension(Position.DEFAULT);
		RegisteredExtension extension2 = createExtension(Position.DEFAULT);
		RegisteredExtension extension3 = createExtension(Position.OUTSIDE_DEFAULT);
		RegisteredExtension extension4 = createExtension(Position.OUTERMOST);
		RegisteredExtension extension5 = createExtension(Position.OUTSIDE_DEFAULT);

		extensionsToSort.add(extension1);
		extensionsToSort.add(extension2);
		extensionsToSort.add(extension3);
		extensionsToSort.add(extension4);
		extensionsToSort.add(extension5);

		assertSorting(extension4, extension3, extension5, extension1, extension2);
	}

	@Test
	void positionINSIDEDEFAULT_AreSortedBetweenDefaultAndInnermost() {
		RegisteredExtension extension1 = createExtension(Position.DEFAULT);
		RegisteredExtension extension2 = createExtension(Position.INNERMOST);
		RegisteredExtension extension3 = createExtension(Position.INSIDE_DEFAULT);
		RegisteredExtension extension4 = createExtension(Position.INSIDE_DEFAULT);
		RegisteredExtension extension5 = createExtension(Position.DEFAULT);

		extensionsToSort.add(extension1);
		extensionsToSort.add(extension2);
		extensionsToSort.add(extension3);
		extensionsToSort.add(extension4);
		extensionsToSort.add(extension5);

		assertSorting(extension1, extension5, extension3, extension4, extension2);
	}

	@Test
	void twoExtensions_withPositionINNERMOST_throwException() {
		RegisteredExtension extension1 = createExtension(Position.INNERMOST);
		RegisteredExtension extension2 = createExtension(Position.INNERMOST);

		extensionsToSort.add(extension1);
		extensionsToSort.add(extension2);

		ExtensionConfigurationException ex = expectThrows(ExtensionConfigurationException.class,
			() -> sorter.sort(extensionsToSort));

		assertTrue(ex.getMessage().startsWith("Conflicting extensions:"));
		assertTrue(ex.getMessage().contains(fooMethod.toString()));
	}

	@Test
	void twoExtensions_withPositionOUTERMOST_throwException() {
		RegisteredExtension extension1 = createExtension(Position.OUTERMOST);
		RegisteredExtension extension2 = createExtension(Position.OUTERMOST);

		extensionsToSort.add(extension1);
		extensionsToSort.add(extension2);

		ExtensionConfigurationException ex = expectThrows(ExtensionConfigurationException.class,
			() -> sorter.sort(extensionsToSort));

		assertTrue(ex.getMessage().startsWith("Conflicting extensions:"));
		assertTrue(ex.getMessage().contains(fooMethod.toString()));
	}

	private void assertSorting(RegisteredExtension... extensions) {
		sorter.sort(extensionsToSort);

		String failureMessage = String.format("Expected %s but was %s", Arrays.asList(extensions), extensionsToSort);

		assertEquals(extensions.length, extensionsToSort.size(), failureMessage);
		for (int i = 0; i < extensions.length; i++) {
			assertTrue(extensions[i] == extensionsToSort.get(i), failureMessage);
		}
	}

	private RegisteredExtension<LocalExtension> createExtension(Position position) {
		return new RegisteredExtension<>(this::foo, fooMethod, position);
	}

	/**
	 * "Implements" LocalExtension.
	 */
	void foo() {
		/* no-op */
	}

	final Method fooMethod = ReflectionUtils.findMethod(getClass(), "foo").get();

	@FunctionalInterface
	interface LocalExtension extends Extension {

		void doSomething();
	}

}
