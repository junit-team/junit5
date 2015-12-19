/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util.org.junit.gen5.meta;

import static de.schauderhaft.degraph.check.JCheck.*;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class DependencyTests {

    @Test
    public void noCycles(){
        Assert.assertThat(classpath()
                .noJars()
                .printTo("dependencies.graphml"),
                is(violationFree()));
    }
}
