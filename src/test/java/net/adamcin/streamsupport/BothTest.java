package net.adamcin.streamsupport;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BothTest {

    @Test
    void empty() {
        Both<Optional<Integer>> values = Both.empty();
        assertTrue(values.left().isEmpty());
        assertTrue(values.right().isEmpty());
    }

    @Test
    void of() {
        Both<Integer> values = Both.of(1, 2);
        assertEquals(1, values.left());
        assertEquals(2, values.right());
    }

    @Test
    void ofNullables() {
        Both<Optional<Integer>> values = Both.ofNullables(null, 2);
        assertTrue(values.left().isEmpty());
        assertEquals(Optional.of(2), values.right());
    }

    @Test
    void ofResults() {
        Result<Both<Integer>> result = Both.ofResults(Both.of(Result.success(1), Result.success(2)));
        assertEquals(1, result.getOrThrow().left());
        assertEquals(2, result.getOrThrow().right());

        Result<Both<Integer>> rightFail = Both.ofResults(Both.of(Result.success(1), Result.failure("a failure on the right")));
        assertTrue(rightFail.isFailure());
        assertEquals(Optional.of("a failure on the right"), rightFail.getError().map(RuntimeException::getMessage));

        Result<Both<Integer>> leftFail = Both.ofResults(Both.of(Result.failure("a failure on the left"), Result.success(2)));
        assertTrue(leftFail.isFailure());
        assertEquals(Optional.of("a failure on the left"), leftFail.getError().map(RuntimeException::getMessage));

        Result<Both<Integer>> bothFail = Both.ofResults(Both.of(Result.failure("a failure on the left"),
                Result.failure("a failure on the right")));
        assertTrue(bothFail.isFailure());
        assertEquals(Optional.of("a failure on the left"), bothFail.getError().map(RuntimeException::getMessage));
    }

    @Test
    void map() {
        Both<Integer> values = Both.of(1, 2).map(value -> value * 2);
        assertEquals(2, values.left());
        assertEquals(4, values.right());
    }

    @Test
    void mapOptional() {
        Both<Optional<Integer>> values = Both.of(1, 2).mapOptional(value -> value % 2 == 0 ? value : null);
        assertEquals(Optional.empty(), values.left());
        assertEquals(Optional.of(2), values.right());
    }

    @Test
    void mapResult() {
        Both<Result<Integer>> values = Both.of(1, 2).mapResult(value -> {
            if (value % 2 == 0) {
                return value;
            } else {
                throw new IllegalStateException("odd number");
            }
        });
        assertEquals(Optional.of("odd number"), values.left().getError().map(RuntimeException::getMessage));
        assertEquals(Result.success(2), values.right());
    }

    @Test
    void flatMap() {
        Both<Integer> values = Both.of(1, 2)
                .flatMap((left, right) -> Both.of(left + 5, right - 7));
        assertEquals(6, values.left());
        assertEquals(-5, values.right());
    }

    @Test
    void applyBoth() {
        Both<Integer> values = Both.of(1, 2);
        assertEquals(Integer.valueOf(3), values.applyBoth(Math::addExact));
    }

    @Test
    void onBoth() throws Exception {
        Both<Integer> values = Both.of(1, 2);
        CompletableFuture<Integer> sum = new CompletableFuture<>();
        values.onBoth((left, right) -> sum.complete(left + right));
        assertEquals(3, sum.get());
    }

    @Test
    void testBoth() {
        Both<Integer> values = Both.of(1, 2);
        assertFalse(values.testBoth((left, right) -> left % 2 == 0 && right % 2 == 0));
        assertTrue(values.testBoth((left, right) -> left > 0 && right > 0));
    }

    @Test
    void swap() {
        Both<Integer> values = Both.of(1, 2).swap();
        assertEquals(2, values.left());
        assertEquals(1, values.right());
    }

    @Test
    void entry() {
        Map.Entry<Integer, Integer> values = Both.of(1, 2).entry();
        assertEquals(1, values.getKey());
        assertEquals(2, values.getValue());
    }

    @Test
    void stream() {
        Both<Integer> values = Both.of(1, 2);
        assertEquals(List.of(1, 2), values.stream().collect(Collectors.toList()));
    }

    @Test
    void zip() {
        Both<Integer> values = Both.of(1, 2);
        Both<String> items = Both.of("apples", "oranges");
        Both<Map.Entry<Integer, String>> results = values.zip(items);
        assertEquals(1, results.left().getKey());
        assertEquals("apples", results.left().getValue());
        assertEquals(2, results.right().getKey());
        assertEquals("oranges", results.right().getValue());
    }

    @Test
    void zipWith() {
        Both<Integer> values = Both.of(1, 2);
        Both<String> items = Both.of("apples", "oranges");
        Both<String> results = values.zipWith(items, (value, item) -> String.format("%d %s", value, item));
        assertEquals("1 apples", results.left());
        assertEquals("2 oranges", results.right());
    }

    @Test
    void equalsHashCodeToString() {
        Both<Integer> values = Both.of(1, 2);
        assertEquals("Both(1, 2)", values.toString());
        assertEquals(Objects.hash(1, 2), values.hashCode());
        assertEquals(Both.of(1, 2), values);
    }
}
