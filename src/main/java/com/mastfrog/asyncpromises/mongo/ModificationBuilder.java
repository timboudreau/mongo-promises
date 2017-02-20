/*
 * The MIT License
 *
 * Copyright 2015 Tim Boudreau.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.asyncpromises.mongo;

/**
 * Creates a Document describing how to modify documents in an update operation,
 * and passes it to the originating UpdateBuilder
 *
 * @author Tim Boudreau
 */
public interface ModificationBuilder<T> {

    /**
     * Build this modification and return the originating UpdateBuilder.
     *
     * @return The Updatebuilder that created this ModificationBuilder.
     */
    T build();

    /**
     * Add a field to unset.
     *
     * @param name The field name
     * @return this
     */
    ModificationBuilder<T> unset(String name);

    /**
     * Add a field the value of which should be set.
     *
     * @param name The name
     * @param value The value
     * @return this
     */
    ModificationBuilder<T> set(String name, Object value);

    /**
     * Add a field to decrement.
     *
     * @param name The name of the field
     * @return this
     */
    ModificationBuilder<T> decrement(String name);

    /**
     * Add a field to increment.
     *
     * @param name The name of the field
     * @return this
     */
    ModificationBuilder<T> increment(String name);

    /**
     * Add a field to increment by a specific amount.
     *
     * @param name The name of the field
     * @param amount The amount to increment
     * @return this
     */
    ModificationBuilder<T> increment(String name, int amount);

    /**
     * Add a field to increment by a specific amount.
     *
     * @param name The name of the field
     * @param amount The amount to increment
     * @return this
     */
    ModificationBuilder<T> increment(String name, long amount);

    /**
     * Add an object to push into an array with the specificed name.
     *
     * @param name The name
     * @param val The value
     * @return this
     */
    ModificationBuilder<T> push(String name, Object... val);

    /**
     * Specify an object to remove from an array with the specified name.
     *
     * @param name The name
     * @param val The value
     * @return this
     */
    ModificationBuilder<T> pull(String name, Object val);

    /**
     * Specify fields to rename.
     *
     * @param old The old name
     * @param nue The new name (may not be the same as the old)
     * @return this
     */
    ModificationBuilder<T> rename(String old, String nue);

    /**
     * Add a property that should be set in the case that the update is an
     * upsert.
     *
     * @param key The name
     * @param value The value
     * @return this
     */
    ModificationBuilder<T> setOnInsert(String key, Object value);

    /**
     * Add a field to increment by a specific amount.
     *
     * @param name The name of the field
     * @param amount The amount to increment
     * @return this
     */
    ModificationBuilder<T> decrement(String name, int amount);

    /**
     * Add a field to increment by a specific amount.
     *
     * @param name The name of the field
     * @param amount The amount to increment
     * @return this
     */
    ModificationBuilder<T> decrement(String name, long amount);

    /**
     * Determine if this builder contains no modifications.
     *
     * @return true if it is empty
     */
    boolean isEmpty();
}
