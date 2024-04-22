/*
 * Copyright 2024 Mark Adamcin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.adamcin.streamsupport.io;

import net.adamcin.streamsupport.Nothing;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Just a simple IO monad.
 *
 * @param <A> the type of value read from "input" after execution, to be fed back into the functional execution chain.
 */
@ConsumerType
public interface IO<A> extends Supplier<A> {

    default <T> IO<T> add(final IO<T> io) {
        return () -> {
            IO.this.get();
            return io.get();
        };
    }

    IO<Nothing> empty = () -> Nothing.instance;

    static <A> IO<A> unit(A a) {
        return () -> a;
    }

    default <B> IO<B> map(final Function<A, B> f) {
        return () -> f.apply(this.get());
    }

    default <B> IO<B> flatMap(final Function<A, IO<B>> f) {
        return () -> f.apply(this.get()).get();
    }
}
