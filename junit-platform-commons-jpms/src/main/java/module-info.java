/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
module org.junit.platform.commons.jpms {
  requires org.apiguardian.api;
  requires org.junit.platform.commons;

  provides org.junit.platform.commons.util.ModuleClassFinder with
          org.junit.platform.commons.jpms.DefaultModuleClassFinder;
}
