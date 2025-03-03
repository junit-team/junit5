/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterContainerTemplateInvocationCallback;
import org.junit.jupiter.api.extension.BeforeContainerTemplateInvocationCallback;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedClassContext.InjectionType;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

class ParameterizedClassInvocationContext extends ParameterizedInvocationContext<ParameterizedClassContext>
		implements ContainerTemplateInvocationContext {

	private final ResolutionCache resolutionCache = ResolutionCache.enabled();

	ParameterizedClassInvocationContext(ParameterizedClassContext classContext,
			ParameterizedInvocationNameFormatter formatter, Arguments arguments, int invocationIndex) {
		super(classContext, formatter, arguments, invocationIndex);
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return super.getDisplayName(invocationIndex);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return Stream.concat(Stream.of(createParameterInjector()), createLifecycleMethodInvokers()) //
				.collect(toList());
	}

	@Override
	public void prepareInvocation(ExtensionContext context) {
		super.prepareInvocation(context);
	}

	private Extension createParameterInjector() {
		InjectionType injectionType = this.declarationContext.getInjectionType();
		switch (injectionType) {
			case CONSTRUCTOR:
				return createExtensionForConstructorInjection();
			case FIELDS:
				return createExtensionForFieldInjection();
		}
		throw new JUnitException("Unsupported injection type: " + injectionType);
	}

	private ContainerTemplateConstructorParameterResolver createExtensionForConstructorInjection() {
		Preconditions.condition(this.declarationContext.getTestInstanceLifecycle() == PER_METHOD,
			"Constructor injection is only supported for lifecycle PER_METHOD");
		return new ContainerTemplateConstructorParameterResolver(this.declarationContext, this.arguments,
			this.invocationIndex, this.resolutionCache);
	}

	private Extension createExtensionForFieldInjection() {
		ResolverFacade resolverFacade = this.declarationContext.getResolverFacade();
		TestInstance.Lifecycle lifecycle = this.declarationContext.getTestInstanceLifecycle();
		switch (lifecycle) {
			case PER_CLASS:
				return new BeforeContainerTemplateInvocationFieldInjector(resolverFacade, this.arguments,
					this.invocationIndex, this.resolutionCache);
			case PER_METHOD:
				return new InstancePostProcessingContainerTemplateFieldInjector(resolverFacade, this.arguments,
					this.invocationIndex, this.resolutionCache);
		}
		throw new JUnitException("Unsupported lifecycle: " + lifecycle);
	}

	private Stream<Extension> createLifecycleMethodInvokers() {
		return Stream.concat(createBeforeArgumentSetMethodInvoker(), createAfterArgumentSetMethodInvoker());
	}

	private Stream<BeforeContainerTemplateInvocationCallback> createBeforeArgumentSetMethodInvoker() {
		return this.declarationContext.getBeforeMethods().stream() //
				.map(method -> new BeforeArgumentSetMethodInvoker(this.declarationContext, this.arguments,
					this.invocationIndex, this.resolutionCache, method));
	}

	private Stream<AfterContainerTemplateInvocationCallback> createAfterArgumentSetMethodInvoker() {
		return this.declarationContext.getAfterMethods().stream() //
				.map(method -> new AfterArgumentSetMethodInvoker(this.declarationContext, this.arguments,
					this.invocationIndex, this.resolutionCache, method));
	}

}
