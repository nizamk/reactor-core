/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.core.publisher;

import java.util.NoSuchElementException;

import org.junit.Test;
import reactor.test.StepVerifier;
import reactor.test.subscriber.AssertSubscriber;

public class MonoSingleTest {
	@Test(expected = NullPointerException.class)
	public void source1Null() {
		new MonoSingle<>(null, 1, false);
	}

	@Test(expected = NullPointerException.class)
	public void defaultSupplierNull() {
		new MonoSingle<>(Mono.never(), null, false);
	}


	@Test
	public void normal() {

		AssertSubscriber<Integer> ts = AssertSubscriber.create();

		Flux.just(1).single().subscribe(ts);

		ts.assertValues(1)
		  .assertNoError()
		  .assertComplete();
	}

	@Test
	public void normalBackpressured() {
		AssertSubscriber<Integer> ts = AssertSubscriber.create(0);

		Flux.just(1).single().subscribe(ts);

		ts.assertNoValues()
		  .assertNoError()
		  .assertNotComplete();

		ts.request(1);

		ts.assertValues(1)
		  .assertNoError()
		  .assertComplete();
	}

	@Test
	public void empty() {

		AssertSubscriber<Integer> ts = AssertSubscriber.create();

		Flux.<Integer>empty().single().subscribe(ts);

		ts.assertNoValues()
		  .assertError(NoSuchElementException.class)
		  .assertNotComplete();
	}

	@Test
	public void emptyDefault() {
		AssertSubscriber<Integer> ts = AssertSubscriber.create();

		Flux.<Integer>empty().single(1).subscribe(ts);

		ts.assertValues(1)
		  .assertNoError()
		  .assertComplete();
	}

	@Test
	public void emptyDefaultBackpressured() {
		AssertSubscriber<Integer> ts = AssertSubscriber.create(0);

		Flux.<Integer>empty().single(1).subscribe(ts);

		ts.assertNoValues()
		  .assertNoError()
		  .assertNotComplete();

		ts.request(1);

		ts.assertValues(1)
		  .assertNoError()
		  .assertComplete();
	}

	@Test
	public void multi() {

		AssertSubscriber<Integer> ts = AssertSubscriber.create();

		Flux.range(1, 10).single().subscribe(ts);

		ts.assertNoValues()
		  .assertError(IndexOutOfBoundsException.class)
		  .assertNotComplete();
	}

	@Test
	public void multiBackpressured() {

		AssertSubscriber<Integer> ts = AssertSubscriber.create(0);

		Flux.range(1, 10).single().subscribe(ts);

		ts.assertNoValues()
		  .assertNoError()
		  .assertNotComplete();

		ts.request(1);

		ts.assertNoValues()
		  .assertError(IndexOutOfBoundsException.class)
		  .assertNotComplete();
	}

	@Test
	public void singleCallable() {
		StepVerifier.create(Mono.fromCallable(() -> 1)
		                        .flux()
		                        .single())
		            .expectNext(1)
		            .verifyComplete();
	}

	@Test
	public void singleFallbackEmpty() {
		StepVerifier.create(Flux.empty()
		                        .single(1))
		            .expectNext(1)
		            .verifyComplete();
	}

	@Test
	public void singleFallbackJust() {
		StepVerifier.create(Flux.just(1)
		                        .single(2))
		            .expectNext(1)
		            .verifyComplete();
	}

	@Test
	public void singleFallbackCallable() {
		StepVerifier.create(Mono.fromCallable(() -> 1)
		                        .flux()
		                        .single(2))
		            .expectNext(1)
		            .verifyComplete();
	}

	@Test
	public void singleJustHide() {
		StepVerifier.create(Flux.empty()
		                        .single())
		            .verifyError(NoSuchElementException.class);
	}

	@Test
	public void singleFallbackJustHide() {
		StepVerifier.create(Flux.just(1)
		                        .hide()
		                        .single(2))
		            .expectNext(1)
		            .verifyComplete();
	}

	@Test
	public void singleEmptyFallbackCallable() {
		StepVerifier.create(Mono.fromCallable(() -> 1)
		                        .flux()
		                        .singleOrEmpty())
		            .expectNext(1)
		            .verifyComplete();
	}


	@Test
	public void singleEmptyFallbackJustHide() {
		StepVerifier.create(Flux.empty()
		                        .hide()
		                        .singleOrEmpty())
		            .verifyComplete();
	}

	@Test
	public void singleEmptyFallbackJustHideError() {
		StepVerifier.create(Flux.just(1, 2, 3)
		                        .hide()
		                        .singleOrEmpty())
		            .verifyError(IndexOutOfBoundsException.class);
	}

}
