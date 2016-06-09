/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.engine.FilterResult.includedIf;

import java.util.Arrays;
import java.util.List;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.Filter;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestEngine;

/**
 * An {@code EngineFilter} is applied to all {@link TestEngine TestEngines}
 * before they are used.
 *
 * <p><strong>Warning</strong>: be cautious when registering multiple competing
 * {@link #requireEngines require} {@code EngineFilters} or multiple competing
 * {@link #excludeEngines exclude} {@code EngineFilters} for the same discovery
 * request since doing so will likely lead to undesirable results (i.e., zero
 * engines being active).
 *
 * @since 5.0
 * @see #requireEngines(String...)
 * @see #excludeEngines(String...)
 * @see TestDiscoveryRequest
 */
@API(Experimental)
public class EngineFilter implements Filter<TestEngine> {

	/**
	 * Create a new <em>require</em> {@code EngineFilter} based on the
	 * supplied engine IDs.
	 *
	 * <p>Only {@code TestEngines} with matching engine IDs will be
	 * <em>included</em> within the test discovery and execution.
	 *
	 * @param engineIds the list of engine IDs to match against; never {@code null}
	 * or empty; individual IDs must also not be null or blank
	 */
	public static EngineFilter requireEngines(String... engineIds) {
		return new EngineFilter(validateAndConvertToList(engineIds), Type.REQUIRE);
	}

	/**
	 * Create a new <em>exclude</em> {@code EngineFilter} based on the
	 * supplied engine IDs.
	 *
	 * <p>{@code TestEngines} with matching engine IDs will be
	 * <em>excluded</em> from test discovery and execution.
	 *
	 * @param engineIds the list of engine IDs to match against; never {@code null}
	 * or empty; individual IDs must also not be null or blank
	 */
	public static EngineFilter excludeEngines(String... engineIds) {
		return new EngineFilter(validateAndConvertToList(engineIds), Type.EXCLUDE);
	}

	private final List<String> engineIds;
	private final Type type;

	private EngineFilter(List<String> engineIds, Type type) {
		this.engineIds = trimmedEngineIds(engineIds);
		this.type = type;
	}

	@Override
	public FilterResult apply(TestEngine testEngine) {
		Preconditions.notNull(testEngine, "TestEngine must not be null");
		String engineId = testEngine.getId();
		Preconditions.notBlank(engineId, "TestEngine ID must not be null or blank");

		if (this.type == Type.REQUIRE) {
			return includedIf(this.engineIds.stream().anyMatch(engineId::equals), //
				() -> String.format("Engine ID [%s] is in required list [%s]", engineId, this.engineIds), //
				() -> String.format("Engine ID [%s] is not in required list [%s]", engineId, this.engineIds));
		}
		else {
			return includedIf(this.engineIds.stream().noneMatch(engineId::equals), //
				() -> String.format("Engine ID [%s] is not in excluded list [%s]", engineId, this.engineIds), //
				() -> String.format("Engine ID [%s] is in excluded list [%s]", engineId, this.engineIds));
		}
	}

	@Override
	public String toString() {
		return String.format("%s that %s engines with IDs %s", getClass().getSimpleName(), this.type.verb,
			this.engineIds);
	}

	private static List<String> validateAndConvertToList(String... engineIds) {
		Preconditions.notNull(engineIds, "engine ID array must not be null");
		List<String> list = Arrays.asList(engineIds);
		Preconditions.notEmpty(list, "engine ID array must not be empty");
		list.forEach(id -> Preconditions.notBlank(id, "engine ID must not be null or blank"));

		return list;
	}

	private static List<String> trimmedEngineIds(List<String> engineIds) {
		return engineIds.stream().distinct().map(String::trim).collect(toList());
	}

	private enum Type {

		REQUIRE("requires"),

		EXCLUDE("excludes");

		private final String verb;

		private Type(String verb) {
			this.verb = verb;
		}

	}

}
