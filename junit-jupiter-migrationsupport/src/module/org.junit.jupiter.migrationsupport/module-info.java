/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

module org.junit.jupiter.migrationsupport {
	requires transitive org.apiguardian.api;
	requires transitive org.junit.jupiter.api;
	requires transitive junit; // 4

	exports org.junit.jupiter.migrationsupport;
	exports org.junit.jupiter.migrationsupport.conditions;
	exports org.junit.jupiter.migrationsupport.rules;
	exports org.junit.jupiter.migrationsupport.rules.adapter;
	exports org.junit.jupiter.migrationsupport.rules.member;
}
