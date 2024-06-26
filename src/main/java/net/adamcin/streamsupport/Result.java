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
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A type representing either a successful result value, or failure, with an error.
 *
 * @param <V> The result type.
 */
@ProviderType
public abstract class Result<V> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Result.class);

    private Result() {
        /* construct from factory */
    }

    /**
     * Map it.
     *
     * @param f   the mapping function
     * @param <W> the result type.
     * @return a mapped result
     */
    @NotNull
    public abstract <W> Result<W> map(@NotNull Function<? super V, W> f);

    /**
     * Flat-map it.
     *
     * @param f   the mapping function
     * @param <W> the result type
     * @return the flat mapped result
     */
    @NotNull
    public abstract <W> Result<W> flatMap(@NotNull Function<? super V, Result<W>> f);

    public abstract V getOrDefault(V defaultValue);

    public abstract V getOrElse(@NotNull Supplier<? extends V> defaultValue);

    /**
     * Get the value if successful or throw a runtime exception.
     *
     * @return the wrapped value if successful
     * @throws java.lang.IllegalStateException if this result is a failure
     */
    @NotNull
    public abstract V getOrThrow();

    /**
     * Get the value if successful, or, throw the failure cause matching the provided errorType or the root runtime
     * exception if no matching cause is found.
     *
     * @param errorType the checked or unchecked exception type to throw if a matching failure cause is found
     * @param <E>       the error type parameter
     * @return the wrapped value if successful
     * @throws E                               if a matching cause is found
     * @throws java.lang.IllegalStateException if this result is a failure but {@code errorType} does not match a cause
     */
    @NotNull
    public abstract <E extends Exception> V getOrThrow(@NotNull Class<E> errorType) throws E;

    @NotNull
    public abstract Result<V> orElse(@NotNull Supplier<Result<V>> defaultValue);

    @NotNull
    public abstract Stream<V> stream();

    @NotNull
    public abstract Result<V> teeLogError();

    public final boolean isSuccess() {
        return !isFailure();
    }

    public final boolean isFailure() {
        return getError().isPresent();
    }

    @SuppressWarnings("WeakerAccess")
    @NotNull
    public final Optional<V> toOptional() {
        return stream().findFirst();
    }

    /**
     * All Failures will be created with a top-level RuntimeException. This method returns it if this result is a
     * failure. Otherwise, Optional.empty() is returned for a success.
     *
     * @return the top level runtime exception or empty if success
     */
    @NotNull
    public abstract Optional<RuntimeException> getError();

    /**
     * Filters the exception stack as a stream using the provided Throwable predicate.
     *
     * @param predicate the Throwable filter
     * @return some matching throwable or empty
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public final Optional<Throwable> findCause(@NotNull Predicate<? super Throwable> predicate) {
        return getError().map(Result::causing).orElse(Stream.empty()).filter(predicate).findFirst();
    }

    /**
     * Filters the exception stack as a stream, matching causes against the provided error type. Since the top-level
     * exception may be an internal RuntimeException, you can use this method to determine if a particular Throwable
     * type was thrown.
     *
     * @param errorType the class providing teh particular Exception type parameter
     * @param <E>       the particular Throwable type parameter
     * @return an Optional error
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public final <E extends Throwable> Optional<E> findCause(@NotNull Class<E> errorType) {
        return findCause(errorType::isInstance).map(errorType::cast);
    }

    /**
     * Feeling down because of too much functional wrapping? This method has you covered. Rethrow the highest result
     * error cause matching the provided type, so you can catch it like old times.
     *
     * @param errorType the class providing the particular Exception type parameter
     * @param <E>       the particular Exception type parameter
     * @throws E if any cause in the chain is an instance of the provided errorType, that cause is rethrown
     */
    public final <E extends Exception> void throwCause(@NotNull Class<E> errorType) throws E {
        Optional<E> cause = findCause(errorType);
        if (cause.isPresent()) {
            throw cause.get();
        }
    }

    /**
     * Produces a stream of Throwable causes for the provided throwable.
     *
     * @param caused the top-level exception
     * @return a stream of throwable causes
     */
    @SuppressWarnings("WeakerAccess")
    static Stream<Throwable> causing(@NotNull Throwable caused) {
        return Stream.concat(Optional.of(caused).stream(),
                Optional.ofNullable(caused.getCause()).map(Result::causing).orElse(Stream.empty()));
    }

    /**
     * Standard forEach method calling a consumer to accept the value. Not executed on a Failure.
     *
     * @param consumer the consumer
     */
    public abstract void forEach(@NotNull Consumer<? super V> consumer);

    /**
     * Wrapping runtime error type for exceptions rethrown by {@link net.adamcin.streamsupport.Result#getOrThrow()}.
     */
    @SuppressWarnings("WeakerAccess")
    static final class RethrownFailureException extends IllegalStateException {
        private RethrownFailureException(@NotNull Result.Failure<?> failedResult) {
            super(failedResult.toString(), failedResult.exception);
        }
    }

    private static final class Failure<V> extends Result<V> {
        private final RuntimeException exception;

        private Failure(String message) {
            super();
            this.exception = new IllegalStateException(message);
        }

        private Failure(@NotNull RuntimeException e) {
            this.exception = e;
        }

        private Failure(@NotNull Exception e) {
            this.exception = new IllegalStateException(e.getMessage(), e);
        }

        private Failure(@NotNull String message, @NotNull Exception e) {
            this.exception = new IllegalStateException(message, e);
        }

        @Override
        @NotNull
        public <W> Result<W> map(@NotNull Function<? super V, W> f) {
            return new Failure<>(this.exception);
        }

        @Override
        @NotNull
        public <W> Result<W> flatMap(@NotNull Function<? super V, Result<W>> f) {
            return new Failure<>(this.exception);
        }

        @Override
        public V getOrDefault(V defaultValue) {
            logSuppression();
            return defaultValue;
        }

        @Override
        public V getOrElse(@NotNull Supplier<? extends V> defaultValue) {
            logSuppression();
            return defaultValue.get();
        }

        @Override
        public @NotNull V getOrThrow() {
            throw new RethrownFailureException(this);
        }

        @Override
        public <E extends Exception> @NotNull V getOrThrow(@NotNull Class<E> errorType) throws E {
            throwCause(errorType);
            return getOrThrow();
        }

        @Override
        @NotNull
        public Result<V> orElse(@NotNull Supplier<Result<V>> defaultValue) {
            logSuppression();
            return Objects.requireNonNull(defaultValue.get());
        }

        @Override
        public void forEach(@NotNull Consumer<? super V> consumer) {
            logSuppression();
        }

        @Override
        @NotNull
        public Stream<V> stream() {
            logSuppression();
            return Stream.empty();
        }

        @Override
        @NotNull
        public Optional<RuntimeException> getError() {
            return Optional.of(this.exception);
        }

        @Override
        @NotNull
        public Result<V> teeLogError() {
            LOGGER.debug("failure [stacktrace visible in TRACE logging]: {}", this);
            logTrace();
            return this;
        }

        private void logTrace() {
            LOGGER.trace("thrown:", this.exception);
        }

        private void logSuppression() {
            LOGGER.debug("failure (suppressed) [stacktrace visible in TRACE logging]: {}", this);
            logTrace();
        }

        @Override
        public String toString() {
            return String.format("Failure(%s)", exception.getMessage());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Failure<?> failure = (Failure<?>) o;
            return Objects.equals(exception, failure.exception);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exception);
        }
    }

    private static final class Success<V> extends Result<V> {
        private final V value;

        private Success(final V value) {
            super();
            this.value = value;
        }

        @Override
        @NotNull
        public <W> Result<W> map(final @NotNull Function<? super V, W> f) {
            return new Success<>(f.apply(value));
        }

        @Override
        @NotNull
        public <W> Result<W> flatMap(final @NotNull Function<? super V, Result<W>> f) {
            return f.apply(value);
        }

        @Override
        public V getOrDefault(final V defaultValue) {
            return value;
        }

        @Override
        public V getOrElse(final @NotNull Supplier<? extends V> defaultValue) {
            return value;
        }

        @Override
        @NotNull
        public V getOrThrow() {
            return value;
        }

        @Override
        @NotNull
        public <E extends Exception> V getOrThrow(@NotNull Class<E> errorType) throws E {
            return value;
        }

        @Override
        @NotNull
        public Result<V> orElse(@NotNull Supplier<Result<V>> defaultValue) {
            return this;
        }

        @Override
        public void forEach(@NotNull Consumer<? super V> consumer) {
            consumer.accept(value);
        }

        @Override
        @NotNull
        public Stream<V> stream() {
            return value != null ? Stream.of(value) : Stream.empty();
        }

        @Override
        @NotNull
        public Result<V> teeLogError() {
            return this;
        }

        @Override
        @NotNull
        public Optional<RuntimeException> getError() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return String.format("Success(%s)", value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?> success = (Success<?>) o;
            return Objects.equals(value, success.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    @NotNull
    public static <V> Result<V> failure(String message) {
        return new Failure<>(message);
    }

    @NotNull
    public static <V> Result<V> failure(@NotNull Exception e) {
        return new Failure<>(e);
    }

    @NotNull
    public static <V> Result<V> failure(@NotNull String message, @NotNull Exception e) {
        return new Failure<>(message, e);
    }

    @NotNull
    public static <V> Result<V> failure(@NotNull RuntimeException e) {
        return new Failure<>(e);
    }

    @NotNull
    public static <V> Result<V> success(V value) {
        return new Success<>(value);
    }

    /**
     * Builds a result for a wrapped collector.
     *
     * @param <V> wrapped incremental value type
     * @param <A> wrapped accumulator type
     */
    public static final class Builder<V, A> implements Consumer<Result<V>> {
        final Result<A> resultAcc;
        final AtomicReference<Result<A>> latch;
        final BiConsumer<A, V> accumulator;

        Builder(@NotNull Result<A> initial,
                @NotNull BiConsumer<A, V> accumulator) {
            this.resultAcc = initial;
            this.latch = new AtomicReference<>(resultAcc);
            this.accumulator = accumulator;
        }

        @Override
        public void accept(Result<V> valueResult) {
            latch.accumulateAndGet(resultAcc,
                    (fromLatch, fromArg) ->
                            fromLatch.flatMap(state ->
                                    valueResult.map(value -> {
                                        accumulator.accept(state, value);
                                        return state;
                                    })));
        }

        Result<A> build() {
            return latch.get();
        }
    }

    /**
     * Create a collector that accumulates a stream of Results into a single Result containing either:
     * 1. the collected values of the streamed results according using supplied collector
     * 2. the first encountered failure
     * <p>
     * This method is indented to invert the relationship between the Result monoid and the Stream/Collector type,
     * such that this transformation becomes easier: {@code List<Result<A>> -> Result<List<A>>}
     *
     * @param collector the underlying collector
     * @param <V>       the incremental value
     * @param <R>       the intended container type
     * @param <A>       the collector's accumulator type
     * @return if all successful, a Result of a Collection; otherwise, the first encountered failure
     */
    @NotNull
    public static <V, R, A> Collector<Result<V>, Builder<V, A>, Result<R>>
    tryCollect(@NotNull Collector<V, A, R> collector) {

        final Supplier<Builder<V, A>>
                // first arg
                supplier = () ->
                new Builder<>(Result.success(collector.supplier().get()), collector.accumulator());

        final BiConsumer<Builder<V, A>, Result<V>>
                // second arg
                accumulator = Builder::accept;

        final BinaryOperator<Builder<V, A>>
                // third arg
                combiner =
                (builder0, builder1) -> new Builder<>(
                        builder0.build().flatMap(left ->
                                builder1.build().map(right ->
                                        collector.combiner().apply(left, right))),
                        collector.accumulator());

        final Function<Builder<V, A>, Result<R>>
                // fourth arg
                finisher = acc -> acc.build().map(collector.finisher());

        final Collector.Characteristics[]
                // fifth arg
                characteristics = collector.characteristics().stream()
                // remove IDENTITY_FINISH, but pass thru the other characteristics.
                .filter(Fun.inSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH)).negate())
                .toArray(Collector.Characteristics[]::new);

        return Collector.of(supplier, accumulator, combiner, finisher, characteristics);
    }

    @NotNull
    public static <V> Collector<Result<V>, Stream.Builder<Result<V>>, Stream<Result<V>>>
    logAndRestream() {
        return new RestreamLogCollector<>(LOGGER, "");
    }

    @NotNull
    public static <V> Collector<Result<V>, Stream.Builder<Result<V>>, Stream<Result<V>>>
    logAndRestream(@NotNull String message) {
        return new RestreamLogCollector<>(LOGGER, ": " + message);
    }

    static final class RestreamLogCollector<T>
            implements Collector<Result<T>, Stream.Builder<Result<T>>, Stream<Result<T>>> {
        final Supplier<Stream.Builder<Result<T>>> supplier;
        final BiConsumer<Stream.Builder<Result<T>>, Result<T>> accum;

        RestreamLogCollector(@NotNull Logger logger, @NotNull String collectorMessage) {
            if (logger.isDebugEnabled()) {
                final Throwable creation = new Throwable();
                this.supplier = () -> {
                    logger.debug("result collector (see TRACE for creation stack)" + collectorMessage);
                    logger.trace("created here", creation);
                    return Stream.builder();
                };
                this.accum = (builder, element) -> builder.accept(element.teeLogError());
            } else {
                this.supplier = Stream::builder;
                this.accum = Stream.Builder::accept;
            }
        }

        @Override
        public Supplier<Stream.Builder<Result<T>>> supplier() {
            return supplier;
        }

        @Override
        public BiConsumer<Stream.Builder<Result<T>>, Result<T>> accumulator() {
            return accum;
        }

        @Override
        public BinaryOperator<Stream.Builder<Result<T>>> combiner() {
            return (left, right) -> {
                right.build().forEachOrdered(left);
                return left;
            };
        }

        @Override
        public Function<Stream.Builder<Result<T>>, Stream<Result<T>>> finisher() {
            return Stream.Builder::build;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }
}
