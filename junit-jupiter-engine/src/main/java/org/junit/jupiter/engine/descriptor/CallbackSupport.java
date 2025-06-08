/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

import java.util.Objects;
import java.util.List;
package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.util.CollectionUtils.forEachInReverseOrder;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * @since 5.13
 */
class CallbackSupport {

	private static <T extends Extension> void invokeCallbacks(List<T> extensions, 
			ExtensionContext extensionContext, ThrowableCollector collector, CallbackInvoker<T> invoker, boolean reverse, boolean breakOnPossibleException){

		if(reverse){
			forEachInReverseOrder(extensions, ext -> collector.execute(() -> invoker.invoke(ext, extensionContext)));
		}else{
			for(T ext: extensions){
				collector.execute(()-> invoker.invoke(ext, extensionContext));
				if (breakOnPossibleException && collector.isNotEmpty()) break;
			}
		}
	}

	static <T extends Extension> void invokeBeforeCallbacks(Class<T> type, JupiterEngineExecutionContext context,
			CallbackInvoker<T> callbackInvoker) {
		
		Objects.requireNonNull(type, "type must not be null");
		Objects.requireNonNull(context, "context must not be null");
		Objects.requireNonNull(callbackInvoker, "callbackInvoker must not be null");
		
		invokeCallbacks(
			context.getExtensionRegistry().getExtensions(type),
			context.getExtensionContext(),
			 context.getThrowableCollector(),
			 false, // forward order on callbacks
			 true //break out on any first exception encountered 
		)
	}

	static <T extends Extension> void invokeAfterCallbacks(Class<T> type, JupiterEngineExecutionContext context,
			CallbackInvoker<T> callbackInvoker) {
		
		Objects.requireNonNull(type, "type must not be null");
		Objects.requireNonNull(context, "context must not be null");
		Objects.requireNonNull(callbackInvoker, "callbackInvoker must not be null");

		invokeCallbacks(
			context.getExtensionRegistry().getExtensions(type),
			context.getExtensionContext(),
			 context.getThrowableCollector(),
			 true, // reverse order on callbacks 
			 false // allow all the callbacks to run.
		)
		
	}

	@FunctionalInterface
	protected interface CallbackInvoker<T extends Extension> {

		void invoke(T t, ExtensionContext context) throws Throwable;

	}

}
