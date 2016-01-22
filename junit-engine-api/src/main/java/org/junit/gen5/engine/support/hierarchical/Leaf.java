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

/**
 * A <em>leaf</em> within the execution hierarchy.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by the {@code HierarchicalTestEngine}
 * @since 5.0
 * @see HierarchicalTestEngine
 * @see Node
 * @see Container
 */
public interface Leaf<C extends EngineExecutionContext> extends Node<C> {

	C execute(C context) throws Exception;

}
