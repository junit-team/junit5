/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.platform.commons.util.ReflectionUtils;

public class AggregatingConverter extends SimpleArgumentConverter {

	private static final Map<Class<?>, AggregatingConverterHelper> CONVERTERS;

	static {
		Map<Class<?>, AggregatingConverterHelper> converters = new HashMap<>();
		converters.put(Collection.class, new CollectionConverter());
		converters.put(Map.class, new MapConverter());
		CONVERTERS = Collections.unmodifiableMap(converters);
	}

	@Override
	protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {

		// Safe : arguments always passed as an array
		Object[] arguments = (Object[]) source;

		Optional<AggregatingConverterHelper> converter = CONVERTERS.entrySet().stream().map(Entry::getValue).filter(
			c -> c.canConvert(targetType)).findFirst();

		if (converter.isPresent()) {
			try {
				return converter.get().convert(arguments, targetType);
			}
			catch (Exception e) {
				throw new ArgumentConversionException("Could not aggregate method arguments", e);
			}
		}
		else {
			throw new ArgumentConversionException("Parameter type must implement one of the following ["
					+ CONVERTERS.entrySet().stream().map(Entry::getKey).map(Class::getCanonicalName).collect(
						Collectors.joining(", "))
					+ "]");
		}
	}

	interface AggregatingConverterHelper {
		Object convert(Object[] args, Class<?> target);

		boolean canConvert(Class<?> target);
	}

	private static class CollectionConverter implements AggregatingConverterHelper {

		static final Map<Class<?>, Class<?>> FALLBACK;

		static {
			Map<Class<?>, Class<?>> fallback = new HashMap<>();
			fallback.put(List.class, ArrayList.class);
			fallback.put(Set.class, HashSet.class);
			fallback.put(Queue.class, LinkedList.class);
			fallback.put(Deque.class, ArrayDeque.class);
			FALLBACK = Collections.unmodifiableMap(fallback);
		}

		public boolean canConvert(Class<?> targetType) {
			return Collection.class.isAssignableFrom(targetType);
		}

		public Object convert(Object[] args, Class<?> target) {

			Class<?> resultingClass = target;

			if (target.isInterface()) {
				Optional<Class<?>> fallbackClass = FALLBACK.entrySet().stream().filter(
					e -> e.getKey().equals(target)).findFirst().map(Entry::getValue);

				resultingClass = fallbackClass.isPresent() ? fallbackClass.get() : ArrayList.class;
			}

			Collection<Object> resultingObject = instantiate(resultingClass);
			populate(resultingObject, args);

			return resultingObject;
		}

		@SuppressWarnings("unchecked")
		Collection<Object> instantiate(Class<?> targetType) {
			return ReflectionUtils.newInstance(targetType.asSubclass(Collection.class));
		}

		void populate(Collection<Object> res, Object[] args) {
			res.addAll(Arrays.asList(args));
		}
	}

	private static class MapConverter implements AggregatingConverterHelper {

		static final Map<Class<?>, Class<?>> FALLBACK;

		static {
			Map<Class<?>, Class<?>> fallback = new HashMap<>();
			fallback.put(Map.class, HashMap.class);
			FALLBACK = Collections.unmodifiableMap(fallback);
		}

		public boolean canConvert(Class<?> targetType) {
			return Map.class.isAssignableFrom(targetType);
		}

		public Object convert(Object[] args, Class<?> target) {

			Class<?> resultingClass = target;

			if (target.isInterface()) {
				Optional<Class<?>> fallbackClass = FALLBACK.entrySet().stream().filter(
					e -> e.getKey().equals(target)).findFirst().map(Entry::getValue);

				resultingClass = fallbackClass.isPresent() ? fallbackClass.get() : HashMap.class;
			}

			Map<String, Object> resultingObject = instantiate(resultingClass);
			populate(resultingObject, args);

			return resultingObject;
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> instantiate(Class<?> targetType) {
			return ReflectionUtils.newInstance(targetType.asSubclass(Map.class));
		}

		void populate(Map<String, Object> res, Object[] args) {
			IntStream.range(0, args.length).forEach(i -> res.put(String.valueOf(i), args[i]));
		}
	}

}
