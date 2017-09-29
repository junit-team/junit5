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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.IOException;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.SourceVersion;

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
public class JigsawUtils {

	private static final Logger logger = LoggerFactory.getLogger(JigsawUtils.class);

	public static List<Class<?>> findAllClassesInModule(String moduleName, ClassFilter filter) {
		logger.config(() -> "findAllClassesInModule(" + moduleName + ")");
		return scan(boot(moduleName::equals), filter, JigsawUtils.class.getClassLoader());
	}

	// collect module references from boot module layer
	private static Set<ModuleReference> boot(Predicate<String> moduleNamePredicate) {
		Stream<ResolvedModule> stream = ModuleLayer.boot().configuration().modules().stream();
		stream = stream.filter(module -> moduleNamePredicate.test(module.name()));
		return stream.map(ResolvedModule::reference).collect(Collectors.toSet());
	}

	// scan for classes
	private static List<Class<?>> scan(Set<ModuleReference> references, ClassFilter filter, ClassLoader loader) {
		ModuleReferenceScanner scanner = new ModuleReferenceScanner(filter, loader);
		List<Class<?>> classes = new ArrayList<>();
		for (ModuleReference reference : references) {
			classes.addAll(scanner.scan(reference));
		}
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
							.filter(SourceVersion::isIdentifier)
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
			assert resourceName.endsWith(".class") : "resource name doesn't end with '.class': " + resourceName;
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
