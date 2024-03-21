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
import static java.util.stream.Collectors.toCollection;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.DiscoverySelectorIdentifier;

/**
 * A {@link DiscoverySelector} that selects the iterations of a parent
 * {@code DiscoverySelector} via their indices so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * a subset of the iterations of tests or containers.
 *
 * @see DiscoverySelectors#selectIteration(DiscoverySelector, int...)
 * @since 1.9
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
			CodingUtil.urlEncode(parentSelectorString.toString()), //
			iterationIndices.stream().map(String::valueOf).collect(Collectors.joining(","))) //
		);
	}

	public static class IdentifierParser implements DiscoverySelectorIdentifierParser {

		private static final String PREFIX = "iteration";

		public IdentifierParser() {
		}

		@Override
		public String getPrefix() {
			return PREFIX;
		}

		@Override
		public Stream<IterationSelector> parse(DiscoverySelectorIdentifier identifier, Context context) {
			int[] iterationIndices = Arrays.stream(identifier.getFragment().split(",")).mapToInt(
				Integer::parseInt).toArray();
			String parentSelector = CodingUtil.urlDecode(identifier.getValue());
			return context.parse(parentSelector).map(
				parent -> DiscoverySelectors.selectIteration(parent, iterationIndices));
		}
	}
}
