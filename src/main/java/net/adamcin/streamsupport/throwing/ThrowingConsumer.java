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

package net.adamcin.streamsupport.throwing;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Inferrable type for {@link java.util.function.Consumer}s that throw.
 *
 * @param <T> input type
 */
@ConsumerType
@FunctionalInterface
public interface ThrowingConsumer<T> {
    void tryAccept(T input) throws Exception;
}
