package net.adamcin.streamsupport;

import net.adamcin.streamsupport.throwing.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;
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
    public static <T> Both<Optional<T>> ofNullables(@Nullable T left, @Nullable T right) {
        return new Both<>(Optional.ofNullable(left), Optional.ofNullable(right));
    }

    /**
     * Unwrap Both of the input Result values into a Result of Both of the output values.
     *
     * @param results a pair of Results
     * @param <U>     The value type
     * @param <R>     the Result value type
     * @return a Result of the pair of values
     */
    public static <U, R extends Result<U>> Result<Both<U>> ofResults(@NotNull Both<R> results) {
        return results.left().flatMap(leftValue ->
                results.right().map(rightValue -> Both.of(leftValue, rightValue)));
    }

    /**
     * Return a {@link net.adamcin.streamsupport.Both} of empty {@link java.util.Optional}s.
     *
     * @param <T> the inferred type for the optionals
     * @return an empty Both
     */
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
     * @param <U> the result type
     * @return the result value
     */
    private <U> U applyLeft(final @NotNull Function<? super T, U> f) {
        return f.apply(leftValue);
    }

    /**
     * Defined to ensure a distinguishing stack element indicating that a throwable originated on the right.
     *
     * @param f   the function to apply
     * @param <U> the result type
     * @return the result value
     */
    private <U> U applyRight(final @NotNull Function<? super T, U> f) {
        return f.apply(rightValue);
    }

    /**
     * Apply the provided function to both values.
     *
     * @param f   the mapping function
     * @param <U> the result type
     * @return a new {@link net.adamcin.streamsupport.Both} with mapped left and right values
     */
    public @NotNull <U> Both<U> map(final @NotNull Function<? super T, U> f) {
        return Both.of(applyLeft(f), applyRight(f));
    }

    /**
     * Apply the provided function to both input values, wrapping each output value with {@link Optional#ofNullable(Object)}
     * in the {@link Both} that is returned.
     *
     * @param f   the mapping function
     * @param <U> the result type
     * @return a new {@link net.adamcin.streamsupport.Both} with mapped left and right values
     */
    public @NotNull <U> Both<Optional<U>> mapOptional(final @NotNull Function<? super T, U> f) {
        return Both.of(applyLeft(Fun.compose1(f, Optional::ofNullable)),
                applyRight(Fun.compose1(f, Optional::ofNullable)));
    }

    /**
     * Apply the provided throwing function to both values.
     *
     * @param f   the mapping function
     * @param <U> the result type
     * @return a new {@link net.adamcin.streamsupport.Both} with mapped left and right values
     */
    public @NotNull <U> Both<Result<U>> mapResult(final @NotNull ThrowingFunction<? super T, U> f) {
        return map(Fun.result1(f));
    }

    /**
     * Apply the provided BiFunction to both values as a pair.
     *
     * @param f   the mapping function
     * @param <U> the result type
     * @return the result of applying {@code f} over the left and right values
     */
    public @NotNull <U> U applyBoth(final @NotNull BiFunction<? super T, ? super T, U> f) {
        return f.apply(leftValue, rightValue);
    }

    /**
     * Apply the provided BiFunction to both values as a pair.
     *
     * @param f the consuming function
     */
    public void onBoth(final @NotNull BiConsumer<? super T, ? super T> f) {
        f.accept(leftValue, rightValue);
    }

    /**
     * Apply the provided BiFunction to both values as a pair.
     *
     * @param f the testing BiPredicate
     * @return the result of testing both values as a pair
     */
    public boolean testBoth(final @NotNull BiPredicate<? super T, ? super T> f) {
        return f.test(leftValue, rightValue);
    }

    /**
     * Return the values as a pair, represented with {@link java.util.Map.Entry}, for use with
     * {@link Fun#mapEntry(java.util.function.BiFunction)}.
     *
     * @return the left and right values as an entry
     */
    public @NotNull Map.Entry<T, T> entry() {
        return Fun.toEntry(leftValue, rightValue);
    }

    /**
     * Return the values as a {@link java.util.stream.Stream}.
     *
     * @return the values as a {@link java.util.stream.Stream}.
     */
    public @NotNull Stream<T> stream() {
        return Stream.concat(Stream.ofNullable(leftValue), Stream.ofNullable(rightValue));
    }

    /**
     * Swap the left value for the right value.
     *
     * @return a new {@link net.adamcin.streamsupport.Both} with left and right values swapped
     */
    public @NotNull Both<T> swap() {
        return Both.of(rightValue, leftValue);
    }

}
