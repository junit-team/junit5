/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules.member;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.rules.TestRule;

/**
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.1")
public interface TestRuleAnnotatedMember {

	TestRule getTestRule();

}
