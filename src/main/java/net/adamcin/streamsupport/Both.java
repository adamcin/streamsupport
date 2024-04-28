package net.adamcin.streamsupport;

import net.adamcin.streamsupport.throwing.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A convenient container of two elements of the same type which may be subject to a comparison.
 *
 * @param <T> the type of both values
 */
public final class Both<T> implements Serializable {
    private final T leftValue;
    private final T rightValue;

    /**
     * Constructor.
     *
     * @param leftValue  the left value
     * @param rightValue the right value
     */
    private Both(@NotNull T leftValue, @NotNull T rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    /**
     * Construct an instance of {@link net.adamcin.streamsupport.Both} containing the provided values.
     *
     * @param left  the left value
     * @param right the right value
     * @param <T>   the type common to both values
     * @return both of the values
     */
    @NotNull
    public static <T> Both<T> of(@NotNull T left, @NotNull T right) {
        return new Both<>(left, right);
    }

    /**
     * Construct an instance of {@link net.adamcin.streamsupport.Both} containing the provided nullable values as Optionals.
     *
     * @param left  the left value
     * @param right the right value
     * @param <T>   the type common to both values
     * @return both of the Optional values
     */
    @NotNull
    public static <T> Both<Optional<T>> ofNullables(@Nullable T left, @Nullable T right) {
        return Both.of(Optional.ofNullable(left), Optional.ofNullable(right));
    }

    /**
     * Unwrap Both of the input Result values into a Result of Both of the output values.
     *
     * @param results a pair of Results
     * @param <V>     The value type
     * @param <R>     the Result value type
     * @return a Result of the pair of values
     */
    @NotNull
    public static <V, R extends Result<V>> Result<Both<V>> ofResults(@NotNull Both<R> results) {
        return results.left().flatMap(leftValue ->
                results.right().map(rightValue -> Both.of(leftValue, rightValue)));
    }

    /**
     * Return a {@link net.adamcin.streamsupport.Both} of empty {@link java.util.Optional}s.
     *
     * @param <T> the inferred type for the optionals
     * @return an empty Both
     */
    @NotNull
    public static <T> Both<Optional<T>> empty() {
        return Both.of(Optional.empty(), Optional.empty());
    }

    /**
     * Get the left-hand value.
     *
     * @return the left value
     */
    @NotNull
    public T left() {
        return leftValue;
    }

    /**
     * Get the right-hand value.
     *
     * @return the right value
     */
    @NotNull
    public T right() {
        return rightValue;
    }

    /**
     * Defined to ensure a distinguishing stack element indicating that a throwable originated on the left.
     *
     * @param f   the function to apply
     * @param <V> the result type
     * @return the result value
     */
    @NotNull
    private <V> V applyLeft(@NotNull Function<? super T, V> f) {
        return Objects.requireNonNull(f.apply(leftValue));
    }

    /**
     * Defined to ensure a distinguishing stack element indicating that a throwable originated on the right.
     *
     * @param f   the function to apply
     * @param <V> the result type
     * @return the result value
     */
    @NotNull
    private <V> V applyRight(@NotNull Function<? super T, V> f) {
        return Objects.requireNonNull(f.apply(rightValue));
    }

    /**
     * Apply the provided function to both values and return a Both of them.
     *
     * @param f   the mapping function
     * @param <V> the result type
     * @return a new {@link net.adamcin.streamsupport.Both} with mapped left and right values
     */
    @NotNull
    public <V> Both<V> map(@NotNull Function<? super T, V> f) {
        return Both.of(applyLeft(f), applyRight(f));
    }

    /**
     * Apply the provided BiFunction to both values, and return the result Both.
     *
     * @param f   the mapping BiFunction
     * @param <V> the result type
     * @return a Both resulting from the provided BiFunction
     */
    @NotNull
    public <V> Both<V> flatMap(@NotNull BiFunction<? super T, ? super T, Both<V>> f) {
        return Objects.requireNonNull(f.apply(leftValue, rightValue));
    }

    /**
     * Apply the provided function to both input values, wrapping each output value with {@link Optional#ofNullable(Object)}
     * in the {@link Both} that is returned.
     *
     * @param f   the mapping function
     * @param <V> the result type
     * @return a new {@link net.adamcin.streamsupport.Both} with mapped left and right values
     */
    @NotNull
    public <V> Both<Optional<V>> mapOptional(@NotNull Function<? super T, V> f) {
        final Function<? super T, Optional<V>> fOptional = Fun.compose1(f, Optional::ofNullable);
        return Both.of(applyLeft(fOptional), applyRight(fOptional));
    }

    /**
     * Apply the provided throwing function to both values.
     *
     * @param f   the mapping function
     * @param <V> the result type
     * @return a new {@link net.adamcin.streamsupport.Both} with mapped left and right values
     */
    @NotNull
    public <V> Both<Result<V>> mapResult(@NotNull ThrowingFunction<? super T, V> f) {
        return map(Fun.result1(f));
    }

    /**
     * Apply the provided BiFunction to both values as a pair.
     *
     * @param f   the mapping function
     * @param <V> the result type
     * @return the result of applying {@code f} over the left and right values
     */
    @NotNull
    public <V> V applyBoth(@NotNull BiFunction<? super T, ? super T, V> f) {
        return Objects.requireNonNull(f.apply(leftValue, rightValue));
    }

    /**
     * Apply the provided BiFunction to both values as a pair.
     *
     * @param f the consuming function
     */
    public void onBoth(@NotNull BiConsumer<? super T, ? super T> f) {
        f.accept(leftValue, rightValue);
    }

    /**
     * Apply the provided BiFunction to both values as a pair.
     *
     * @param f the testing BiPredicate
     * @return the result of testing both values as a pair
     */
    public boolean testBoth(@NotNull BiPredicate<? super T, ? super T> f) {
        return f.test(leftValue, rightValue);
    }

    /**
     * Combine the values of Both with the respective values of another Both, using the provided zipper function.
     *
     * @param that   the other Both of type {@code U}
     * @param zipper a {@link java.util.function.BiFunction} combining this type and that type to produce a third type
     * @param <U>    the other {@code Both}'s type
     * @param <V>    the inferred output type
     * @return Both of a third type {@code V}
     * @see #zip(Both) for example where {@code V} is {@code Map.Entry[T, U]}
     */
    @NotNull
    public <U, V> Both<V> zipWith(@NotNull Both<U> that,
                                  @NotNull BiFunction<? super T, ? super U, ? extends V> zipper) {
        return Both.of(zipper.apply(leftValue, that.leftValue), zipper.apply(rightValue, that.rightValue));
    }

    /**
     * Combine the values of Both with the respective values of another Both, producing a Both containing a {@link Map.Entry}
     * whose key type is {@code this} type and whose value type is {@code that}'s type.
     *
     * @param that the other Both of type {@code U}
     * @param <U>  the other {@code Both}'s type
     * @return a Both of the resulting  Map Entries
     * @see #zipWith(Both, java.util.function.BiFunction)
     */
    @NotNull
    public <U> Both<Map.Entry<T, U>> zip(@NotNull Both<U> that) {
        return zipWith(that, Fun::toEntry);
    }

    /**
     * Return the values as a pair, represented with {@link java.util.Map.Entry}, for use with
     * {@link Fun#mapEntry(java.util.function.BiFunction)}.
     *
     * @return the left and right values as an entry
     */
    @NotNull
    public Map.Entry<T, T> entry() {
        return Fun.toEntry(leftValue, rightValue);
    }

    /**
     * Return the values as a {@link java.util.stream.Stream}.
     *
     * @return the values as a {@link java.util.stream.Stream}.
     */
    @NotNull
    public Stream<T> stream() {
        return Stream.concat(Stream.ofNullable(leftValue), Stream.ofNullable(rightValue));
    }

    /**
     * Swap the left value for the right value.
     *
     * @return a new {@link net.adamcin.streamsupport.Both} with left and right values swapped
     */
    @NotNull
    public Both<T> swap() {
        return Both.of(rightValue, leftValue);
    }

    @Override
    public String toString() {
        return String.format("Both(%s, %s)", leftValue, rightValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Both<?> both = (Both<?>) o;
        return Objects.equals(leftValue, both.leftValue) && Objects.equals(rightValue, both.rightValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftValue, rightValue);
    }
}
