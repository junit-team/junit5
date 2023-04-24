/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.api.tools;

import org.apiguardian.api.API.Status;

import java.util.List;
import java.util.Map;

/**
 * @since 1.0
 */
record ApiReport(List<Class<?>> types, Map<Status, List<Declaration>> declarationsMap) {}
