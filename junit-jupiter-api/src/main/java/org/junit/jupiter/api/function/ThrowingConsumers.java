/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * {@code ThrowingConsumers} contains additional {@link ThrowingConsumer}
 * functional interfaces that take from 2 to up to 22 arguments.
 *
 * @since 5.7
 * @see ThrowingConsumer
 */
@API(status = EXPERIMENTAL, since = "5.7")
public interface ThrowingConsumers {
	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer2<P1, P2> {
		void accept(P1 p1, P2 p2) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer3<P1, P2, P3> {
		void accept(P1 p1, P2 p2, P3 p3) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer4<P1, P2, P3, P4> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer5<P1, P2, P3, P4, P5> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer6<P1, P2, P3, P4, P5, P6> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer7<P1, P2, P3, P4, P5, P6, P7> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer8<P1, P2, P3, P4, P5, P6, P7, P8> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer9<P1, P2, P3, P4, P5, P6, P7, P8, P9> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12)
				throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13)
				throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15, P16 p16) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15, P16 p16, P17 p17) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15, P16 p16, P17 p17, P18 p18) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19, P20 p20) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer21<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19, P20 p20, P21 p21) throws Throwable;
	}

	/**
	 * @since 5.7
	 * @see ThrowingConsumer
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	@FunctionalInterface
	interface ThrowingConsumer22<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22> {
		void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13,
				P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19, P20 p20, P21 p21, P22 p22) throws Throwable;
	}
}
