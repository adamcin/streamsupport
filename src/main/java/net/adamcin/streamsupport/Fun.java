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

import net.adamcin.streamsupport.throwing.ThrowingBiConsumer;
import net.adamcin.streamsupport.throwing.ThrowingBiFunction;
import net.adamcin.streamsupport.throwing.ThrowingBiPredicate;
import net.adamcin.streamsupport.throwing.ThrowingConsumer;
import net.adamcin.streamsupport.throwing.ThrowingFunction;
import net.adamcin.streamsupport.throwing.ThrowingPredicate;
import net.adamcin.streamsupport.throwing.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This alphabet soup provides function transformation methods targeting usage within Java 8+ Streams. Major support for
 * the following functional primitives:
 * <ol>
 * <li>{@link java.util.function.Function}</li>
 * <li>{@link java.util.function.BiFunction}</li>
 * <li>{@link java.util.function.Consumer}</li>
 * <li>{@link java.util.function.BiConsumer}</li>
 * <li>{@link java.util.function.Predicate}</li>
 * <li>{@link java.util.function.BiPredicate}</li>
 * <li>{@link java.util.function.Supplier}</li>
 * </ol>
 * In addition, the {@link java.util.Map.Entry} type is treated as a 2-n tuple, or pair, for mapping {@link java.util.Map#entrySet()} streams
 * to bifunctions, bipredicates, and biconsumers.
 * Some methods here serve a very simple purpose of supporting immediate type inference of method references,
 * like {@link #infer1(java.util.function.Function)}, which allows {@code infer1(String::valueOf).andThen()}.
 * To support method overloads in lambda and method ref type inference is in play, many method names have a numeric
 * suffix of 0, 1, or 2. (Sometimes the 1 is omitted even when "0" and "2" overloads are defined). This suffix indicates
 * the highest *arity of function arguments, and often represents the arity of a returned function type as well.
 * A Supplier argument represents an *arity of 0, because it takes 0 arguments in order to return a value. A Function
 * has an *arity of 1, because it accepts one argument to return one value. A BiFunction accepts two arguments, and
 * therefore has an *arity of 2. Consumers and Predicates also have an *arity of 1, and BiConsumers and BiPredicates also
 * have an *arity of 2.
 * Another area of treatment is transformation of checked signatures to unchecked signatures via the following functional
 * interfaces:
 * <ol>
 * <li>{@link net.adamcin.streamsupport.throwing.ThrowingFunction}</li>
 * <li>{@link net.adamcin.streamsupport.throwing.ThrowingBiFunction}</li>
 * <li>{@link net.adamcin.streamsupport.throwing.ThrowingConsumer}</li>
 * <li>{@link net.adamcin.streamsupport.throwing.ThrowingBiConsumer}</li>
 * <li>{@link net.adamcin.streamsupport.throwing.ThrowingPredicate}</li>
 * <li>{@link net.adamcin.streamsupport.throwing.ThrowingBiPredicate}</li>
 * <li>{@link net.adamcin.streamsupport.throwing.ThrowingSupplier}</li>
 * </ol>
 * The {@code uncheck*} methods will catch any checked exception thrown by the above types and rethrow as a
 * {@link net.adamcin.streamsupport.Fun.FunRuntimeException}.
 * The {@code result*} methods will catch any exception (checked or not) and transform the return type signature to wrap
 * with {@link Result}.
 * For {@link net.adamcin.streamsupport.throwing.ThrowingConsumer} and {@link net.adamcin.streamsupport.throwing.ThrowingBiConsumer}, there is no return type to wrap, so
 * {@link #resultNothing1(net.adamcin.streamsupport.throwing.ThrowingConsumer)} and {@link #resultNothing2(net.adamcin.streamsupport.throwing.ThrowingBiConsumer)} will transform the consumers
 * to equivalent functions that return a Result wrapping the {@link Nothing} sentinel type.
 */
@SuppressWarnings("WeakerAccess")
public final class Fun {
    private Fun() {
        // no construct
    }

    /**
     * What's the most efficient way to stream a single nullable element? I don't know, but maybe I randomly got it
     * right under the hood.
     *
     * @param element the single element to stream
     * @param <T>     the type of the element within the stream
     * @return a single-element stream
     */
    public static <T> Stream<T>
    streamIt(final @Nullable T element) {
        return element != null ? Stream.of(element) : Stream.empty();
    }

    /**
     * Use this to transform a less useful Optional into an equivalent single-element stream.
     *
     * @param element the single element to stream, wrapped in an Optional
     * @param <T>     the type of the element within the stream
     * @return a single-element stream
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Stream<T>
    streamOpt(final @NotNull Optional<T> element) {
        return element.map(Stream::of).orElse(Stream.empty());
    }

    /**
     * Transform a consumer into a "tee" (like the Unix command, evoking the shape of the letter, 'T') function that
     * passes the input argument to output after calling {@link java.util.function.Consumer#accept(Object)} with the input.
     *
     * @param consumer the consumer
     * @param <T>      the input type
     * @return a tee function
     */
    public static <T> @NotNull Function<T, T>
    tee(final @NotNull Consumer<? super T> consumer) {
        return input -> {
            consumer.accept(input);
            return input;
        };
    }

    /**
     * Fits a supplier into a pipeline as a constant {@link java.util.function.Function}.
     *
     * @param supplier the supplier
     * @param <T>      the (ignored) input type
     * @param <R>      the supplied output type
     * @return a constant Function
     */
    public static <T, R> @NotNull Function<T, R>
    constantly1(final @NotNull Supplier<? extends R> supplier) {
        return input -> supplier.get();
    }

    /**
     * Fits a supplier into a pipeline as a constant {@link java.util.function.BiFunction}.
     *
     * @param supplier the supplier
     * @param <K>      the (ignored) input entry key type
     * @param <V>      the (ignored) input entry value type
     * @param <R>      the supplied output type
     * @return a constant BiFunction
     */
    public static <K, V, R> @NotNull BiFunction<K, V, R>
    constantly2(final @NotNull Supplier<? extends R> supplier) {
        return (input0, input1) -> supplier.get();
    }

    /**
     * Essentially the same as calling {@link java.util.function.Function#andThen(java.util.function.Function)}, except needing no cast for a method reference
     * as the {@code before} argument, resulting in function composition of {@code before} followed by {@code after}.
     *
     * @param before the before function
     * @param after  the after function
     * @param <T>    the input type
     * @param <I>    the intermediate type
     * @param <R>    the output type
     * @return a composed Function
     */
    public static <T, I, R> Function<T, R>
    compose1(final @NotNull Function<T, ? extends I> before, final @NotNull Function<? super I, ? extends R> after) {
        return before.andThen(after);
    }

    /**
     * Compose a Supplier with a Function that maps the original supplied type to a new supplied type.
     *
     * @param before the supplier
     * @param after  the after function
     * @param <R>    the original supplied type
     * @param <S>    the new supplied type
     * @return a composed Supplier
     */
    public static <R, S> Supplier<S>
    compose0(final @NotNull Supplier<? extends R> before, final @NotNull Function<? super R, ? extends S> after) {
        final Function<Nothing, S> composed = compose1(constantly1(before), after);
        return () -> composed.apply(Nothing.instance);
    }

    /**
     * Essentially the same as calling {@link java.util.function.BiFunction#andThen(java.util.function.Function)}, except needing no cast for a method reference
     * as the {@code before} argument, resulting in function composition of {@code before} followed by {@code after}.
     *
     * @param before the before function
     * @param after  the after function
     * @param <K>    the input stream entry key type
     * @param <V>    the input stream entry value type
     * @param <I>    the intermediate type
     * @param <R>    the output type
     * @return a composed BiFunction
     */
    public static <K, V, I, R> BiFunction<K, V, R>
    compose2(final @NotNull BiFunction<K, V, ? extends I> before, final @NotNull Function<? super I, ? extends R> after) {
        return before.andThen(after);
    }

    /**
     * Compose a Predicate with a preceding input Function that maps the stream element type to the input type of the
     * Predicate.
     *
     * @param inputFunction the preceding input function
     * @param testResult    the predicate
     * @param <T>           the pipeline element type
     * @param <P>           the original predicate input type
     * @return a composed Predicate
     */
    public static <T, P> Predicate<T>
    composeTest1(final @NotNull Function<? super T, ? extends P> inputFunction,
                 final @NotNull Predicate<? super P> testResult) {
        return input -> testResult.test(inputFunction.apply(input));
    }

    /**
     * Compose a BiPredicate with preceding left and right input Functions that map the stream Entry element key and value
     * types to the respective input type of the provided BiPredicate.
     *
     * @param inputTFunction the preceding left input function
     * @param inputUFunction the preceding right input function
     * @param testResult     the predicate
     * @param <K>            the stream entry key type
     * @param <V>            the stream entry value type
     * @param <P>            the original predicate left input type
     * @param <Q>            the original predicate right input type
     * @return a composed BiPredicate
     */
    public static <K, V, P, Q> BiPredicate<K, V>
    composeTest2(final @NotNull Function<? super K, ? extends P> inputTFunction,
                 final @NotNull Function<? super V, ? extends Q> inputUFunction,
                 final @NotNull BiPredicate<? super P, ? super Q> testResult) {
        return (inputK, inputV) -> testResult.test(inputTFunction.apply(inputK), inputUFunction.apply(inputV));
    }

    /**
     * Compose a BiPredicate with a single preceding BiFunction that maps the stream Entry element key and value types to
     * the input type of the provided Predicate.
     *
     * @param inputFunction the preceding input BiFunction
     * @param testResult    the predicate
     * @param <K>           the stream entry key type
     * @param <V>           the stream entry value type
     * @param <P>           the original predicate input type
     * @return a composed BiPredicate
     */
    public static <K, V, P> BiPredicate<K, V>
    composeTest2(final @NotNull BiFunction<? super K, ? super V, ? extends P> inputFunction,
                 final @NotNull Predicate<? super P> testResult) {
        return (inputK, inputV) -> testResult.test(inputFunction.apply(inputK, inputV));
    }

    /**
     * Infers a {@link java.util.function.Function#apply(Object)} signature as a {@link java.util.function.Consumer}.
     *
     * @param inputFunction the Function to use as a Consumer
     * @param <T>           the consumed input type
     * @return a Consumer
     */
    public static <T> Consumer<T>
    toVoid1(final @NotNull Function<? super T, ?> inputFunction) {
        return inputFunction::apply;
    }

    /**
     * Infers a {@link java.util.function.BiFunction#apply(Object, Object)} signature as a {@link java.util.function.BiConsumer}.
     *
     * @param inputFunction the BiFunction to use as a BiConsumer
     * @param <K>           the consumed stream entry key type
     * @param <V>           the consumed stream entry value type
     * @return a composed BiConsumer
     */
    public static <K, V> BiConsumer<K, V>
    toVoid2(final @NotNull BiFunction<? super K, ? super V, ?> inputFunction) {
        return inputFunction::apply;
    }

    /**
     * Infers a static method ref to a Function.
     *
     * @param methodRef the method ref
     * @param <T>       the input type
     * @param <R>       the output type
     * @return an inferred Function
     */
    public static <T, R> Function<T, R>
    infer1(final @NotNull Function<? super T, ? extends R> methodRef) {
        return methodRef::apply;
    }

    /**
     * Infers a static method ref to a BiFunction.
     *
     * @param methodRef the method ref
     * @param <K>       the input key type
     * @param <V>       the input value type
     * @param <R>       the return type
     * @return an inferred BiFunction
     */
    public static <K, V, R> BiFunction<K, V, R>
    infer2(final @NotNull BiFunction<? super K, ? super V, ? extends R> methodRef) {
        return methodRef::apply;
    }

    /**
     * Infers a method ref to a Supplier.
     *
     * @param methodRef the method ref
     * @param <T>       the supplied type
     * @return an inferred Supplier
     */
    public static <T> Supplier<T>
    infer0(final @NotNull Supplier<? extends T> methodRef) {
        return methodRef::get;
    }

    /**
     * Infers a method ref to a Predicate.
     *
     * @param methodRef the method ref
     * @param <T>       the input type
     * @return an inferred Predicate
     */
    public static <T> Predicate<T>
    inferTest1(final @NotNull Predicate<? super T> methodRef) {
        return methodRef::test;
    }

    /**
     * Infers a method ref to a BiPredicate.
     *
     * @param methodRef the method ref
     * @param <K>       the input key type
     * @param <V>       the input value type
     * @return an inferred BiPredicate
     */
    public static <K, V> BiPredicate<K, V>
    inferTest2(final @NotNull BiPredicate<? super K, ? super V> methodRef) {
        return methodRef::test;
    }

    /**
     * Parallel method to {@link Nothing#voidToNothing1(java.util.function.Consumer)}, for transforming a {@link net.adamcin.streamsupport.throwing.ThrowingConsumer} to a
     * {@link net.adamcin.streamsupport.throwing.ThrowingFunction} that returns {@link Nothing}.
     *
     * @param mayThrowOnAccept the throwing consumer
     * @param <T>              the consumed type
     * @return a {@link net.adamcin.streamsupport.throwing.ThrowingFunction} that returns {@link Nothing}
     */
    public static <T> ThrowingFunction<T, Nothing>
    throwingVoidToNothing1(final @NotNull ThrowingConsumer<? super T> mayThrowOnAccept) {
        return input -> {
            mayThrowOnAccept.tryAccept(input);
            return Nothing.instance;
        };
    }

    /**
     * Parallel method to {@link Nothing#voidToNothing2(java.util.function.BiConsumer)}, for transforming a {@link net.adamcin.streamsupport.throwing.ThrowingBiConsumer} to a
     * {@link net.adamcin.streamsupport.throwing.ThrowingBiFunction} that returns {@link Nothing}.
     *
     * @param mayThrowOnAccept the throwing BiConsumer
     * @param <K>              the consumed key type
     * @param <V>              the consumed value type
     * @return a {@link net.adamcin.streamsupport.throwing.ThrowingBiFunction} that returns {@link Nothing}
     */
    public static <K, V> ThrowingBiFunction<K, V, Nothing>
    throwingVoidToNothing2(final @NotNull ThrowingBiConsumer<? super K, ? super V> mayThrowOnAccept) {
        return (inputK, inputV) -> {
            mayThrowOnAccept.tryAccept(inputK, inputV);
            return Nothing.instance;
        };
    }

    /**
     * Like {@link #tee(java.util.function.Consumer)}, this method transforms a {@link java.util.function.BiConsumer} into a "tee" (like the Unix command,
     * evoking the shape of the letter, 'T') function for {@link java.util.Map.Entry} streams, that passes the input entry to output
     * after calling {@link java.util.function.BiConsumer#accept(Object, Object)} with the input entry key and value.
     *
     * @param consumer the BiConsumer function
     * @param <K>      the input entry key type
     * @param <V>      the input entry value type
     * @return a tee function for Map.Entry streams
     */
    public static <K, V> Function<Map.Entry<K, V>, Map.Entry<K, V>>
    entryTee(final @NotNull BiConsumer<? super K, ? super V> consumer) {
        return entry -> {
            consumer.accept(entry.getKey(), entry.getValue());
            return entry;
        };
    }

    /**
     * Zips stream elements into {@link java.util.Map.Entry} elements as keys, with values mapped from the provided function, which
     * can, for example, be a {@link java.util.Map#get(Object)} method reference to perform a lookup.
     *
     * @param valueFunc the function to map a stream element as a key to an associated value for the constructed {@link java.util.Map.Entry}
     * @param <K>       the stream element and entry key type
     * @param <V>       the entry value type
     * @return a function to map a stream element type to an entry with the same type for the key
     */
    public static <K, V> Function<K, Map.Entry<K, V>>
    zipKeysWithValueFunc(final @NotNull Function<? super K, ? extends V> valueFunc) {
        return key -> toEntry(key, valueFunc.apply(key));
    }

    /**
     * Zips stream elements into {@link java.util.Map.Entry} elements as values, with keys mapped from the provided function, which
     * can, for example, be a {@link java.util.Map#get(Object)} method reference to perform a lookup.
     *
     * @param keyFunction the function to map a stream element as a value to an associated key for the constructed {@link java.util.Map.Entry}
     * @param <K>         the entry key type
     * @param <V>         the stream element and entry value type
     * @return a function to map a stream element type to an entry with the same type for the value
     */
    public static <K, V> Function<V, Map.Entry<K, V>>
    zipValuesWithKeyFunc(final @NotNull Function<? super V, ? extends K> keyFunction) {
        return value -> toEntry(keyFunction.apply(value), value);
    }

    /**
     * An alias for creating a {@link java.util.AbstractMap.SimpleImmutableEntry} for the provided key and value.
     *
     * @param key   the entry key
     * @param value the entry value
     * @param <K>   the entry key type
     * @param <V>   the entry value type
     * @return an immutable {@link java.util.Map.Entry}
     */
    public static <K, V> Map.Entry<K, V>
    toEntry(final @Nullable K key, final @Nullable V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /**
     * A merge function for {@link #entriesToMap(java.util.function.BinaryOperator)} and {@link #entriesToMapOfType(java.util.function.Supplier, java.util.function.BinaryOperator)}
     * that keeps the first element defined for a key when merging duplicates.
     *
     * @param <V> the entry value type
     * @return a merge function
     */
    public static <V> BinaryOperator<V> keepFirstMerger() {
        return (kv1, kv2) -> kv1;
    }

    /**
     * A merge function for {@link #entriesToMap(java.util.function.BinaryOperator)} and {@link #entriesToMapOfType(java.util.function.Supplier, java.util.function.BinaryOperator)}
     * that keeps the last element defined for a key when merging duplicates.
     *
     * @param <V> the entry value type
     * @return a merge function
     */
    public static <V> BinaryOperator<V> keepLastMerger() {
        return (kv1, kv2) -> kv2;
    }

    /**
     * A merge function for {@link #entriesToMap(java.util.function.BinaryOperator)} and {@link #entriesToMapOfType(java.util.function.Supplier, java.util.function.BinaryOperator)}
     * that throws an {@link IllegalStateException} when merging duplicates to guarantee distinct pairs.
     *
     * @param <V> the entry value type
     * @return a merge function
     */
    public static <V> BinaryOperator<V> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    /**
     * Shorthand for {@link java.util.stream.Collectors#toMap(java.util.function.Function, java.util.function.Function)} for collecting streams of {@link java.util.Map.Entry}, except
     * using {@link #keepLastMerger()} as the merge function, and {@link java.util.LinkedHashMap} as the supplied map type. This
     * preserves stream sequence by default.
     *
     * @param <K> the entry key type
     * @param <V> the entry value type
     * @return a Map {@link java.util.stream.Collector} for a stream of {@link java.util.Map.Entry}
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>>
    entriesToMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, keepLastMerger(), LinkedHashMap::new);
    }

    /**
     * Shorthand for {@link java.util.stream.Collectors#toMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator, java.util.function.Supplier)} for collecting streams of
     * {@link java.util.Map.Entry}, except using {@link #keepLastMerger()} as the merge function.
     *
     * @param mapSupplier a Map constructor method reference
     * @param <K>         the entry key type
     * @param <V>         the entry value type
     * @return a Map {@link java.util.stream.Collector} for a stream of {@link java.util.Map.Entry}
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>>
    entriesToMapOfType(final @NotNull Supplier<Map<K, V>> mapSupplier) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, keepLastMerger(), mapSupplier);
    }

    /**
     * Shorthand for {@link java.util.stream.Collectors#toMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator, java.util.function.Supplier)} for collecting streams of
     * {@link java.util.Map.Entry}, except using {@link java.util.LinkedHashMap} as the supplied map type.
     *
     * @param mergeFunction a function to merge entry values on key duplication.
     * @param <K>           the entry key type
     * @param <V>           the entry value type
     * @return a Map {@link java.util.stream.Collector} for a stream of {@link java.util.Map.Entry}
     * @see #keepFirstMerger()
     * @see #keepLastMerger()
     * @see #throwingMerger()
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>>
    entriesToMap(final @NotNull BinaryOperator<V> mergeFunction) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction, LinkedHashMap::new);
    }

    /**
     * Shorthand for {@link java.util.stream.Collectors#toMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator, java.util.function.Supplier)} for collecting streams of
     * {@link java.util.Map.Entry}.
     *
     * @param mapSupplier   a Map constructor method reference
     * @param mergeFunction a function to merge entry values on key duplication.
     * @param <K>           the entry key type
     * @param <V>           the entry value type
     * @return a Map {@link java.util.stream.Collector} for a stream of {@link java.util.Map.Entry}
     * @see #keepFirstMerger()
     * @see #keepLastMerger()
     * @see #throwingMerger()
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>>
    entriesToMapOfType(final @NotNull Supplier<Map<K, V>> mapSupplier, final @NotNull BinaryOperator<V> mergeFunction) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction, mapSupplier);
    }

    /**
     * Transform a {@link java.util.function.BiFunction} to a {@link java.util.function.Function} over {@link java.util.Map.Entry} stream elements of the same key and
     * value type parameters.
     *
     * @param biMapFunction the mapping BiFunction
     * @param <K>           the entry key type
     * @param <V>           the entry value type
     * @param <R>           the output type
     * @return a Function mapping over {@link java.util.Map.Entry} elements
     */
    public static <K, V, R> Function<Map.Entry<K, V>, R>
    mapEntry(final @NotNull BiFunction<? super K, ? super V, ? extends R> biMapFunction) {
        return entry -> biMapFunction.apply(entry.getKey(), entry.getValue());
    }

    /**
     * Transform a {@link java.util.function.BiFunction} to a {@link java.util.function.Function} over {@link java.util.Map.Entry} stream elements of the same key and
     * value type parameters, that returns new {@link java.util.Map.Entry} elements that retain the input key but take the output
     * of the {@link java.util.function.BiFunction} as the new value.
     *
     * @param valueBiFunction the mapping BiFunction
     * @param <K>             the input and output entry key type
     * @param <V>             the input entry value type
     * @param <W>             the output entry value type
     * @return a Function mapping over {@link java.util.Map.Entry} elements
     */
    public static <K, V, W> Function<Map.Entry<K, V>, Map.Entry<K, W>>
    mapValue(final @NotNull BiFunction<? super K, ? super V, ? extends W> valueBiFunction) {
        return entry -> toEntry(entry.getKey(), valueBiFunction.apply(entry.getKey(), entry.getValue()));
    }

    /**
     * Transform a {@link java.util.function.Function} over value types to a {@link java.util.function.Function} over {@link java.util.Map.Entry} stream elements whose
     * value type matches the input type of the {@link java.util.function.Function}, that returns new {@link java.util.Map.Entry} elements that retain
     * the input key but take the output of the {@link java.util.function.Function} as the new value.
     *
     * @param valueFunction the mapping Function
     * @param <K>           the input and output entry key type
     * @param <V>           the input entry value type
     * @param <W>           the output entry value type
     * @return a Function mapping over {@link java.util.Map.Entry} elements
     */
    public static <K, V, W> Function<Map.Entry<K, V>, Map.Entry<K, W>>
    mapValue(final @NotNull Function<? super V, ? extends W> valueFunction) {
        return mapValue((key, value) -> valueFunction.apply(value));
    }

    /**
     * Transform a {@link java.util.function.BiFunction} to a {@link java.util.function.Function} over {@link java.util.Map.Entry} stream elements of the same key and
     * value type parameters, that returns new {@link java.util.Map.Entry} elements that retain the input value but take the output
     * of the {@link java.util.function.BiFunction} as the new key.
     *
     * @param keyBiFunction the mapping Function
     * @param <K>           the input entry key type
     * @param <V>           the input and output entry value type
     * @param <L>           the output entry key type
     * @return a Function mapping over {@link java.util.Map.Entry} elements
     */
    public static <K, V, L> Function<Map.Entry<K, V>, Map.Entry<L, V>>
    mapKey(final @NotNull BiFunction<? super K, ? super V, ? extends L> keyBiFunction) {
        return entry -> toEntry(keyBiFunction.apply(entry.getKey(), entry.getValue()), entry.getValue());
    }

    /**
     * Transform a {@link java.util.function.Function} over key types to a {@link java.util.function.Function} over {@link java.util.Map.Entry} stream elements whose
     * key type matches the input type of the {@link java.util.function.Function}, that returns new {@link java.util.Map.Entry} elements that retain
     * the input value but take the output of the {@link java.util.function.Function} as the new key.
     *
     * @param keyFunction the mapping Function
     * @param <K>         the input entry key type
     * @param <V>         the input and output entry value type
     * @param <L>         the output entry key type
     * @return a Function mapping over {@link java.util.Map.Entry} elements
     */
    public static <K, V, L> Function<Map.Entry<K, V>, Map.Entry<L, V>>
    mapKey(final @NotNull Function<? super K, ? extends L> keyFunction) {
        return mapKey((key, value) -> keyFunction.apply(key));
    }

    /**
     * Transform a {@link java.util.function.BiConsumer} to a {@link java.util.function.Consumer} of {@link java.util.Map.Entry} stream elements with matching respective
     * key and value type parameters.
     *
     * @param biConsumer the BiConsumer
     * @param <K>        the stream entry key type
     * @param <V>        the stream entry value type
     * @return a {@link java.util.function.Consumer} of {@link java.util.Map.Entry}
     */
    public static <K, V> Consumer<Map.Entry<K, V>>
    onEntry(final @NotNull BiConsumer<? super K, ? super V> biConsumer) {
        return entry -> biConsumer.accept(entry.getKey(), entry.getValue());
    }

    /**
     * Transform a {@link java.util.function.Consumer} of key types to a {@link java.util.function.Consumer} of {@link java.util.Map.Entry} stream elements with a matching
     * key type parameter.
     *
     * @param consumer the key consumer
     * @param <K>      the input type and stream entry key type
     * @param <V>      the stream entry value type
     * @return a {@link java.util.function.Consumer} of {@link java.util.Map.Entry} stream elements
     */
    public static <K, V> Consumer<Map.Entry<K, V>>
    onKey(final @NotNull Consumer<? super K> consumer) {
        return entry -> consumer.accept(entry.getKey());
    }

    /**
     * Transform a {@link java.util.function.Consumer} of value types to a {@link java.util.function.Consumer} of {@link java.util.Map.Entry} stream elements with a matching
     * value type parameter.
     *
     * @param consumer the key consumer
     * @param <K>      the stream entry key type
     * @param <V>      the input type and stream entry value type
     * @return a {@link java.util.function.Consumer} of {@link java.util.Map.Entry} stream elements
     */
    public static <K, V> Consumer<Map.Entry<K, V>>
    onValue(final @NotNull Consumer<? super V> consumer) {
        return entry -> consumer.accept(entry.getValue());
    }

    /**
     * Transform a {@link java.util.function.BiPredicate} to a {@link java.util.function.Predicate} testing {@link java.util.Map.Entry} stream elements with matching
     * respective key and value type parameters.
     *
     * @param biPredicate the BiPredicate
     * @param <K>         the input key type
     * @param <V>         the input value type
     * @return a {@link java.util.function.Predicate} filtering {@link java.util.Map.Entry} stream elements
     */
    public static <K, V> Predicate<? super Map.Entry<K, V>>
    testEntry(final @NotNull BiPredicate<? super K, ? super V> biPredicate) {
        return entry -> biPredicate.test(entry.getKey(), entry.getValue());
    }

    /**
     * Transform a {@link java.util.function.Predicate} of value types to a {@link java.util.function.Predicate} testing {@link java.util.Map.Entry} stream elements with
     * a matching value type parameter.
     *
     * @param valuePredicate the Predicate
     * @param <K>            the stream entry key type
     * @param <V>            the input type and stream entry value type
     * @return a {@link java.util.function.Predicate} filtering {@link java.util.Map.Entry} stream elements
     */
    public static <K, V> Predicate<? super Map.Entry<K, V>>
    testValue(final @NotNull Predicate<? super V> valuePredicate) {
        return testEntry((key, value) -> valuePredicate.test(value));
    }

    /**
     * Transform a {@link java.util.function.Predicate} of key types to a {@link java.util.function.Predicate} testing {@link java.util.Map.Entry} stream elements with
     * a matching key type parameter.
     *
     * @param keyPredicate the Predicate
     * @param <K>          the input value and stream entry key type
     * @param <V>          the stream entry value type
     * @return a {@link java.util.function.Predicate} filtering {@link java.util.Map.Entry} stream elements
     */
    public static <K, V> Predicate<? super Map.Entry<K, V>>
    testKey(final @NotNull Predicate<? super K> keyPredicate) {
        return testEntry((key, value) -> keyPredicate.test(key));
    }

    /**
     * This method is used to support type inference around the {@link java.util.Collection#contains(Object)} method, whose argument
     * is not otherwise typed with the element type parameter of the collection. This returns the method reference for the
     * provided collection's {@code contains} method as a Predicate, but with restrictive type parameters so that it does
     * not break inference by introducing {@code Object} as the input type bound.
     *
     * @param haystack the collection possibly containing stream elements
     * @param <T>      the stream element type
     * @param <S>      the collection type wildcard
     * @return {@code haystack::contains} as a properly bounded predicate
     */
    public static <T, S extends Collection<? super T>> Predicate<T>
    inSet(final @NotNull S haystack) {
        return haystack::contains;
    }

    /**
     * This method is used to support type inference around the {@link java.util.Map#containsKey(Object)} method, whose argument
     * is not otherwise typed with the key type parameter of the Map. This returns the method reference for the
     * provided map's {@code containsKey} method as a Predicate, but with restrictive type parameters so that it does
     * not break inference by introducing {@code Object} as the input type bound.
     *
     * @param haystack the map possibly containing stream elements as keys
     * @param <K>      the stream element type
     * @param <M>      the map type wildcard
     * @return {@code haystack::containsKey} as a properly bounded predicate
     */
    public static <K, M extends Map<? super K, ?>> Predicate<K>
    isKeyIn(final @NotNull M haystack) {
        return haystack::containsKey;
    }

    /**
     * This method is used to support type inference around the {@link java.util.Map#containsValue(Object)} method, whose argument
     * is not otherwise typed with the value type parameter of the Map. This returns the method reference for the
     * provided map's {@code containsValue} method as a Predicate, but with restrictive type parameters so that it does
     * not break inference by introducing {@code Object} as the input type bound.
     *
     * @param haystack the map possibly containing stream elements as values
     * @param <V>      the stream element type
     * @param <M>      the map type wildcard
     * @return {@code haystack::containsValue} as a properly bounded predicate
     */
    public static <V, M extends Map<?, ? super V>> Predicate<V>
    isValueIn(final @NotNull M haystack) {
        return haystack::containsValue;
    }

    /**
     * Wrapping runtime error type for unchecked throwing functions.
     */
    public static final class FunRuntimeException extends RuntimeException {
        private FunRuntimeException(final @NotNull Throwable cause) {
            super(cause);
        }
    }

    /**
     * Composes four lambdas into a single function for use with flatMap() defined by {@link java.util.stream.Stream},
     * {@link java.util.Optional}, etc. Useful for eliminating clumsy try/catch blocks from lambdas.
     *
     * @param monadUnit       the "unit" (or "single") function defined by the appropriate monoid/monad. I.E. Stream::of,
     *                        Optional::of, or Optional::ofNullable.
     * @param monadZero       the "zero" (or "empty") function defined by the appropriate monoid/monad, as in Stream::empty,
     *                        or Optional::empty
     * @param mayThrowOnApply some function that produces type {@code R} when applied to an input of type {@code T}, or fails
     *                        with an Exception.
     * @param onError         an optional consumer function to perform some logic when the parser function throws.
     *                        Receives both the failing input element and the caught Exception.
     * @param <M>             The captured monad type, which must match the return types of the {@code monadUnit} and
     *                        {@code monadZero} functions, but which is not involved in the {@code onElement} or
     *                        {@code onError} functions.
     * @param <T>             The input type mapped by the monoid/monad, i.e. the String type in {@code Stream<String>}.
     * @param <R>             The output type mapped by the monoid/monad, i.e. the URL type in {@code Stream<URL>}.
     * @return a function that never throws an exception.
     */
    public static <M, T, R> Function<T, M>
    composeTry1(final @NotNull Function<? super R, ? extends M> monadUnit,
                final @NotNull Supplier<? extends M> monadZero,
                final @NotNull ThrowingFunction<? super T, ? extends R> mayThrowOnApply,
                final @Nullable BiConsumer<? super T, ? super Exception> onError) {
        final BiConsumer<? super T, ? super Exception> consumeError = onError != null
                ? onError
                : (e, t) -> { /* do nothing */ };

        return element -> {
            try {
                return monadUnit.apply(mayThrowOnApply.tryApply(element));
            } catch (final Exception error) {
                consumeError.accept(element, error);
                return monadZero.get();
            }
        };
    }

    /**
     * Composes four lambdas into a single function for use with flatMap() defined by {@link java.util.stream.Stream},
     * {@link java.util.Optional}, etc. Useful for eliminating clumsy try/catch blocks from lambdas.
     * This variation is geared towards use with {@link Result} or some other union type with an Exception constructor.
     *
     * @param monoidSuccess   the "successful" function defined by the appropriate monoid/monad. I.E. Result::success,
     *                        Optional::of, or Optional::ofNullable.
     * @param monoidError     the "failure" function defined by the appropriate monoid/monad, as in Result::failure.
     * @param mayThrowOnApply some function that produces type {@code R} when given an object of type {@code T}, or fails
     *                        with an Exception.
     * @param <M>             The captured monoid type, which must match the return types of the {@code monoidSuccess} and
     *                        {@code monoidError} functions, but which is not involved in the {@code mayThrowOnApply} function.
     * @param <T>             The input type mapped by the monoid/monad, i.e. the String type in {@code Stream<String>}.
     * @param <R>             The output type mapped by the monoid/monad, i.e. the URL type in {@code Stream<URL>}.
     * @return a function that returns a union type distinguishable between a result type and an error type
     */
    public static <M, T, R> Function<T, M>
    composeTry1(final @NotNull Function<? super R, ? extends M> monoidSuccess,
                final @NotNull Function<? super Exception, ? extends M> monoidError,
                final @NotNull ThrowingFunction<? super T, ? extends R> mayThrowOnApply) {
        return element -> {
            try {
                return monoidSuccess.apply(mayThrowOnApply.tryApply(element));
            } catch (final Exception error) {
                return monoidError.apply(error);
            }
        };
    }

    /**
     * Composes four lambdas into a single supplier for use with flatMap() defined by {@link java.util.stream.Stream},
     * {@link java.util.Optional}, etc. Useful for eliminating clumsy try/catch blocks from lambdas.
     *
     * @param monadUnit     the "unit" (or "single") function defined by the appropriate monoid/monad. I.E. Stream::of,
     *                      Optional::of, or Optional::ofNullable.
     * @param monadZero     the "zero" (or "empty") function defined by the appropriate monoid/monad, as in Stream::empty,
     *                      or Optional::empty
     * @param mayThrowOnGet some supplier that produces type {@code R}, or fails
     *                      with an Exception.
     * @param onError       an optional consumer function to perform some logic when the parser function throws.
     *                      Receives both the failing input element and the caught Exception.
     * @param <M>           The captured monad type, which must match the return types of the {@code monadUnit} and
     *                      {@code monadZero} functions, but which is not involved in the {@code onElement} or
     *                      {@code onError} functions.
     * @param <R>           The output type mapped by the monoid/monad, i.e. the URL type in {@code Stream<URL>}.
     * @return a supplier that never throws an exception.
     */
    public static <M, R> Supplier<M>
    composeTry0(final @NotNull Function<? super R, ? extends M> monadUnit,
                final @NotNull Supplier<? extends M> monadZero,
                final @NotNull ThrowingSupplier<? extends R> mayThrowOnGet,
                final @Nullable Consumer<? super Exception> onError) {
        final Consumer<? super Exception> consumeError = onError != null
                ? onError
                : t -> {
        };

        return () -> {
            try {
                return monadUnit.apply(mayThrowOnGet.tryGet());
            } catch (final Exception error) {
                consumeError.accept(error);
                return monadZero.get();
            }
        };
    }

    /**
     * Composes four lambdas into a single supplier for use with flatMap() defined by {@link java.util.stream.Stream},
     * {@link java.util.Optional}, etc. Useful for eliminating clumsy try/catch blocks from lambdas.
     * This variation is geared towards use with {@link Result} or some other union type with an Exception constructor.
     *
     * @param monoidSuccess the "successful" function defined by the appropriate monoid/monad. I.E. Result::success,
     *                      Optional::of, or Optional::ofNullable.
     * @param monoidError   the "failure" function defined by the appropriate monoid/monad, as in Result::failure.
     * @param mayThrowOnGet some function that produces type {@code R} when given an object of type {@code T}, or fails
     *                      with an Exception.
     * @param <M>           The captured monoid type, which must match the return types of the {@code monoidSuccess} and
     *                      {@code monoidError} functions, but which is not involved in the {@code mayThrowOnApply} function.
     * @param <R>           The output type mapped by the monoid/monad, i.e. the URL type in {@code Stream<URL>}.
     * @return a supplier that returns a union type distinguishable between a result type and an error type
     */
    public static <M, R> Supplier<M>
    composeTry0(final @NotNull Function<? super R, ? extends M> monoidSuccess,
                final @NotNull Function<? super Exception, ? extends M> monoidError,
                final @NotNull ThrowingSupplier<? extends R> mayThrowOnGet) {
        return () -> {
            try {
                return monoidSuccess.apply(mayThrowOnGet.tryGet());
            } catch (final Exception error) {
                return monoidError.apply(error);
            }
        };
    }

    /**
     * Composes four lambdas into a single bifunction for use with flatMap() defined by {@link java.util.stream.Stream},
     * {@link java.util.Optional}, etc. Useful for eliminating clumsy try/catch blocks from lambdas.
     *
     * @param monadUnit       the "unit" (or "single") function defined by the appropriate monoid/monad. I.E. Stream::of,
     *                        Optional::of, or Optional::ofNullable.
     * @param monadZero       the "zero" (or "empty") function defined by the appropriate monoid/monad, as in Stream::empty,
     *                        or Optional::empty
     * @param mayThrowOnApply some function that produces type {@code R} when applied to inputs of type {@code T} and {@code U},
     *                        or fails with an Exception.
     * @param onError         an optional consumer function to perform some logic when the parser function throws.
     *                        Receives both the failing input element and the caught Exception.
     * @param <M>             The captured monad type, which must match the return types of the {@code monadUnit} and
     *                        {@code monadZero} functions, but which is not involved in the {@code onElement} or
     *                        {@code onError} functions.
     * @param <K>             The left input type mapped by the function, i.e. the String type in {@code Stream<String>}.
     * @param <V>             The right input type mapped by the function, i.e. the String type in {@code Stream<String>}.
     * @param <R>             The output type mapped by the monoid/monad, i.e. the URL type in {@code Stream<URL>}.
     * @return a BiFunction that never throws an exception.
     */
    public static <M, K, V, R> BiFunction<K, V, M>
    composeTry2(final @NotNull Function<? super R, ? extends M> monadUnit,
                final @NotNull Supplier<? extends M> monadZero,
                final @NotNull ThrowingBiFunction<? super K, ? super V, ? extends R> mayThrowOnApply,
                final @Nullable BiConsumer<? super Map.Entry<? super K, ? super V>, ? super Exception> onError) {

        return (elementK, elementV) -> {
            try {
                return monadUnit.apply(mayThrowOnApply.tryApply(elementK, elementV));
            } catch (final Exception error) {
                if (onError != null) {
                    onError.accept(toEntry(elementK, elementV), error);
                }
                return monadZero.get();
            }
        };
    }

    /**
     * Composes four lambdas into a single function for use with flatMap() defined by {@link java.util.stream.Stream},
     * {@link java.util.Optional}, etc. Useful for eliminating clumsy try/catch blocks from lambdas.
     * This variation is geared towards use with {@link Result} or some other union type with an Exception constructor.
     *
     * @param monoidSuccess   the "successful" function defined by the appropriate monoid/monad. I.E. Result::success,
     *                        Optional::of, or Optional::ofNullable.
     * @param monoidError     the "failure" function defined by the appropriate monoid/monad, as in Result::failure.
     * @param mayThrowOnApply some function that produces type {@code R} when given inputs of type {@code T} and {@code U},
     *                        or fails with an Exception.
     * @param <M>             The captured monoid type, which must match the return types of the {@code monoidSuccess} and
     *                        {@code monoidError} functions, but which is not involved in the {@code mayThrowOnApply} function.
     * @param <K>             The left input type mapped by the function
     * @param <V>             The right input type mapped by the function
     * @param <R>             The output type mapped by the monoid/monad, i.e. the URL type in {@code Stream<URL>}.
     * @return a function that returns a union type distinguishable between a result type and an error type
     */
    public static <M, K, V, R> BiFunction<K, V, M>
    composeTry2(final @NotNull Function<? super R, ? extends M> monoidSuccess,
                final @NotNull Function<? super Exception, ? extends M> monoidError,
                final @NotNull ThrowingBiFunction<? super K, ? super V, ? extends R> mayThrowOnApply) {
        return (elementK, elementV) -> {
            try {
                return monoidSuccess.apply(mayThrowOnApply.tryApply(elementK, elementV));
            } catch (final Exception error) {
                return monoidError.apply(error);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingSupplier} to an unchecked {@link java.util.function.Supplier} of the same supplied type parameter.
     * Exceptions will be caught and rethrown as {@link net.adamcin.streamsupport.Fun.FunRuntimeException}s.
     *
     * @param mayThrowOnGet the ThrowingSupplier
     * @param <R>           the supplied type
     * @return an unchecked {@link java.util.function.Supplier}
     */
    public static <R> Supplier<R>
    uncheck0(final @NotNull ThrowingSupplier<? extends R> mayThrowOnGet) {
        return () -> {
            try {
                return mayThrowOnGet.tryGet();
            } catch (Exception e) {
                throw new FunRuntimeException(e);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingSupplier} to a {@link java.util.function.Supplier} of {@link Result} whose success type parameter is the
     * same as the type parameter of the {@link net.adamcin.streamsupport.throwing.ThrowingSupplier}.
     *
     * @param mayThrowOnGet the ThrowingSupplier
     * @param <R>           the originally supplied type
     * @return a {@link java.util.function.Supplier} of {@link Result}s of type {@code R}
     */
    public static <R> Supplier<Result<R>>
    result0(final @NotNull ThrowingSupplier<? extends R> mayThrowOnGet) {
        return composeTry0(Result::success, Result::failure, mayThrowOnGet);
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingFunction} to an unchecked {@link java.util.function.Function} of the same input and output type parameters.
     * Exceptions will be caught and rethrown as {@link net.adamcin.streamsupport.Fun.FunRuntimeException}s.
     *
     * @param mayThrowOnApply the ThrowingFunction
     * @param <T>             the input type
     * @param <R>             the output type
     * @return an unchecked {@link java.util.function.Function}
     */
    public static <T, R> Function<T, R>
    uncheck1(final @NotNull ThrowingFunction<? super T, ? extends R> mayThrowOnApply) {
        return input -> {
            try {
                return mayThrowOnApply.tryApply(input);
            } catch (Exception e) {
                throw new FunRuntimeException(e);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingFunction} to a {@link java.util.function.Function} over the same input type parameter, which returns a
     * {@link Result} whose success type parameter is the same as the output type of the {@link net.adamcin.streamsupport.throwing.ThrowingFunction}.
     *
     * @param mayThrowOnApply the ThrowingFunction
     * @param <T>             the input type
     * @param <R>             the original output type
     * @return a {@link java.util.function.Function} over the same input returning a {@link Result} of type {@code R}
     */
    public static <T, R> Function<T, Result<R>>
    result1(final @NotNull ThrowingFunction<? super T, ? extends R> mayThrowOnApply) {
        return composeTry1(Result::success, Result::failure, mayThrowOnApply);
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiFunction} to an unchecked {@link java.util.function.BiFunction} of the same input and output type
     * parameters. Exceptions will be caught and rethrown as {@link net.adamcin.streamsupport.Fun.FunRuntimeException}s.
     *
     * @param mayThrowOnApply the ThrowingBiFunction
     * @param <K>             the input entry key type
     * @param <V>             the input entry value type
     * @param <R>             the output type
     * @return an unchecked {@link java.util.function.BiFunction}
     */
    public static <K, V, R> BiFunction<K, V, R>
    uncheck2(final @NotNull ThrowingBiFunction<? super K, ? super V, ? extends R> mayThrowOnApply) {
        return (inputK, inputV) -> {
            try {
                return mayThrowOnApply.tryApply(inputK, inputV);
            } catch (Exception e) {
                throw new FunRuntimeException(e);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiFunction} to a {@link java.util.function.BiFunction} over the same input type parameters, which returns
     * a {@link Result} whose success type parameter is the same as the output type of the {@link net.adamcin.streamsupport.throwing.ThrowingBiFunction}.
     *
     * @param mayThrowOnApply the ThrowingBiFunction
     * @param <K>             the input entry key type
     * @param <V>             the input entry value type
     * @param <R>             the original output type
     * @return a {@link java.util.function.BiFunction} over the same inputs returning a {@link Result} of type {@code R}
     */
    public static <K, V, R> BiFunction<K, V, Result<R>>
    result2(final @NotNull ThrowingBiFunction<? super K, ? super V, ? extends R> mayThrowOnApply) {
        return composeTry2(Result::success, Result::failure, mayThrowOnApply);
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingPredicate} to a {@link java.util.function.Predicate} of the same input type parameter. Exceptions will be
     * caught and rethrown as {@link net.adamcin.streamsupport.Fun.FunRuntimeException}s.
     *
     * @param mayThrowOnTest the ThrowingPredicate
     * @param <T>            the input type
     * @return an unchecked {@link java.util.function.Predicate}
     */
    public static <T> Predicate<T>
    uncheckTest1(final @NotNull ThrowingPredicate<? super T> mayThrowOnTest) {
        return input -> {
            try {
                return mayThrowOnTest.tryTest(input);
            } catch (Exception e) {
                throw new FunRuntimeException(e);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiPredicate} to a {@link java.util.function.BiPredicate} of the same input type parameter. Exceptions will
     * be caught and rethrown as {@link net.adamcin.streamsupport.Fun.FunRuntimeException}s.
     *
     * @param mayThrowOnTest the ThrowingBiPredicate
     * @param <K>            the input entry key type
     * @param <V>            the input entry value type
     * @return an unchecked {@link java.util.function.BiPredicate}
     */
    public static <K, V> BiPredicate<K, V>
    uncheckTest2(final @NotNull ThrowingBiPredicate<? super K, ? super V> mayThrowOnTest) {
        return (inputK, inputV) -> {
            try {
                return mayThrowOnTest.tryTest(inputK, inputV);
            } catch (Exception e) {
                throw new FunRuntimeException(e);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingConsumer} to a {@link java.util.function.Consumer} of the same input type parameter. Exceptions will
     * be caught and rethrown as {@link net.adamcin.streamsupport.Fun.FunRuntimeException}s.
     *
     * @param mayThrowOnAccept the ThrowingConsumer
     * @param <T>              the input type
     * @return an unchecked {@link java.util.function.Consumer}
     */
    public static <T> Consumer<T>
    uncheckVoid1(final @NotNull ThrowingConsumer<? super T> mayThrowOnAccept) {
        return input -> {
            try {
                mayThrowOnAccept.tryAccept(input);
            } catch (Exception e) {
                throw new FunRuntimeException(e);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingConsumer} to a {@link java.util.function.Function} over the same input type parameter, but which returns
     * a {@link Result} of success type {@link Nothing}.
     *
     * @param mayThrowOnAccept the ThrowingConsumer
     * @param <T>              the input type
     * @return a {@link java.util.function.Function} returning a {@link Result} of type {@link Nothing}
     */
    public static <T> Function<T, Result<Nothing>>
    resultNothing1(final @NotNull ThrowingConsumer<? super T> mayThrowOnAccept) {
        return result1(throwingVoidToNothing1(mayThrowOnAccept));
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiConsumer} to a {@link java.util.function.BiConsumer} of the same input type parameter. Exceptions will
     * be caught and rethrown as {@link net.adamcin.streamsupport.Fun.FunRuntimeException}s.
     *
     * @param mayThrowOnAccept the ThrowingBiConsumer
     * @param <K>              the input entry key type
     * @param <V>              the input entry value type
     * @return an unchecked {@link java.util.function.BiConsumer}
     */
    public static <K, V> BiConsumer<K, V>
    uncheckVoid2(final @NotNull ThrowingBiConsumer<? super K, ? super V> mayThrowOnAccept) {
        return (inputK, inputV) -> {
            try {
                mayThrowOnAccept.tryAccept(inputK, inputV);
            } catch (Exception e) {
                throw new FunRuntimeException(e);
            }
        };
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiConsumer} to a {@link java.util.function.BiFunction} over the same input type parameters, but which returns
     * a {@link Result} of success type {@link Nothing}.
     *
     * @param mayThrowOnAccept the ThrowingBiConsumer
     * @param <K>              the input entry key type
     * @param <V>              the input entry value type
     * @return a {@link java.util.function.BiFunction} returning a {@link Result} of type {@link Nothing}
     */
    public static <K, V> BiFunction<K, V, Result<Nothing>>
    resultNothing2(final @NotNull ThrowingBiConsumer<? super K, ? super V> mayThrowOnAccept) {
        return result2(throwingVoidToNothing2(mayThrowOnAccept));
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingPredicate} to a {@link java.util.function.Predicate}, providing a default boolean value to return if an
     * exception is thrown.
     *
     * @param mayThrowOnTest the ThrowingPredicate
     * @param defaultValue   the value to return when an exception is thrown
     * @param <T>            the input type
     * @return a {@link java.util.function.Predicate} that returns a default value when an exception is thrown
     */
    public static <T> Predicate<T>
    testOrDefault1(final @NotNull ThrowingPredicate<? super T> mayThrowOnTest, boolean defaultValue) {
        return compose1(result1(mayThrowOnTest::tryTest), result -> result.getOrDefault(defaultValue))::apply;
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiPredicate} to a {@link java.util.function.BiPredicate}, providing a default boolean value to return if
     * an exception is thrown.
     *
     * @param mayThrowOnTest the ThrowingBiPredicate
     * @param defaultValue   the value to return when an exception is thrown
     * @param <K>            the input entry key type
     * @param <V>            the input entry value type
     * @return a {@link java.util.function.BiPredicate} that returns a default value when an exception is thrown
     */
    public static <K, V> BiPredicate<K, V>
    testOrDefault2(final @NotNull ThrowingBiPredicate<? super K, ? super V> mayThrowOnTest, boolean defaultValue) {
        return compose2(result2(mayThrowOnTest::tryTest), result -> result.getOrDefault(defaultValue))::apply;
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingSupplier} to a {@link java.util.function.Supplier}, providing a default value to return if
     * an exception is thrown.
     *
     * @param mayThrowOnGet the ThrowingSupplier
     * @param defaultValue  the value to return when an exception is thrown
     * @param <R>           the supplied type
     * @return a {@link java.util.function.Supplier} that supplies a default value when an exception is thrown
     */
    public static <R> Supplier<R>
    tryOrDefault0(final @NotNull ThrowingSupplier<R> mayThrowOnGet, @Nullable R defaultValue) {
        return compose0(result0(mayThrowOnGet), result -> result.getOrDefault(defaultValue));
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingFunction} to a {@link java.util.function.Function}, providing a default value to return if
     * an exception is thrown.
     *
     * @param mayThrowOnApply the ThrowingFunction
     * @param defaultValue    the value to return when an exception is thrown
     * @param <T>             the input type
     * @param <R>             the output type
     * @return a {@link java.util.function.Function} that returns a default value when an exception is thrown
     */
    public static <T, R> Function<T, R>
    tryOrDefault1(final @NotNull ThrowingFunction<? super T, R> mayThrowOnApply, @Nullable R defaultValue) {
        return compose1(result1(mayThrowOnApply), result -> result.getOrDefault(defaultValue));
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiFunction} to a {@link java.util.function.BiFunction}, providing a default value to return if
     * an exception is thrown.
     *
     * @param mayThrowOnApply the ThrowingBiFunction
     * @param defaultValue    the value to return when an exception is thrown
     * @param <K>             the input entry key type
     * @param <V>             the input entry value type
     * @param <R>             the output type
     * @return a {@link java.util.function.BiFunction} that returns a default value when an exception is thrown
     */
    public static <K, V, R> BiFunction<K, V, R>
    tryOrDefault2(final @NotNull ThrowingBiFunction<? super K, ? super V, R> mayThrowOnApply, @Nullable R defaultValue) {
        return compose2(result2(mayThrowOnApply), result -> result.getOrDefault(defaultValue));
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingSupplier} to a {@link java.util.function.Supplier} of the same type, wrapped in an {@link java.util.Optional}.
     * The {@link java.util.Optional} value will be empty if an exception is thrown.
     *
     * @param mayThrowOnGet the ThrowingSupplier
     * @param <R>           the originally supplied type
     * @return a {@link java.util.function.Supplier} that supplies an optional value that is empty if an exception is thrown
     */
    public static <R> Supplier<Optional<R>>
    tryOrOptional0(final @NotNull ThrowingSupplier<R> mayThrowOnGet) {
        return compose0(result0(mayThrowOnGet), Result::toOptional);
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingFunction} to a {@link java.util.function.Function} of the same input type, with the output type wrapped in
     * an {@link java.util.Optional}. The {@link java.util.Optional} value will be empty if an exception is thrown.
     *
     * @param mayThrowOnApply the ThrowingFunction
     * @param <T>             the input type
     * @param <R>             the original output type
     * @return a {@link java.util.function.Function} that returns an optional value that is empty if an exception is thrown
     */
    public static <T, R> Function<T, Optional<R>>
    tryOrOptional1(final @NotNull ThrowingFunction<? super T, R> mayThrowOnApply) {
        return compose1(result1(mayThrowOnApply), Result::toOptional);
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiFunction} to a {@link java.util.function.BiFunction} of the same input types, with the output type
     * wrapped in an {@link java.util.Optional}. The {@link java.util.Optional} value will be empty if an exception is thrown.
     *
     * @param mayThrowOnApply the ThrowingBiFunction
     * @param <K>             the input entry key type
     * @param <V>             the input entry value type
     * @param <R>             the original output type
     * @return a {@link java.util.function.BiFunction} that returns an optional value that is empty if an exception is thrown
     */
    public static <K, V, R> BiFunction<K, V, Optional<R>>
    tryOrOptional2(final @NotNull ThrowingBiFunction<? super K, ? super V, R> mayThrowOnApply) {
        return compose2(result2(mayThrowOnApply), Result::toOptional);
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingConsumer} to a {@link java.util.function.Consumer} of the same input type, that simply suppresses any
     * exceptions if thrown.
     *
     * @param mayThrowOnAccept the ThrowingConsumer
     * @param <T>              the input type
     * @return a {@link java.util.function.Consumer} that suppresses any exceptions if thrown
     */
    public static <T> Consumer<T>
    tryOrVoid1(final @NotNull ThrowingConsumer<? super T> mayThrowOnAccept) {
        return compose1(resultNothing1(mayThrowOnAccept), Result::teeLogError)::apply;
    }

    /**
     * Transform a {@link net.adamcin.streamsupport.throwing.ThrowingBiConsumer} to a {@link java.util.function.BiConsumer} of the same input types, that simply suppresses any
     * exceptions if thrown.
     *
     * @param mayThrowOnAccept the ThrowingBiConsumer
     * @param <K>              the input entry key type
     * @param <V>              the input entry value type
     * @return a {@link java.util.function.Consumer} that suppresses any exceptions if thrown
     */
    public static <K, V> BiConsumer<K, V>
    tryOrVoid2(final @NotNull ThrowingBiConsumer<? super K, ? super V> mayThrowOnAccept) {
        return compose2(resultNothing2(mayThrowOnAccept), Result::teeLogError)::apply;
    }
}

