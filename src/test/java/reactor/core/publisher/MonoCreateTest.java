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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MonoCreateTest {

	@Test
	public void createStreamFromMonoCreate() {
		AtomicInteger onDispose = new AtomicInteger();
		AtomicInteger onCancel = new AtomicInteger();
		StepVerifier.create(Mono.create(s -> {
							s.onDispose(onDispose::getAndIncrement)
							 .onCancel(onCancel::getAndIncrement)
							 .success("test1");
						}))
		            .expectNext("test1")
		            .verifyComplete();
		assertThat(onDispose.get()).isEqualTo(1);
		assertThat(onCancel.get()).isEqualTo(0);
	}

	@Test
	public void createStreamFromMonoCreateHide() {
		StepVerifier.create(Mono.create(s -> s.success("test1")).hide())
		            .expectNext("test1")
		            .verifyComplete();
	}

	@Test
	public void createStreamFromMonoCreateError() {
		AtomicInteger onDispose = new AtomicInteger();
		AtomicInteger onCancel = new AtomicInteger();
		StepVerifier.create(Mono.create(s -> {
							s.onDispose(onDispose::getAndIncrement)
							 .onCancel(onCancel::getAndIncrement)
							 .error(new Exception("test"));
						}))
		            .verifyErrorMessage("test");
		assertThat(onDispose.get()).isEqualTo(1);
		assertThat(onCancel.get()).isEqualTo(0);
	}

	@Test
	public void cancellation() {
		AtomicInteger onDispose = new AtomicInteger();
		AtomicInteger onCancel = new AtomicInteger();
		StepVerifier.create(Mono.create(s -> {
							s.onDispose(onDispose::getAndIncrement)
							 .onCancel(onCancel::getAndIncrement);
						}))
		            .thenAwait()
		            .consumeSubscriptionWith(Subscription::cancel)
		            .thenCancel()
		            .verify();
		assertThat(onDispose.get()).isEqualTo(1);
		assertThat(onCancel.get()).isEqualTo(1);
	}

	public void monoCreateDisposables() {
		AtomicInteger dispose1 = new AtomicInteger();
		AtomicInteger dispose2 = new AtomicInteger();
		AtomicInteger cancel1 = new AtomicInteger();
		AtomicInteger cancel2 = new AtomicInteger();
		AtomicInteger cancellation = new AtomicInteger();
		Mono<String> created = Mono.create(s -> {
			s.onDispose(dispose1::getAndIncrement)
			 .onCancel(cancel1::getAndIncrement);
			s.onDispose(dispose2::getAndIncrement);
			assertThat(dispose2.get()).isEqualTo(1);
			s.onCancel(cancel2::getAndIncrement);
			assertThat(cancel2.get()).isEqualTo(1);
			s.setCancellation(cancellation::getAndIncrement);
			assertThat(cancellation.get()).isEqualTo(1);
			assertThat(dispose1.get()).isEqualTo(0);
			assertThat(cancel1.get()).isEqualTo(0);
			s.success();
		});

		StepVerifier.create(created)
		            .verifyComplete();

		assertThat(dispose1.get()).isEqualTo(1);
		assertThat(cancel1.get()).isEqualTo(0);
	}

	@Test
	public void createStreamFromMonoCreate2() {
		StepVerifier.create(Mono.create(MonoSink::success)
		                        .publishOn(Schedulers.parallel()))
		            .verifyComplete();
	}
}