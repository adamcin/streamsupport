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

import java.lang.reflect.Constructor;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NothingTest {
    @Test
    public void testVoidToNothing1() throws Exception {
        final String sentinel = "sentinel";
        final CompletableFuture<String> latch = new CompletableFuture<>();
        final Consumer<String> consumer = latch::complete;
        final Nothing nothing = Nothing.voidToNothing1(consumer).apply(sentinel);
        assertTrue(latch.isDone(), "future should be done: " + latch);
        assertSame(Nothing.instance, nothing, "nothing should be returned");
        assertSame(sentinel, latch.get(), "latched should be same as input");
    }

    @Test
    public void testVoidToNothing2() throws Exception {
        final String sentinel = "sentinel";
        final CompletableFuture<String> latch = new CompletableFuture<>();
        final BiConsumer<String, Boolean> consumer = (string, test) -> latch.complete(string);
        final Nothing nothing = Nothing.voidToNothing2(consumer).apply(sentinel, true);
        assertTrue(latch.isDone(), "future should be done: " + latch);
        assertSame(Nothing.instance, nothing, "nothing should be returned");
        assertSame(sentinel, latch.get(), "latched should be same as input");
    }

    @Test
    public void testCombine() {
        assertSame(Nothing.instance, Nothing.instance.combine(Nothing.instance), "combine Nothing to produce Nothing");
    }

    @Test
    void equalsHashCodeToString() throws Exception {
        Nothing nothing = Nothing.instance;
        assertEquals(Nothing.NAME, nothing.toString());
        assertEquals(Nothing.NAME.hashCode(), nothing.hashCode());
        Constructor<Nothing> factory = Nothing.class.getDeclaredConstructor();
        factory.setAccessible(true);
        Nothing magicNothing = factory.newInstance();
        assertEquals(nothing, magicNothing);

    }
}
