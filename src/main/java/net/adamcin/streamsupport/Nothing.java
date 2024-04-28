/*
 * Copyright 2020 Mark Adamcin
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

package net.adamcin.streamsupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A sentinel type for functional parameters representing Nothing, like Void.
 */
public final class Nothing implements Serializable {
    @SuppressWarnings("WeakerAccess")
    static final String NAME = "Nothing";

    /**
     * This is the singleton sentinel value.
     */
    public static final Nothing instance = new Nothing();

    private Nothing() {
        /* prevent instantiation */
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> Function<T, Nothing>
    voidToNothing1(@NotNull Consumer<? super T> consumer) {
        return input -> {
            consumer.accept(input);
            return Nothing.instance;
        };
    }

    @SuppressWarnings("WeakerAccess")
    public static <T, U> BiFunction<T, U, Nothing>
    voidToNothing2(@NotNull BiConsumer<? super T, ? super U> consumer) {
        return (inputT, inputU) -> {
            consumer.accept(inputT, inputU);
            return Nothing.instance;
        };
    }

    @SuppressWarnings("WeakerAccess")
    public Nothing combine(@Nullable Nothing nothing) {
        return this;
    }

    @Override
    public String toString() {
        return NAME;
    }

    @Override
    public int hashCode() {
        return NAME.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof Nothing;
    }
}
