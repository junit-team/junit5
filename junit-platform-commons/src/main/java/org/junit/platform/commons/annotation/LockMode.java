/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.annotation;

/**
 * LockMode translates to the respective {@link java.util.concurrent.locks.ReentrantReadWriteLock} locks.
 *
 * Enum order is important, since it can be used to sort locks, so the stronger mode has to be first.
 */
public enum LockMode {
	ReadWrite,
	Read
}
