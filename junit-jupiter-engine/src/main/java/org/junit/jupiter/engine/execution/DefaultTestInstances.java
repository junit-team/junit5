/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.TestInstances;

@API(status = INTERNAL, since = "5.4")
public class DefaultTestInstances implements TestInstances {

	public static DefaultTestInstances of(Object instance) {
		return new DefaultTestInstances(Collections.singletonList(instance));
	}

	public static DefaultTestInstances of(TestInstances testInstances, Object instance) {
		List<Object> allInstances = new ArrayList<>(testInstances.getAll());
		allInstances.add(instance);
		return new DefaultTestInstances(Collections.unmodifiableList(allInstances));
	}

	private final List<Object> instances;

	private DefaultTestInstances(List<Object> instances) {
		this.instances = instances;
	}

	@Override
	public Object getInnermost() {
		return instances.get(instances.size() - 1);
	}

	@Override
	public List<Object> getEnclosing() {
		return instances.size() <= 1 ? Collections.emptyList() : instances.subList(0, instances.size() - 1);
	}

	@Override
	public List<Object> getAll() {
		return instances;
	}

	@Override
	public <T> Optional<T> find(Class<T> requiredType) {
		ListIterator<Object> iterator = instances.listIterator(instances.size());
		while (iterator.hasPrevious()) {
			Object instance = iterator.previous();
			if (requiredType.isInstance(instance)) {
				return Optional.of(requiredType.cast(instance));
			}
		}
		return Optional.empty();
	}

}
