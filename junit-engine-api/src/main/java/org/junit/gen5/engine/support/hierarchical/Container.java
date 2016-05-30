/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.hierarchical;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;

/**
 * A <em>container</em> within the execution hierarchy.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by the {@code HierarchicalTestEngine}
 * @since 5.0
 * @see HierarchicalTestEngine
 * @see Node
 * @see Leaf
 */
@API(Experimental)
public interface Container<C extends EngineExecutionContext> extends Node<C> {

	/**
	 * Execute the <em>before-all</em> behavior of this container.
	 *
	 * <p>This method will be called once <em>before</em> executing the children
	 * of this container.
	 *
	 * @param context the context to execute in
	 * @return the new context to be used for children of this container
	 */
	default C beforeAll(C context) throws Exception {
		return context;
	}

	/**
	 * Execute the <em>after-all</em> behavior of this container.
	 *
	 * <p>This method will be called once <em>after</em> executing the children
	 * of this container.
	 *
	 * @param context the context to execute in
	 * @return the new context to be used for children of this container
	 */
	default C afterAll(C context) throws Exception {
		return context;
	}

}
