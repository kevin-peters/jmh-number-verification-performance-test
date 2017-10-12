/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.example;

import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Fork(1)
@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
@BenchmarkMode(Mode.AverageTime)
public class NumberVerificationComparisonBenchmark {

	@Param({"X", StringUtils.EMPTY})
	public String prefix;

	@State(Scope.Thread)
	public static class IterationEnvironment {

		private final AtomicInteger positive = new AtomicInteger(0);
		private final AtomicInteger negative = new AtomicInteger(0);

		IntStream getIntStream() {
			return IntStream.range(0, 10_000_000);
		}

		AtomicInteger getPositive() {
			return positive;
		}

		AtomicInteger getNegative() {
			return negative;
		}

	}

	@Benchmark
	public void parseIntWithTryCatch(final IterationEnvironment iterationEnvironment, final Blackhole blackhole) {

		iterationEnvironment.getIntStream().forEach(value -> parseAndCatch(iterationEnvironment.getPositive(), iterationEnvironment.getNegative(), prefix, value));

		blackhole.consume(iterationEnvironment.getPositive().get());
		blackhole.consume(iterationEnvironment.getNegative().get());
	}

	@Benchmark
	public void isNumberWithRegex(final IterationEnvironment iterationEnvironment, final Blackhole blackhole) {

		iterationEnvironment.getIntStream().forEach(value -> matchWithRegex(iterationEnvironment.getPositive(), iterationEnvironment.getNegative(), prefix, value));

		blackhole.consume(iterationEnvironment.getPositive().get());
		blackhole.consume(iterationEnvironment.getNegative().get());
	}

	@Benchmark
	public void isNumericWithStringUtils(final IterationEnvironment iterationEnvironment, final Blackhole blackhole) {

		iterationEnvironment.getIntStream().forEach(value -> checkWithStringUtils(iterationEnvironment.getPositive(), iterationEnvironment.getNegative(), prefix, value));

		blackhole.consume(iterationEnvironment.getPositive().get());
		blackhole.consume(iterationEnvironment.getNegative().get());
	}

	private void parseAndCatch(final AtomicInteger positive, final AtomicInteger negative, final String prefix, final int value) {

		final String s = prefix + value;
		try {
			positive.getAndAdd(Integer.parseInt(s));
		} catch (NumberFormatException e) {
			negative.getAndAdd(value);
		}
	}

	private void matchWithRegex(final AtomicInteger positive, final AtomicInteger negative, final String prefix, final int value) {

		final String s = prefix + value;
		if (s.matches("\\d+")) {
			positive.getAndAdd(value);
		} else {
			negative.getAndAdd(value);
		}
	}

	private void checkWithStringUtils(final AtomicInteger positive, final AtomicInteger negative, final String prefix, final int value) {

		final String s = prefix + value;
		if (StringUtils.isNumeric(s)) {
			positive.getAndAdd(value);
		} else {
			negative.getAndAdd(value);
		}
	}

}
