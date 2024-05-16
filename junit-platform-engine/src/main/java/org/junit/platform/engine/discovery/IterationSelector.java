/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.DiscoverySelectorIdentifier;

/**
 * A {@link DiscoverySelector} that selects the iterations of a parent
 * {@code DiscoverySelector} via their indices so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * a subset of the iterations of tests or containers.
 *
 * @since 1.9
 * @see DiscoverySelectors#selectIteration(DiscoverySelector, int...)
 */
@API(status = EXPERIMENTAL, since = "1.9")
public class IterationSelector implements DiscoverySelector {

	private final DiscoverySelector parentSelector;
	private final SortedSet<Integer> iterationIndices;

	IterationSelector(DiscoverySelector parentSelector, int... iterationIndices) {
		this.parentSelector = parentSelector;
		this.iterationIndices = toSortedSet(iterationIndices);
	}

	private SortedSet<Integer> toSortedSet(int[] iterationIndices) {
		return Arrays.stream(iterationIndices) //
				.boxed() //
				.collect(collectingAndThen(toCollection(TreeSet::new), Collections::unmodifiableSortedSet));
	}

	/**
	 * Get the selected parent {@link DiscoverySelector}.
	 */
	public DiscoverySelector getParentSelector() {
		return parentSelector;
	}

	/**
	 * Get the selected iteration indices.
	 */
	public SortedSet<Integer> getIterationIndices() {
		return iterationIndices;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IterationSelector that = (IterationSelector) o;
		return parentSelector.equals(that.parentSelector) && iterationIndices.equals(that.iterationIndices);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parentSelector, iterationIndices);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("parentSelector", this.parentSelector)
				.append("iterationIndices", this.iterationIndices)
				.toString();
		// @formatter:on
	}

	@Override
	public Optional<DiscoverySelectorIdentifier> toIdentifier() {
		return parentSelector.toIdentifier().map(parentSelectorString -> DiscoverySelectorIdentifier.create( //
			IdentifierParser.PREFIX, //
			String.format("%s[%s]", parentSelectorString, formatIterationIndicesAsRanges())) //
		);
	}

	private String formatIterationIndicesAsRanges() {
		class Range {
			final int start;
			int end;

			Range(int start) {
				this.start = start;
				this.end = start;
			}
		}
		List<Range> ranges = new ArrayList<>();
		Range current = new Range(iterationIndices.first());
		ranges.add(current);
		for (int n : iterationIndices.tailSet(current.start + 1)) {
			if (n == current.end + 1) {
				current.end = n;
			}
			else {
				current = new Range(n);
				ranges.add(current);
			}
		}
		return ranges.stream() //
				.map(r -> {
					if (r.start == r.end) {
						return String.valueOf(r.start);
					}
					if (r.start == r.end - 1) {
						return r.start + "," + r.end;
					}
					return r.start + ".." + r.end;
				}) //
				.collect(joining(","));
	}

	/**
	 * The {@link DiscoverySelectorIdentifierParser} for
	 * {@link IterationSelector IterationSelectors}.
	 */
	@API(status = INTERNAL, since = "1.11")
	public static class IdentifierParser implements DiscoverySelectorIdentifierParser {

		public static final String PREFIX = "iteration";

		private static final Pattern PATTERN = Pattern.compile(
			"(?<parentIdentifier>.+)\\[(?<indices>(\\d+)(\\.\\.\\d+)?(\\s*,\\s*(\\d+)(\\.\\.\\d+)?)*)]");

		public IdentifierParser() {
		}

		@Override
		public String getPrefix() {
			return PREFIX;
		}

		@Override
		public Optional<IterationSelector> parse(DiscoverySelectorIdentifier identifier, Context context) {
			Matcher matcher = PATTERN.matcher(identifier.getValue());
			Preconditions.condition(matcher.matches(), "Invalid format: must be IDENTIFIER[INDEX(,INDEX)*]");
			return context.parse(matcher.group("parentIdentifier")) //
					.map(parent -> {
						int[] iterationIndices = Arrays.stream(matcher.group("indices").split(",")) //
								.flatMapToInt(this::parseIndexDefinition) //
								.toArray();
						return DiscoverySelectors.selectIteration(parent, iterationIndices);
					});
		}

		private IntStream parseIndexDefinition(String value) {
			return StringUtils.splitIntoTwo("..", value).map( //
				index -> IntStream.of(Integer.parseInt(index)), //
				(firstIndex, lastIndex) -> IntStream.rangeClosed(Integer.parseInt(firstIndex),
					Integer.parseInt(lastIndex))//
			);
		}
	}
}
