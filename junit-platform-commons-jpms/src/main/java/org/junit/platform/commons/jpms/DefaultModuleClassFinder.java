/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.jpms;

import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ModuleClassFinder;
import org.junit.platform.commons.util.Preconditions;

/**
 * Default module class finder implementation.
 *
 * @see ModuleClassFinder
 * @since 1.1
 */
@API(status = API.Status.INTERNAL)
public class DefaultModuleClassFinder implements ModuleClassFinder {

	@Override
	public List<Class<?>> findAllClassesInModule(ClassFilter filter, String moduleName) {
		Preconditions.notNull(filter, "class filter must not be null");
		Preconditions.notBlank(moduleName, "module name must not be null or blank");

		return scan(boot(moduleName::equals), filter);
	}

	@Override
	public List<Class<?>> findAllClassesOnModulePath(ClassFilter filter) {
		Preconditions.notNull(filter, "class filter must not be null");
		return scan(boot(name -> true), filter);
	}

	@Override
	public List<Class<?>> findAllClassesOnModulePath(ClassFilter filter, ClassLoader parent, Path... entries) {
		Preconditions.notNull(filter, "class filter must not be null");
		Preconditions.notNull(parent, "parent class loader must not be null");
		Preconditions.notNull(entries, "path entries must not be null");

		return scan(ModuleFinder.of(entries).findAll(), filter, parent);
	}

	// collect module references
	private Set<ModuleReference> boot(Predicate<String> moduleNamePredicate) {
		Stream<ResolvedModule> stream = ModuleLayer.boot().configuration().modules().stream();
		stream = stream.filter(module -> moduleNamePredicate.test(module.name()));
		return stream.map(ResolvedModule::reference).collect(Collectors.toSet());
	}

	private List<Class<?>> scan(Collection<ModuleReference> references, ClassFilter filter) {
		return scan(references, filter, getClass().getClassLoader());
	}

	// scan for classes
	private List<Class<?>> scan(Collection<ModuleReference> references, ClassFilter filter, ClassLoader loader) {
		ModuleReferenceScanner scanner = new ModuleReferenceScanner(filter, loader);
		List<Class<?>> classes = new ArrayList<>();
		for (ModuleReference reference : references) {
			classes.addAll(scanner.scan(reference));
		}
		return Collections.unmodifiableList(classes);
	}

}
