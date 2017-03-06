/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.testinterface;

import example.timing.TimingExtension;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

//tag::user_guide[]
@Tag("timed")
@ExtendWith(TimingExtension.class)
public interface TimeExecutionLogger {
}
//end::user_guide[]
