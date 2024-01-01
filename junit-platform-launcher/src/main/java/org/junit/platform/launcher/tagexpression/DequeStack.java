/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @since 1.1
 */
class DequeStack<T> implements Stack<T> {

	private final Deque<T> deque = new ArrayDeque<>();

	@Override
	public void push(T t) {
		deque.addFirst(t);
	}

	@Override
	public T peek() {
		return deque.peek();
	}

	@Override
	public T pop() {
		return deque.pollFirst();
	}

	@Override
	public boolean isEmpty() {
		return deque.isEmpty();
	}

	@Override
	public int size() {
		return deque.size();
	}

}
