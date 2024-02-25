/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncherConfigBuilderWithDisabledServiceLoading;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.fakes.TestEngineStub;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.mockito.ArgumentCaptor;

class LauncherSessionTests {

	LauncherSessionListener firstSessionListener = mock(LauncherSessionListener.class, "firstSessionListener");
	LauncherSessionListener secondSessionListener = mock(LauncherSessionListener.class, "secondSessionListener");
	LauncherConfig launcherConfig = createLauncherConfigBuilderWithDisabledServiceLoading() //
			.addLauncherSessionListeners(firstSessionListener, secondSessionListener) //
			.addTestEngines(new TestEngineStub()) //
			.build();
	LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().build();

	@Test
	void callsRegisteredListenersWhenLauncherIsUsedDirectly() {
		var launcher = LauncherFactory.create(launcherConfig);

		var testPlan = launcher.discover(request);

		var inOrder = inOrder(firstSessionListener, secondSessionListener);
		var launcherSession = ArgumentCaptor.forClass(LauncherSession.class);
		inOrder.verify(firstSessionListener).launcherSessionOpened(launcherSession.capture());
		inOrder.verify(secondSessionListener).launcherSessionOpened(launcherSession.getValue());
		inOrder.verify(secondSessionListener).launcherSessionClosed(launcherSession.getValue());
		inOrder.verify(firstSessionListener).launcherSessionClosed(launcherSession.getValue());

		launcher.execute(testPlan);

		inOrder.verify(firstSessionListener).launcherSessionOpened(launcherSession.capture());
		inOrder.verify(secondSessionListener).launcherSessionOpened(launcherSession.getValue());
		inOrder.verify(secondSessionListener).launcherSessionClosed(launcherSession.getValue());
		inOrder.verify(firstSessionListener).launcherSessionClosed(launcherSession.getValue());

		launcher.execute(request);

		inOrder.verify(firstSessionListener).launcherSessionOpened(launcherSession.capture());
		inOrder.verify(secondSessionListener).launcherSessionOpened(launcherSession.getValue());
		inOrder.verify(secondSessionListener).launcherSessionClosed(launcherSession.getValue());
		inOrder.verify(firstSessionListener).launcherSessionClosed(launcherSession.getValue());
	}

	@Test
	void callsRegisteredListenersWhenLauncherIsUsedViaSession() {
		@SuppressWarnings("resource")
		var session = LauncherFactory.openSession(launcherConfig);
		var launcher = session.getLauncher();

		var inOrder = inOrder(firstSessionListener, secondSessionListener);
		inOrder.verify(firstSessionListener).launcherSessionOpened(session);
		inOrder.verify(secondSessionListener).launcherSessionOpened(session);
		verifyNoMoreInteractions(firstSessionListener, secondSessionListener);

		var testPlan = launcher.discover(request);

		verifyNoMoreInteractions(firstSessionListener, secondSessionListener);

		launcher.execute(testPlan);

		verifyNoMoreInteractions(firstSessionListener, secondSessionListener);

		launcher.execute(request);

		verifyNoMoreInteractions(firstSessionListener, secondSessionListener);

		session.close();

		inOrder.verify(secondSessionListener).launcherSessionClosed(session);
		inOrder.verify(firstSessionListener).launcherSessionClosed(session);
		verifyNoMoreInteractions(firstSessionListener, secondSessionListener);
	}

	@Test
	void closedSessionCannotBeUsed() {
		@SuppressWarnings("resource")
		var session = LauncherFactory.openSession(launcherConfig);
		var launcher = session.getLauncher();
		var testPlan = launcher.discover(request);

		session.close();

		assertThrows(PreconditionViolationException.class, () -> launcher.discover(request));
		assertThrows(PreconditionViolationException.class, () -> launcher.execute(testPlan));
		assertThrows(PreconditionViolationException.class, () -> launcher.execute(request));
	}

}
