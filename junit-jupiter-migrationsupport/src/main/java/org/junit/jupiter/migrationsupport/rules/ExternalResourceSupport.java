/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.adapter.ExternalResourceAdapter;
import org.junit.jupiter.migrationsupport.rules.member.TestRuleAnnotatedMember;
import org.junit.rules.ExternalResource;

/**
 * This {@code Extension} provides native support for subclasses of
 * the {@link ExternalResource} rule from JUnit 4.
 *
 * <p>{@code @Rule}-annotated fields as well as methods are supported.
 *
 * <p>By using this class-level extension on a test class such
 * {@code ExternalResource} implementations in legacy code bases
 * can be left unchanged including the JUnit 4 rule import statements.
 *
 * <p>However, if you intend to develop a <em>new</em> extension for
 * JUnit 5 please use the new extension model of JUnit Jupiter instead
 * of the rule-based model of JUnit 4.
 *
 * @since 5.0
 * @see org.junit.rules.ExternalResource
 * @see org.junit.rules.TestRule
 * @see org.junit.Rule
 */
@API(status = EXPERIMENTAL, since = "5.0")
public class ExternalResourceSupport implements BeforeEachCallback, AfterEachCallback {

	private final Function<TestRuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator = ExternalResourceAdapter::new;

	private final TestRuleFieldSupport fieldSupport = new TestRuleFieldSupport(this.adapterGenerator,
		ExternalResource.class);
	private final TestRuleMethodSupport methodSupport = new TestRuleMethodSupport(this.adapterGenerator,
		ExternalResource.class);

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		this.fieldSupport.beforeEach(context);
		this.methodSupport.beforeEach(context);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		this.methodSupport.afterEach(context);
		this.fieldSupport.afterEach(context);
	}

}
