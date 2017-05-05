/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * A {@code DynamicContainer} is a container generated at runtime.
 *
 * @since 5.0
 */
@API(Experimental)
public class DynamicContainer extends DynamicNode {

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <D extends DynamicNode> DynamicContainer dynamicContainer(String displayName, D... dynamicNodes) {
		return new DynamicContainer(displayName, Arrays.stream(dynamicNodes));
	}

	public static DynamicContainer dynamicContainer(String displayName, Iterable<? extends DynamicNode> dynamicNodes) {
		return new DynamicContainer(displayName, StreamSupport.stream(dynamicNodes.spliterator(), false));
	}

	public static DynamicContainer dynamicContainer(String displayName, Stream<? extends DynamicNode> dynamicNodes) {
		return new DynamicContainer(displayName, dynamicNodes);
	}

	private final List<DynamicNode> dynamicNodes;

	private DynamicContainer(String displayName, Stream<? extends DynamicNode> dynamicNodes) {
		super(displayName, false);
		Preconditions.notNull(dynamicNodes, "dynamicNodes must not be null");
		this.dynamicNodes = dynamicNodes.collect(CollectionUtils.toUnmodifiableList());
		Preconditions.containsNoNullElements(this.dynamicNodes, "individual dynamic node must not be null");
	}

	public Iterable<DynamicNode> getDynamicNodes() {
		return dynamicNodes;
	}
}
