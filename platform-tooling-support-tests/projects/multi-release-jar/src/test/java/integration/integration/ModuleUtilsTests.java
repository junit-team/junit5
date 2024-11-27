/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.module.ModuleDescriptor;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ModuleUtils;

/**
 * Unit tests for {@link ModuleUtils}.
 *
 * @since 1.1
 */
class ModuleUtilsTests {

	@Test
	void javaPlatformModuleSystemIsAvailable() {
		assertTrue(ModuleUtils.isJavaPlatformModuleSystemAvailable());
	}

	@Test
	void findAllNonSystemBootModuleNames() {
		Set<String> moduleNames = ModuleUtils.findAllNonSystemBootModuleNames();

		assertTrue(moduleNames.isEmpty());
	}

	@Test
	void findAllClassesInModule() {
		ClassFilter modular = ClassFilter.of(name -> name.contains("Module"), type -> true);
		List<Class<?>> classes = ModuleUtils.findAllClassesInModule("java.base", modular);
		assertFalse(classes.isEmpty());
		assertTrue(classes.contains(Module.class));
		assertTrue(classes.contains(ModuleDescriptor.class));
	}

	@Test
	void preconditions() {
		Class<PreconditionViolationException> expected = PreconditionViolationException.class;
		assertThrows(expected, () -> ModuleUtils.getModuleName(null));
		assertThrows(expected, () -> ModuleUtils.getModuleVersion(null));
		assertThrows(expected, () -> ModuleUtils.findAllClassesInModule(null, null));
		assertThrows(expected, () -> ModuleUtils.findAllClassesInModule("", null));
		assertThrows(expected, () -> ModuleUtils.findAllClassesInModule(" ", null));
		assertThrows(expected, () -> ModuleUtils.findAllClassesInModule("java.base", null));
	}
}
