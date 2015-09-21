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
 * Builder to make it easy to perform updates.
 *
 * @author Tim Boudreau
 */
public interface ReplaceBuilder<T,R> {

    /**
     * Set whether or not this is an upsert.
     *
     * @param upsert True if an upsert, false if not
     * @return this
     */
    ReplaceBuilder<T,R> upsert(boolean upsert);

    /**
     * Set this to be an upsert.
     *
     * @return this
     */
    ReplaceBuilder<T,R> upsert();

    /**
     * Create a promise to replace a record that matches query you pass to the
     * returned promise.
     *
     * @param replacement The replacement object
     * @return The promise
     */
    T replaceWith(R replacement);
}
