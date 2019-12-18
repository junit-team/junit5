/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesSeparator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DisplayNameIndicativeSentencesDemo {

	@Nested
	@DisplayNameGeneration(DisplayNameGenerator.IndicativeSentencesGenerator.class)
	@IndicativeSentencesSeparator(" >> ")
	class A_cat_named_schrodinger {

		@Test
		void is_in_a_box() {
		}

		@Test
		void can_be_alive_and_not() {
		}

		@Nested
		class when_we_look_the_box {

			@Test
			void our_mind_will_blow() {
			}
		}

	}

}
// end::user_guide[]
