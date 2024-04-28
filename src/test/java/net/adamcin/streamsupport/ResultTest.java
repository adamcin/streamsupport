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

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.adamcin.streamsupport.Fun.result1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResultTest {
    private static final String specialLoggerError = "special.error";

    @Test
    public void testIsSuccess() {
        assertTrue(Result.success("success").isSuccess());
        assertFalse(Result.failure("failure").isSuccess());
    }

    @Test
    public void testIsFailure() {
        assertFalse(Result.success("success").isFailure());
        assertTrue(Result.failure("failure").isFailure());
    }

    @Test
    public void testToOptional() {
        assertTrue(Result.success("success").toOptional().isPresent());
        assertFalse(Result.failure("failure").toOptional().isPresent());
    }

    @Test
    public void testFindCause_predicate() {
        assertFalse(Result.success("success").findCause(cause -> true).isPresent());
        assertTrue(Result.failure("failure").findCause(cause -> true).isPresent());
    }

    private static class SignatureCause extends RuntimeException {

    }

    @Test
    public void testFindCause_class() {
        assertFalse(Result.success("success").findCause(SignatureCause.class).isPresent());
        assertFalse(Result.failure("failure").findCause(SignatureCause.class).isPresent());
        assertTrue(Result.failure(new SignatureCause()).findCause(SignatureCause.class).isPresent());
    }

    @Test
    public void testThrowCause() {
        Result.success("success").throwCause(SignatureCause.class);
        Result.failure("failure").throwCause(SignatureCause.class);
    }

    @Test
    public void testThrowCause_throws() {
        assertThrows(SignatureCause.class, () -> {
            Result.failure(new SignatureCause()).throwCause(SignatureCause.class);
        });
    }

    @Test
    public void testFailure_constructMessageError() {
        final Result<String> result = Result.failure("failure", new SignatureCause());
        assertEquals("Failure(failure)", result.toString());
    }

    @Test
    public void testGetOrDefault() {
        final String defaultValue = "defaultValue";
        assertNotEquals("success gets non-default",
                defaultValue, Result.success("success").getOrDefault(defaultValue));
        assertEquals(defaultValue, Result.failure("failure").getOrDefault(defaultValue));
    }

    @Test
    public void testGetOrElse() {
        final String defaultValue = "defaultValue";
        assertNotEquals("success gets non-default",
                defaultValue, Result.success("success").getOrElse(() -> defaultValue));
        assertEquals(defaultValue, Result.failure("failure").getOrElse(() -> defaultValue));
    }

    @Test
    void getOrThrow_success() {
        assertEquals("success", Result.success("success").getOrThrow());
        assertEquals("success", Result.success("success").getOrThrow(RuntimeException.class));
    }

    @Test
    void getOrThrow_rethrow() {
        final Result<String> result = Result.failure("failure", new SignatureCause());
        assertThrows(Result.RethrownFailureException.class, result::getOrThrow);
    }

    @Test
    void getOrThrow_throwCause() {
        final Result<String> result = Result.failure("failure", new SignatureCause());
        assertThrows(SignatureCause.class, () -> result.getOrThrow(SignatureCause.class));
    }

    @Test
    void getOrThrow_throwCause_notMatched() {
        final Result<String> result = Result.failure("failure", new SignatureCause());
        assertThrows(Result.RethrownFailureException.class, () -> result.getOrThrow(IOException.class));
    }

    @Test
    public void testOrElse() {
        final Result<String> defaultValue = Result.success("defaultValue");
        assertNotSame(defaultValue, Result.success("success").orElse(() -> defaultValue));
        assertSame(defaultValue, Result.<String>failure("failure").orElse(() -> defaultValue));
    }

    @Test
    public void testTeeLogError() {
        final Result<String> success = Result.success("success");
        final Result<String> failure = Result.failure("failure");
        assertSame(success, success.teeLogError());
        assertSame(failure, failure.teeLogError());
    }

    @Test
    public void testForEach() {
        final CompletableFuture<String> slotSuccess = new CompletableFuture<>();
        Result.success("success").forEach(slotSuccess::complete);
        assertEquals("success", slotSuccess.getNow(null));

        final CompletableFuture<String> slotFailure = new CompletableFuture<>();
        Result.<String>failure("failure").forEach(slotFailure::complete);
        assertFalse(slotFailure.isDone());
    }

    @Test
    public void testMap() {
        final Result<String> success = Result.success("success");
        final Result<String> successMapped = success.map(String::toUpperCase);
        assertEquals("SUCCESS", successMapped.getOrDefault("success"));

        final Result<String> failure = Result.failure("failure");
        final Result<String> failureMapped = failure.map(String::toUpperCase);
        assertSame(failure.getError().get(), failureMapped.getError().get());
    }

    @Test
    public void testToString() {
        final Result<String> success = Result.success("success");
        final Result<String> failure = Result.failure("failure");
        assertEquals("Success(success)", success.toString());
        assertEquals("Failure(failure)", failure.toString());
    }

    @Test
    public void testBuilder() {
        final Result<Set<String>> setResult = Stream.of(
                Result.success("a"),
                Result.success("b"),
                Result.success("c")).collect(Result.tryCollect(Collectors.toSet()));

        assertTrue(setResult.isSuccess());
    }

    @Test
    public void testCollectOrFailOnFirst() {
        List<Result<Integer>> rawValues = Stream.of(0, 1, 2, 3, 4, 5, 6)
                .map(result1(value -> value / (value % 3)))
                .collect(Collectors.toList());

        Result<List<Integer>> allAreGood = rawValues.stream().collect(Result.tryCollect(Collectors.toList()));

        assertTrue(allAreGood.isFailure());
        assertTrue(allAreGood.findCause(ArithmeticException.class::isInstance).isPresent());
    }

    @Test
    public void testLogAndRestream() {
        final Set<Result<String>> original = Stream.of("a", "b", "c")
                .map(Result::success).collect(Collectors.toSet());
        assertEquals(original,
                original.stream().collect(Result.logAndRestream()).collect(Collectors.toSet()));
        assertEquals(original,
                original.stream().collect(Result.logAndRestream("collect message")).collect(Collectors.toSet()));
        assertEquals(original, original.stream()
                .collect(new Result.RestreamLogCollector<>(LoggerFactory.getLogger(specialLoggerError), "withError"))
                .collect(Collectors.toSet()));
    }

    @Test
    public void testTryCollect_combiner() {
        final Set<Result<String>> original = Stream.of("a", "b", "c", "d", "e")
                .map(Result::success).collect(Collectors.toSet());
        Result<List<String>> collected = StreamSupport.stream(original.spliterator(), true)
                .collect(Result.tryCollect(Collectors.toList()));
    }

    @Test
    public void testLogAndRestream_combiner() {
        final Set<Result<String>> original = Stream.of("a", "b", "c", "d", "e")
                .map(Result::success).collect(Collectors.toSet());
        assertEquals(original,
                StreamSupport.stream(original.spliterator(), true).collect(Result.logAndRestream()).collect(Collectors.toSet()));
        assertEquals(original,
                StreamSupport.stream(original.spliterator(), true).collect(Result.logAndRestream("collect message")).collect(Collectors.toSet()));
        assertEquals(original, StreamSupport.stream(original.spliterator(), true)
                .collect(new Result.RestreamLogCollector<>(LoggerFactory.getLogger(specialLoggerError), "withError"))
                .collect(Collectors.toSet()));
    }

    @Test
    void equalsHashCodeToString() {
        final Result<String> success = Result.success("success");
        final Result<String> failure = Result.failure("failure");
        assertEquals("Success(success)", success.toString());
        assertEquals(String.format("Failure(%s)", Objects.requireNonNull(failure.getError().orElse(null)).getMessage()),
                failure.toString());
        assertEquals(Objects.hash("success"), success.hashCode());
        assertEquals(Objects.hash(Objects.requireNonNull(failure.getError().orElse(null))),
                failure.hashCode());
        assertNotEquals(success, failure);
        assertEquals(Result.success("success"), success);
        assertEquals(Result.failure(Objects.requireNonNull(failure.getError().orElse(null))), failure);
    }
}
