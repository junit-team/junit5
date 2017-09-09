/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.filter;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.FilterResult;

/**
 * Decorator for a {@link Filter} that passes the <em>object</em> and the
 * <em>reason</em> to a {@link BiConsumer} in case it is <em>excluded</em>.
 *
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public class ExclusionReasonConsumingFilter<T> implements Filter<T> {

	private final Filter<T> filter;
	private final BiConsumer<T, Optional<String>> reasonConsumer;

	/**
	 * Create a new {@code ExclusionReasonConsumingFilter} using the supplied
	 * {@code filter} and {@code reasonConsumer}.
	 *
	 * @param filter the filter to decorate; must not be {@code null}
	 * @param reasonConsumer the consumer to call in case of exclusions; must not be {@code null}
	 */
	public ExclusionReasonConsumingFilter(Filter<T> filter, BiConsumer<T, Optional<String>> reasonConsumer) {
		this.filter = Preconditions.notNull(filter, "filter must not be null");
		this.reasonConsumer = Preconditions.notNull(reasonConsumer, "reasonConsumer must not be null");
	}

	@Override
	public FilterResult apply(T object) {
		FilterResult result = this.filter.apply(object);
		if (result.excluded()) {
			this.reasonConsumer.accept(object, result.getReason());
		}
		return result;
	}

}
