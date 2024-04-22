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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.adamcin.streamsupport.Fun.compose0;
import static net.adamcin.streamsupport.Fun.compose1;
import static net.adamcin.streamsupport.Fun.compose2;
import static net.adamcin.streamsupport.Fun.composeTest1;
import static net.adamcin.streamsupport.Fun.composeTest2;
import static net.adamcin.streamsupport.Fun.composeTry0;
import static net.adamcin.streamsupport.Fun.composeTry1;
import static net.adamcin.streamsupport.Fun.composeTry2;
import static net.adamcin.streamsupport.Fun.entriesToMap;
import static net.adamcin.streamsupport.Fun.entriesToMapOfType;
import static net.adamcin.streamsupport.Fun.entryTee;
import static net.adamcin.streamsupport.Fun.inSet;
import static net.adamcin.streamsupport.Fun.infer0;
import static net.adamcin.streamsupport.Fun.infer1;
import static net.adamcin.streamsupport.Fun.infer2;
import static net.adamcin.streamsupport.Fun.inferTest1;
import static net.adamcin.streamsupport.Fun.inferTest2;
import static net.adamcin.streamsupport.Fun.isKeyIn;
import static net.adamcin.streamsupport.Fun.isValueIn;
import static net.adamcin.streamsupport.Fun.keepFirstMerger;
import static net.adamcin.streamsupport.Fun.keepLastMerger;
import static net.adamcin.streamsupport.Fun.mapEntry;
import static net.adamcin.streamsupport.Fun.mapKey;
import static net.adamcin.streamsupport.Fun.mapValue;
import static net.adamcin.streamsupport.Fun.onEntry;
import static net.adamcin.streamsupport.Fun.onKey;
import static net.adamcin.streamsupport.Fun.onValue;
import static net.adamcin.streamsupport.Fun.result0;
import static net.adamcin.streamsupport.Fun.result1;
import static net.adamcin.streamsupport.Fun.result2;
import static net.adamcin.streamsupport.Fun.resultNothing1;
import static net.adamcin.streamsupport.Fun.resultNothing2;
import static net.adamcin.streamsupport.Fun.testEntry;
import static net.adamcin.streamsupport.Fun.testKey;
import static net.adamcin.streamsupport.Fun.testOrDefault1;
import static net.adamcin.streamsupport.Fun.testOrDefault2;
import static net.adamcin.streamsupport.Fun.testValue;
import static net.adamcin.streamsupport.Fun.throwingMerger;
import static net.adamcin.streamsupport.Fun.throwingVoidToNothing1;
import static net.adamcin.streamsupport.Fun.throwingVoidToNothing2;
import static net.adamcin.streamsupport.Fun.toEntry;
import static net.adamcin.streamsupport.Fun.toVoid1;
import static net.adamcin.streamsupport.Fun.toVoid2;
import static net.adamcin.streamsupport.Fun.tryOrDefault0;
import static net.adamcin.streamsupport.Fun.tryOrDefault1;
import static net.adamcin.streamsupport.Fun.tryOrDefault2;
import static net.adamcin.streamsupport.Fun.tryOrOptional0;
import static net.adamcin.streamsupport.Fun.tryOrOptional1;
import static net.adamcin.streamsupport.Fun.tryOrOptional2;
import static net.adamcin.streamsupport.Fun.tryOrVoid1;
import static net.adamcin.streamsupport.Fun.tryOrVoid2;
import static net.adamcin.streamsupport.Fun.uncheck0;
import static net.adamcin.streamsupport.Fun.uncheck1;
import static net.adamcin.streamsupport.Fun.uncheck2;
import static net.adamcin.streamsupport.Fun.uncheckTest1;
import static net.adamcin.streamsupport.Fun.uncheckTest2;
import static net.adamcin.streamsupport.Fun.uncheckVoid1;
import static net.adamcin.streamsupport.Fun.uncheckVoid2;
import static net.adamcin.streamsupport.Fun.zipKeysWithValueFunc;
import static net.adamcin.streamsupport.Fun.zipValuesWithKeyFunc;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FunTest {
    final String sentinel = "sentinel";

    @Test
    public void testStreamIt() {
        Stream<String> strings = Fun.streamIt(sentinel);
        final Optional<String> testHead = strings.findFirst();
        assertTrue(testHead.isPresent());
        assertSame(sentinel, testHead.get());
    }

    @Test
    public void testStreamOpt() {
        final Optional<String> sentinel = Optional.of("sentinel");
        Stream<String> strings = Fun.streamOpt(sentinel);
        final Optional<String> testHead = strings.findFirst();
        assertTrue(testHead.isPresent());
        assertSame(sentinel.get(), testHead.get());
    }

    @Test
    public void testTee() throws Exception {
        final CompletableFuture<String> latch = new CompletableFuture<>();
        final Consumer<String> consumer = latch::complete;
        final String value = Fun.tee(consumer).apply(sentinel);
        assertTrue(latch.isDone());
        assertSame(sentinel, value);
        assertSame(sentinel, latch.get());
    }


    @Test
    public void testConstantly1() {
        final Supplier<String> supplier = () -> sentinel;
        final Function<Boolean, String> function = Fun.constantly1(supplier);
        final String[] results = Stream.of(Boolean.TRUE, Boolean.FALSE).map(function).toArray(String[]::new);
        assertArrayEquals(new String[]{sentinel, sentinel}, results);
    }

    @Test
    public void testConstantly2() {
        final Supplier<String> supplier = () -> sentinel;
        final BiFunction<Boolean, Boolean, String> function = Fun.constantly2(supplier);
        final String[] results = Stream.of(
                Fun.toEntry(Boolean.TRUE, Boolean.FALSE),
                Fun.toEntry(Boolean.FALSE, Boolean.TRUE)
        ).map(mapEntry(function)).toArray(String[]::new);
        assertArrayEquals(new String[]{sentinel, sentinel}, results);
    }

    @Test
    public void testCompose() {
        assertEquals("SENTINELSENTINEL",
                compose1(sentinel::concat, String::toUpperCase).apply(sentinel));
    }

    @Test
    public void testCompose0() {
        assertEquals("SENTINEL",
                compose0(() -> sentinel, String::toUpperCase).get());
    }

    @Test
    public void testCompose2() {
        assertEquals("sentineltrue",
                compose2((String string, Boolean bool) -> string + bool, String::toLowerCase)
                        .apply(sentinel, true));
    }

    @Test
    public void testComposeTest() {
        final Predicate<String> predicate = composeTest1(sentinel::concat,
                string -> string.length() > sentinel.length());
        assertFalse(predicate.test(""));
        assertTrue(predicate.test("more"));
    }

    @Test
    public void testComposeTest2() {
        final Function<String, String> substring4 = string -> string.substring(0, 4);
        final Function<String, String> substring6 = string -> string.substring(0, 6);
        final BiPredicate<String, String> truePredicate = composeTest2(substring6, substring4, String::startsWith);
        final BiPredicate<String, String> falsePredicate = composeTest2(substring4, substring6, String::startsWith);
        assertTrue(truePredicate.test(sentinel, sentinel));
        assertFalse(falsePredicate.test(sentinel, sentinel));
    }

    @Test
    public void testComposeTest2BiFunction() {
        final BiFunction<String, Integer, String> chomp = (string, length) -> string.substring(0, length);
        final BiPredicate<String, Integer> sentinelStartsWith = composeTest2(chomp, sentinel::startsWith);
        assertTrue(sentinelStartsWith.test("senile", 3));
        assertFalse(sentinelStartsWith.test("straighten", 2));
    }

    @Test
    public void testToVoid1() {
        final Function<String, String> func = String::toUpperCase;
        final Consumer<String> cons = toVoid1(func);
        cons.accept(sentinel);
    }

    @Test
    public void testToVoid2() {
        final BiFunction<String, String, String> func = String::concat;
        final BiConsumer<String, String> cons = toVoid2(func);
        cons.accept(sentinel, sentinel);
    }

    @Test
    public void testInfer1() {
        final Function<String, String> func = String::toUpperCase;
        final Function<String, String> inferred = infer1(String::toUpperCase);
        assertEquals(func.apply(sentinel), inferred.apply(sentinel));
    }

    @Test
    public void testInfer2() {
        final BiFunction<String, String, String> func = String::concat;
        final BiFunction<String, String, String> inferred = infer2(String::concat);
        assertEquals(func.apply(sentinel, sentinel), inferred.apply(sentinel, sentinel));
    }

    private class TestInfer0Provider {
        String getValue() {
            return sentinel;
        }
    }

    @Test
    public void testInfer0() {
        final TestInfer0Provider provider = new TestInfer0Provider();
        final Supplier<String> supplier = infer0(provider::getValue);
        assertEquals(sentinel, supplier.get());
    }

    @Test
    public void testInferTest1() {
        final Predicate<String> test = inferTest1(String::isEmpty);
        assertTrue(test.test(""));
        assertFalse(test.test("abcde"));
    }

    @Test
    public void testInferTest2() {
        final BiPredicate<String, String> test = inferTest2(String::equalsIgnoreCase);
        assertTrue(test.test("a", "A"));
        assertFalse(test.test("b", "A"));
    }

    @Test
    public void testThrowingVoidToNothing1() throws Exception {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingConsumer<Integer> consumer = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(value);
        };
        assertSame(Nothing.instance, throwingVoidToNothing1(consumer).tryApply(2));
        assertEquals(2, latch.get());
    }

    @Test
    public void testThrowingVoidToNothing2() throws Exception {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingBiConsumer<String, Integer> consumer = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(newValue);
        };

        assertSame(Nothing.instance, throwingVoidToNothing2(consumer).tryApply("", 2));
        assertEquals(2, latch.get());
    }

    @Test
    public void testEntryTee() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Map.Entry<String, Boolean> entry = toEntry("foo", false);
        final boolean boolResult = Stream.of(entry)
                .map(entryTee((key, value) -> future.complete(key)))
                .map(Map.Entry::getValue)
                .findFirst().orElse(true);
        assertFalse(boolResult);
        assertEquals("foo", future.getNow(""));
    }

    @Test
    public void testZipKeysWithValueFunc() {
        final Map<String, Integer> lengths = Stream.of("foo", "bar", "acme", "baboon")
                .collect(Collectors.toMap(Function.identity(), String::length));
        final Map<String, Integer> collected = Stream.of("foo")
                .map(zipKeysWithValueFunc(lengths::get))
                .collect(Fun.entriesToMap());
        assertEquals(Collections.singletonMap("foo", 3), collected);
    }

    @Test
    public void testZipValuesWithKeyFunc() {
        final Map<Integer, String> lengths = Stream.of("bar", "acme", "baboon")
                .collect(Collectors.toMap(String::length, Function.identity()));
        final Map<String, Integer> collected = Stream.of(4)
                .map(zipValuesWithKeyFunc(lengths::get))
                .collect(Fun.entriesToMap());
        assertEquals(Collections.singletonMap("acme", 4), collected);
    }

    @Test
    public void testToEntry() {
        final Map.Entry<String, String> entry = toEntry("foo", "bar");
        assertEquals("foo", entry.getKey());
        assertEquals("bar", entry.getValue());
    }

    @Test
    public void testEntriesToMap() {
        final Map<String, Integer> parallelConstruction = new HashMap<>();
        parallelConstruction.put("one", 1);
        parallelConstruction.put("two", 2);
        parallelConstruction.put("three", 3);
        final Map<String, Integer> collected = Stream.of(toEntry("one", 1), toEntry("two", 2), toEntry("three", 3))
                .collect(entriesToMap());
        assertEquals(parallelConstruction, collected);
        assertTrue(collected instanceof LinkedHashMap);
    }

    @Test
    public void testEntriesToMapOfType() {
        final Map<String, Integer> collected = Stream.of(
                        toEntry("one", 1),
                        toEntry("one", 2),
                        toEntry("three", 3))
                .collect(entriesToMapOfType(HashMap::new));
        assertEquals(Integer.valueOf(2), collected.get("one"));
        assertInstanceOf(HashMap.class, collected);
        assertFalse(collected instanceof LinkedHashMap);
    }

    @Test
    public void testEntriesToMapOfTypeWithMerger() {
        final Map<String, Integer> collected = Stream.of(
                        toEntry("one", 1),
                        toEntry("one", 2),
                        toEntry("three", 3))
                .collect(entriesToMapOfType(HashMap::new, keepLastMerger()));
        assertEquals(Integer.valueOf(2), collected.get("one"));
        assertInstanceOf(HashMap.class, collected);
        assertFalse(collected instanceof LinkedHashMap);
        final Map<String, Integer> collectedFirst = Stream.of(
                        toEntry("one", 1),
                        toEntry("one", 2),
                        toEntry("three", 3))
                .collect(entriesToMapOfType(TreeMap::new, keepFirstMerger()));
        assertEquals(Integer.valueOf(1), collectedFirst.get("one"));
        assertTrue(collectedFirst instanceof TreeMap);
    }

    @Test
    public void testEntriesToMapOfTypeAndThrows() {
        assertThrows(IllegalStateException.class, () -> {
            final Map<String, Integer> collected = Stream.of(
                            toEntry("one", 1),
                            toEntry("one", 2),
                            toEntry("three", 3))
                    .collect(entriesToMapOfType(TreeMap::new, throwingMerger()));
        });

    }

    @Test
    public void testKeepFirstMerger() {
        final Map<String, Integer> collected = Stream.of(
                        toEntry("one", 1),
                        toEntry("one", 2),
                        toEntry("three", 3))
                .collect(entriesToMap(keepFirstMerger()));
        assertEquals(Integer.valueOf(1), collected.get("one"));
    }

    @Test
    public void testKeepLastMerger() {
        final Map<String, Integer> collected = Stream.of(
                        toEntry("one", 1),
                        toEntry("one", 2),
                        toEntry("three", 3))
                .collect(entriesToMap(keepLastMerger()));
        assertEquals(Integer.valueOf(2), collected.get("one"));
    }

    @Test
    public void testThrowingMerger() {
        assertThrows(IllegalStateException.class, () -> {
            final Map<String, Integer> collected = Stream.of(
                            toEntry("one", 1),
                            toEntry("one", 2),
                            toEntry("three", 3))
                    .collect(entriesToMap(throwingMerger()));
        });
    }

    @Test
    public void testEntriesToMapWithMerge() {
        final Map<String, Integer> parallelConstruction = new HashMap<>();
        parallelConstruction.put("one", 4);
        parallelConstruction.put("two", 2);
        final Map<String, Integer> collected = Stream.of(toEntry("one", 1), toEntry("two", 2), toEntry("one", 3))
                .collect(entriesToMap((int1, int2) -> int1 + int2));
        assertEquals(parallelConstruction, collected);
    }

    @Test
    public void testMapEntry() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        List<String> collected = refMap.entrySet().stream()
                .map(mapEntry((string, number) -> string + "_" + number)).collect(Collectors.toList());
        assertEquals(Arrays.asList("one_1", "two_2", "three_3"), collected);
    }

    @Test
    public void testMapKey() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        Set<String> collected = refMap.entrySet().stream()
                .map(mapKey(string -> string + "_" + string)).collect(entriesToMap()).keySet();
        assertEquals(new LinkedHashSet<>(Arrays.asList("one_one", "two_two", "three_three")), collected);
    }

    @Test
    public void testMapKeyBiFunction() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        Set<String> collected = refMap.entrySet().stream()
                .map(mapKey((string, number) -> string + "_" + number)).collect(entriesToMap()).keySet();
        assertEquals(new LinkedHashSet<>(Arrays.asList("one_1", "two_2", "three_3")), collected);
    }

    @Test
    public void testMapValue() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        List<Integer> collected = refMap.entrySet().stream()
                .map(mapValue(number -> number * 2))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(2, 4, 6), collected);
    }

    @Test
    public void testMapValueBiFunction() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        List<Integer> collected = refMap.entrySet().stream()
                .map(mapValue((string, number) -> string.startsWith("t") ? number * 2 : number))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(1, 4, 6), collected);
    }

    @Test
    public void testOnEntry() {
        final AtomicInteger latch = new AtomicInteger(0);
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        refMap.entrySet().forEach(onEntry((string, number) -> latch.addAndGet(number)));
        assertEquals(6, latch.get());
    }

    @Test
    public void testOnKey() {
        final AtomicReference<String> latch = new AtomicReference<>("");
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        refMap.entrySet().forEach(onKey(string -> latch.accumulateAndGet(string, String::concat)));
        assertTrue(latch.get().contains("one"));
        assertTrue(latch.get().contains("two"));
        assertTrue(latch.get().contains("three"));
    }

    @Test
    public void testOnValue() {
        final AtomicInteger latch = new AtomicInteger(0);
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        refMap.entrySet().forEach(onValue(latch::addAndGet));
        assertEquals(6, latch.get());
    }

    @Test
    public void testTestEntry() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        Map<String, Integer> collected = refMap.entrySet().stream()
                .filter(testEntry((string, number) -> number % 2 == 0))
                .collect(entriesToMap());
        assertEquals(Collections.singletonMap("two", 2), collected);
    }

    @Test
    public void testTestKey() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        Map<String, Integer> collected = refMap.entrySet().stream()
                .filter(testKey(string -> string.endsWith("o")))
                .collect(entriesToMap());
        assertEquals(Collections.singletonMap("two", 2), collected);
    }

    @Test
    public void testTestValue() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        Map<String, Integer> collected = refMap.entrySet().stream()
                .filter(testValue(number -> number % 2 == 0))
                .collect(entriesToMap());
        assertEquals(Collections.singletonMap("two", 2), collected);
    }

    @Test
    public void testInSet() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        Set<String> set = refMap.keySet();

        String[] filtered = Stream.of("one", "two", "tree", "four").filter(inSet(set).negate()).toArray(String[]::new);
        assertArrayEquals(new String[]{"tree", "four"}, filtered);
    }

    @Test
    public void testIsKeyIn() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);

        String[] filtered = Stream.of("one", "two", "tree", "four")
                .filter(isKeyIn(refMap).negate()).toArray(String[]::new);
        assertArrayEquals(new String[]{"tree", "four"}, filtered);
    }

    @Test
    public void testIsValueIn() {
        final Map<String, Integer> refMap = new LinkedHashMap<>();
        refMap.put("one", 1);
        refMap.put("two", 2);
        refMap.put("three", 3);
        Integer[] filtered = Stream.of(5, 1, 2, 3, 4)
                .filter(isValueIn(refMap).negate()).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{5, 4}, filtered);
    }

    @Test
    public void testComposeTry_noOnError() {
        final ThrowingFunction<String, Class<?>> classLoader = this.getClass().getClassLoader()::loadClass;
        final Function<String, Stream<Class<?>>> func =
                Fun.composeTry1(Stream::of, Stream::empty, classLoader, null);
        final String notARealClassName = "net.adamcin.streamsupport.NotARealClass";
        final Class<?>[] loadedClasses = Stream.of("java.lang.String", notARealClassName, "java.util.Map")
                .flatMap(func).toArray(Class<?>[]::new);
        assertArrayEquals(new Class<?>[]{String.class, Map.class}, loadedClasses);
    }

    @Test
    public void testComposeTry() {
        final ThrowingFunction<String, Class<?>> classLoader = this.getClass().getClassLoader()::loadClass;
        final Map<String, Exception> collectedErrors = new HashMap<>();
        final Function<String, Stream<Class<?>>> func =
                Fun.composeTry1(Stream::of, Stream::empty, classLoader, collectedErrors::put);
        final String notARealClassName = "net.adamcin.streamsupport.NotARealClass";
        final Class<?>[] loadedClasses = Stream.of("java.lang.String", notARealClassName, "java.util.Map")
                .flatMap(func).toArray(Class<?>[]::new);
        assertArrayEquals(new Class<?>[]{String.class, Map.class}, loadedClasses);
        assertTrue(collectedErrors.containsKey(notARealClassName));
        final Exception error = collectedErrors.get(notARealClassName);
        assertInstanceOf(ClassNotFoundException.class, error);
    }

    @Test
    public void testComposeTryResult() {
        final ThrowingFunction<String, Class<?>> classLoader = this.getClass().getClassLoader()::loadClass;
        final Map<String, Exception> collectedErrors = new HashMap<>();
        final Function<String, Result<Class<?>>> func = composeTry1(Result::success, Result::failure, classLoader);
        final String notARealClassName = "net.adamcin.streamsupport.NotARealClass";
        final Map<String, Result<Class<?>>> results = Stream
                .of("java.lang.String", notARealClassName, "java.util.Map")
                .map(zipKeysWithValueFunc(func)).collect(entriesToMap());
        final Class<?>[] loadedClasses = results.values().stream().flatMap(Result::stream).toArray(Class<?>[]::new);

        results.entrySet().stream()
                .filter(testValue(Result::isFailure))
                .flatMap(entry -> entry.getValue().findCause(ClassNotFoundException.class)
                        .map(cause -> Stream.of(toEntry(entry.getKey(), cause))).orElse(Stream.empty()))
                .forEach(onEntry(collectedErrors::put));
        assertArrayEquals(new Class<?>[]{String.class, Map.class}, loadedClasses);
        assertTrue(collectedErrors.containsKey(notARealClassName));
        final Exception error = collectedErrors.get(notARealClassName);
        assertInstanceOf(ClassNotFoundException.class, error);
    }

    @Test
    public void testComposeTry0() {
        final List<Exception> collectedErrors = new ArrayList<>();
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingSupplier<Integer> supplier = () -> {
            int newValue = latch.incrementAndGet();
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        Integer[] ints = Stream.generate(composeTry0(Stream::of, Stream::empty, supplier, collectedErrors::add))
                .limit(10).flatMap(Function.identity())
                .toArray(Integer[]::new);
        assertEquals(3, collectedErrors.size());

        latch.set(0);

        Integer[] ints2 = Stream.generate(composeTry0(Stream::of, Stream::empty, supplier, null))
                .limit(10).flatMap(Function.identity())
                .toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 4, 5, 7, 8, 10}, ints2);
    }

    @Test
    public void testComposeTry0Result() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingSupplier<Integer> supplier = () -> {
            int newValue = latch.incrementAndGet();
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        List<Result<Integer>> results = Stream.generate(composeTry0(Result::success, Result::<Integer>failure, supplier))
                .limit(10).collect(Collectors.toList());

        assertEquals(3, results.stream().filter(Result::isFailure).count());
    }

    @Test
    public void testComposeTry2() {
        final List<Exception> collectedErrors = new ArrayList<>();
        final ThrowingBiFunction<String, Integer, Integer> function = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        Integer[] ints = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .flatMap(mapEntry(composeTry2(Stream::of, Stream::empty, function,
                        (entry, error) -> collectedErrors.add(error))))
                .toArray(Integer[]::new);
        assertEquals(3, collectedErrors.size());
    }

    @Test
    public void testComposeTry2Result() {
        final ThrowingBiFunction<String, Integer, Integer> function = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        List<Result<Integer>> results = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .map(mapEntry(composeTry2(Result::success, Result::<Integer>failure, function)))
                .collect(Collectors.toList());
        assertEquals(3, results.stream().filter(Result::isFailure).count());
    }

    @Test
    public void testUncheck0() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingSupplier<Integer> supplier = () -> {
            int newValue = latch.incrementAndGet();
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        Integer[] ints = Stream.generate(uncheck0(supplier)).limit(2).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2}, ints);
    }

    @Test
    public void testUncheck0AndThrow() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingSupplier<Integer> supplier = () -> {
            int newValue = latch.incrementAndGet();
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        // should throw
        assertThrows(Fun.FunRuntimeException.class, () -> {
            Stream.generate(uncheck0(supplier)).limit(3).toArray(Integer[]::new);
        });
    }

    @Test
    public void testResult0() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingSupplier<Integer> supplier = () -> {
            int newValue = latch.incrementAndGet();
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        final List<Result<Integer>> results = Stream.generate(result0(supplier)).limit(10).collect(Collectors.toList());
        assertEquals(3, results.stream().filter(Result::isFailure).count());
        final Integer[] ints = results.stream().flatMap(Result::stream).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 4, 5, 7, 8, 10}, ints);
    }

    @Test
    public void testUncheck1() {
        final ThrowingFunction<Integer, Integer> function = newValue -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        Integer[] ints = Stream.generate(new AtomicInteger(0)::incrementAndGet)
                .map(uncheck1(function)).limit(2).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2}, ints);
    }

    @Test
    public void testUncheck1AndThrow() {
        final ThrowingFunction<Integer, Integer> function = newValue -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        // should throw
        assertThrows(Fun.FunRuntimeException.class, () -> {
            Stream.generate(new AtomicInteger(0)::incrementAndGet)
                    .map(uncheck1(function)).limit(3).toArray(Integer[]::new);
        });
    }

    @Test
    public void testResult1() {
        final ThrowingFunction<Integer, Integer> function = newValue -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        final List<Result<Integer>> results = Stream.generate(new AtomicInteger(0)::incrementAndGet)
                .map(result1(function)).limit(10).collect(Collectors.toList());
        assertEquals(3, results.stream().filter(Result::isFailure).count());
        final Integer[] ints = results.stream().flatMap(Result::stream).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 4, 5, 7, 8, 10}, ints);
    }

    @Test
    public void testUncheck2() {
        final ThrowingBiFunction<String, Integer, Integer> function = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        Integer[] ints = Stream.generate(new AtomicInteger(0)::incrementAndGet)
                .map(zipValuesWithKeyFunc(String::valueOf)).map(mapEntry(uncheck2(function))).limit(2).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2}, ints);
    }

    @Test
    public void testUncheck2AndThrow() {
        final ThrowingBiFunction<String, Integer, Integer> function = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        // should throw
        assertThrows(Fun.FunRuntimeException.class, () -> {
            Stream.generate(new AtomicInteger(0)::incrementAndGet)
                    .map(zipValuesWithKeyFunc(String::valueOf)).map(mapEntry(uncheck2(function))).limit(3).toArray(Integer[]::new);
        });
    }

    @Test
    public void testResult2() {
        final ThrowingBiFunction<String, Integer, Integer> function = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        List<Result<Integer>> results = Stream.generate(new AtomicInteger(0)::incrementAndGet)
                .map(zipValuesWithKeyFunc(String::valueOf)).map(mapEntry(result2(function))).limit(10)
                .collect(Collectors.toList());
        assertEquals(3, results.stream().filter(Result::isFailure).count());
        final Integer[] ints = results.stream().flatMap(Result::stream).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 4, 5, 7, 8, 10}, ints);
    }

    @Test
    public void testUncheckTest1() {
        final ThrowingPredicate<Integer> predicate = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return value % 2 == 0;
        };

        Integer[] ints = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(2)
                .filter(uncheckTest1(predicate)).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{2}, ints);
    }

    @Test
    public void testUncheckTest1AndThrow() {
        final ThrowingPredicate<Integer> predicate = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return value % 2 == 0;
        };

        // should throw
        assertThrows(Fun.FunRuntimeException.class, () -> {
            Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(3)
                    .filter(uncheckTest1(predicate)).toArray(Integer[]::new);
        });
    }

    @Test
    public void testUncheckTest2() {
        final ThrowingBiPredicate<String, Integer> predicate = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue % 2 == 0;
        };

        Integer[] ints = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(2)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .filter(testEntry(uncheckTest2(predicate)))
                .map(Map.Entry::getValue)
                .toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{2}, ints);
    }

    @Test
    public void testUncheckTest2AndThrow() {
        final ThrowingBiPredicate<String, Integer> predicate = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue % 2 == 0;
        };

        // should throw
        assertThrows(Fun.FunRuntimeException.class, () -> {
            Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(3)
                    .map(zipValuesWithKeyFunc(String::valueOf))
                    .filter(testEntry(uncheckTest2(predicate)))
                    .map(Map.Entry::getValue)
                    .toArray(Integer[]::new);
        });
    }

    @Test
    public void testUncheckVoid1() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingConsumer<Integer> consumer = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(value);
        };

        Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(2)
                .forEach(uncheckVoid1(consumer));
        assertEquals(3, latch.get());
    }

    @Test
    public void testUncheckVoid1AndThrow() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingConsumer<Integer> consumer = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(value);
        };

        // should throw
        assertThrows(Fun.FunRuntimeException.class, () -> {
            Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(3)
                    .forEach(uncheckVoid1(consumer));
        });
    }

    @Test
    public void testResultNothing1() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingConsumer<Integer> consumer = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(value);
        };

        final List<Result<Nothing>> results = Stream.generate(new AtomicInteger(0)::incrementAndGet)
                .map(resultNothing1(consumer)).limit(10).collect(Collectors.toList());
        assertEquals(3, results.stream().filter(Result::isFailure).count());
    }

    @Test
    public void testUncheckVoid2() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingBiConsumer<String, Integer> consumer = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(newValue);
        };

        Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(2)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .forEach(onEntry(uncheckVoid2(consumer)));
        assertEquals(3, latch.get());
    }

    @Test
    public void testUncheckVoid2AndThrow() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingBiConsumer<String, Integer> consumer = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(newValue);
        };

        // should throw
        assertThrows(Fun.FunRuntimeException.class, () -> {
            Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(3)
                    .map(zipValuesWithKeyFunc(String::valueOf))
                    .forEach(onEntry(uncheckVoid2(consumer)));
        });
    }

    @Test
    public void testResultNothing2() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingBiConsumer<String, Integer> consumer = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(newValue);
        };

        List<Result<Nothing>> results = Stream.generate(new AtomicInteger(0)::incrementAndGet)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .map(mapEntry(resultNothing2(consumer))).limit(10)
                .collect(Collectors.toList());
        assertEquals(3, results.stream().filter(Result::isFailure).count());
    }

    @Test
    public void testTestOrDefault1() {
        final ThrowingPredicate<Integer> predicate = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return value % 2 == 0;
        };

        assertFalse(testOrDefault1(predicate, true).test(1));
        assertTrue(testOrDefault1(predicate, true).test(2));
        assertTrue(testOrDefault1(predicate, true).test(3));
    }

    @Test
    public void testTestOrDefault2() {
        final ThrowingBiPredicate<String, Integer> predicate = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue % 2 == 0;
        };

        assertFalse(testOrDefault2(predicate, true).test("", 1));
        assertTrue(testOrDefault2(predicate, true).test("", 2));
        assertTrue(testOrDefault2(predicate, true).test("", 3));
    }

    @Test
    public void testTryOrDefault0() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingSupplier<Integer> supplier = () -> {
            int newValue = latch.incrementAndGet();
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        final Integer[] ints = Stream.generate(tryOrDefault0(supplier, 0)).limit(10)
                .toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 0, 4, 5, 0, 7, 8, 0, 10}, ints);
    }

    @Test
    public void testTryOrDefault1() {
        final ThrowingFunction<Integer, Integer> function = newValue -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        final Integer[] ints = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .map(tryOrDefault1(function, 0))
                .toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 0, 4, 5, 0, 7, 8, 0, 10}, ints);
    }

    @Test
    public void testTryOrDefault2() {
        final ThrowingBiFunction<String, Integer, Integer> function = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };
        final Integer[] ints = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .map(mapEntry(tryOrDefault2(function, 0)))
                .toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 0, 4, 5, 0, 7, 8, 0, 10}, ints);
    }

    @Test
    public void testTryOrOptional0() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingSupplier<Integer> supplier = () -> {
            int newValue = latch.incrementAndGet();
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        final List<Optional<Integer>> results = Stream.generate(tryOrOptional0(supplier)).limit(10)
                .collect(Collectors.toList());
        Integer[] ints = results.stream().map(element -> element.orElse(0)).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 0, 4, 5, 0, 7, 8, 0, 10}, ints);
    }

    @Test
    public void testTryOrOptional1() {
        final ThrowingFunction<Integer, Integer> function = newValue -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };

        final List<Optional<Integer>> results = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .map(tryOrOptional1(function)).collect(Collectors.toList());
        Integer[] ints = results.stream().map(element -> element.orElse(0)).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 0, 4, 5, 0, 7, 8, 0, 10}, ints);
    }

    @Test
    public void testTryOrOptional2() {
        final ThrowingBiFunction<String, Integer, Integer> function = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            return newValue;
        };
        final List<Optional<Integer>> results = Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .map(mapEntry(tryOrOptional2(function))).collect(Collectors.toList());
        Integer[] ints = results.stream().map(element -> element.orElse(0)).toArray(Integer[]::new);
        assertArrayEquals(new Integer[]{1, 2, 0, 4, 5, 0, 7, 8, 0, 10}, ints);
    }

    @Test
    public void testTryOrVoid1() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingConsumer<Integer> consumer = value -> {
            if (value % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(value);
        };

        Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .forEach(tryOrVoid1(consumer));
        assertEquals(1 + 2 + 4 + 5 + 7 + 8 + 10, latch.get());
    }

    @Test
    public void testTryOrVoid2() {
        final AtomicInteger latch = new AtomicInteger(0);
        final ThrowingBiConsumer<String, Integer> consumer = (key, newValue) -> {
            if (newValue % 3 == 0) {
                throw new Exception("multiples of three are disallowed");
            }
            latch.addAndGet(newValue);
        };

        Stream.generate(new AtomicInteger(0)::incrementAndGet).limit(10)
                .map(zipValuesWithKeyFunc(String::valueOf))
                .forEach(onEntry(tryOrVoid2(consumer)));
        assertEquals(1 + 2 + 4 + 5 + 7 + 8 + 10, latch.get());
    }
}
