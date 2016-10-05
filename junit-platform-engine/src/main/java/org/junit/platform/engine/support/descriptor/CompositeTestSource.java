/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestSource;

/**
 * A {@code CompositeTestSource} contains one or more {@link TestSource TestSources}.
 *
 * <p>{@code CompositeTestSource} and its {@link #getSources sources} are immutable.
 *
 * @since 1.0
 */
@API(Experimental)
public class CompositeTestSource implements TestSource {

	private static final long serialVersionUID = 1L;

	private final List<TestSource> sources;

	/**
	 * Create a new {@code CompositeTestSource} based on the supplied
	 * collection of {@link TestSource sources}.
	 *
	 * <p>This constructor makes a defensive copy of the supplied collection
	 * and stores the sources as a list in the order in which they are
	 * returned by the collection's iterator.
	 *
	 * @param sources the collection of sources to store in this
	 * {@code CompositeTestSource}; never {@code null} or empty
	 */
	public CompositeTestSource(Collection<? extends TestSource> sources) {
		Preconditions.notEmpty(sources, "TestSource collection must not be null or empty");
		Preconditions.containsNoNullElements(sources, "individual TestSources must not be null");
		this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
	}

	/**
	 * Get an immutable list of the {@linkplain TestSource sources} stored in this
	 * {@code CompositeTestSource}.
	 *
	 * @return the sources stored in this {@code CompositeTestSource}; never {@code null}
	 */
	public final List<TestSource> getSources() {
		return this.sources;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CompositeTestSource that = (CompositeTestSource) obj;
		return this.sources.equals(that.sources);
	}

	@Override
	public int hashCode() {
		return this.sources.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("sources", this.sources).toString();
	}

}
