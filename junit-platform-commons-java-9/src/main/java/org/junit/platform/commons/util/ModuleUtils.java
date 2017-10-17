/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * Collection of utilities for working with {@code java.lang.Module}
 * and friends.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public class ModuleUtils {

	/**
	 * Version hint is set to {@code "9"} here.
	 */
	public static final String VERSION = "9";

	private static final Logger logger = LoggerFactory.getLogger(ModuleUtils.class);

	/**
	 * Find all non-system boot modules names.
	 *
	 * @return a set of all such module names; never {@code null} but
	 * potentially empty
	 */
	public static Set<String> findAllNonSystemBootModuleNames() {
		// @formatter:off
		Set<String> systemModules = ModuleFinder.ofSystem().findAll().stream()
				.map(reference -> reference.descriptor().name())
				.collect(toSet());
		return boot(name -> !systemModules.contains(name))
				.map(ResolvedModule::name)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	/**
	 * Find all classes for the given module name.
	 *
	 * @param moduleName name of the module to scan
	 * @param filter class filter to apply
	 * @return an immutable list of all such classes found; never {@code null}
	 * but potentially empty
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, ClassFilter filter) {
		Preconditions.notBlank(moduleName, "Module name must not be null or empty");
		Preconditions.notNull(filter, "Class filter must not be null");

		logger.debug(() -> "Looking for classes in module: " + moduleName);
		// @formatter:off
		Set<ModuleReference> moduleReferences = boot(isEqual(moduleName))
				.map(ResolvedModule::reference)
				.collect(toSet());
		// @formatter:on
		return scan(moduleReferences, filter, ModuleUtils.class.getClassLoader());
	}

	// collect module references from boot module layer
	private static Stream<ResolvedModule> boot(Predicate<String> moduleNamePredicate) {
		Stream<ResolvedModule> stream = ModuleLayer.boot().configuration().modules().stream();
		return stream.filter(module -> moduleNamePredicate.test(module.name()));
	}

	// scan for classes
	private static List<Class<?>> scan(Set<ModuleReference> references, ClassFilter filter, ClassLoader loader) {
		logger.debug(() -> "Scanning " + references.size() + " module references: " + references);
		ModuleReferenceScanner scanner = new ModuleReferenceScanner(filter, loader);
		List<Class<?>> classes = new ArrayList<>();
		for (ModuleReference reference : references) {
			classes.addAll(scanner.scan(reference));
		}
		logger.debug(() -> "Found " + classes.size() + " classes: " + classes);
		return Collections.unmodifiableList(classes);
	}

	/**
	 * Module reference scanner.
	 */
	static class ModuleReferenceScanner {

		private final ClassFilter classFilter;
		private final ClassLoader classLoader;

		ModuleReferenceScanner(ClassFilter classFilter, ClassLoader classLoader) {
			this.classFilter = classFilter;
			this.classLoader = classLoader;
		}

		/**
		 * Scan module reference for classes that potentially contain testable methods.
		 */
		List<Class<?>> scan(ModuleReference reference) {
			try (ModuleReader reader = reference.open()) {
				try (Stream<String> names = reader.list()) {
					// @formatter:off
					return names.filter(name -> name.endsWith(".class"))
							.map(this::className)
							.filter(name -> !name.equals("module-info"))
							.filter(classFilter::match)
							.map(this::loadClassUnchecked)
							.filter(classFilter::match)
							.collect(Collectors.toList());
					// @formatter:on
				}
			}
			catch (IOException e) {
				throw new JUnitException("reading contents of " + reference + " failed", e);
			}
		}

		/** Convert resource name to binary class name. */
		private String className(String resourceName) {
			resourceName = resourceName.substring(0, resourceName.length() - 6); // 6 = ".class".length()
			resourceName = resourceName.replace('/', '.');
			return resourceName;
		}

		/**
		 * Load class by its binary name.
		 *
		 * @see ClassLoader#loadClass(String)
		 */
		private Class<?> loadClassUnchecked(String binaryName) {
			try {
				return classLoader.loadClass(binaryName);
			}
			catch (ClassNotFoundException e) {
				throw new JUnitException("loading class with name '" + binaryName + "' failed", e);
			}
		}

	}
}
