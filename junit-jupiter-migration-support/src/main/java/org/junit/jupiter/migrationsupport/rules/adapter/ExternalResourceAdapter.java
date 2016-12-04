/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules.adapter;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import org.junit.jupiter.migrationsupport.rules.member.TestRuleAnnotatedMember;
import org.junit.platform.commons.meta.API;
import org.junit.rules.ExternalResource;

/**
 * @since 5.0
 */
@API(Internal)
public class ExternalResourceAdapter extends AbstractTestRuleAdapter {

	public ExternalResourceAdapter(TestRuleAnnotatedMember annotatedMember) {
		super(annotatedMember, ExternalResource.class);
	}

	@Override
	public void before() {
		executeMethod("before");
	}

	@Override
	public void after() {
		executeMethod("after");
	}

}
