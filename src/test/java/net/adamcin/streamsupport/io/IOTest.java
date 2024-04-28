/*
 * Copyright 2019 Mark Adamcin
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOTest {

    @Test
    void map() {
        IO<Integer> io = IO.unit(42);
        assertEquals( "42", io.map(Object::toString).get(), "map works");
    }

    @Test
    void flatMap() {
        IO<Integer> io = IO.unit(42);
        assertEquals( "42", io.flatMap(value -> IO.unit(value.toString())).get(), "map works");
    }

    @Test
    void add() {
        IO<Integer> io = IO.unit(42).add(IO.unit(443));
        assertEquals(443, io.get());
    }
}
