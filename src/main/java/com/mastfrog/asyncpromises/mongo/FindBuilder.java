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

import com.mastfrog.asyncpromises.AsyncPromise;
import com.mongodb.CursorType;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bson.conversions.Bson;

/**
 * Configure the cursor properties for a find operation.
 *
 * @param <T> The type of the collection's elements
 */
public interface FindBuilder<T> {

    /**
     * Set the cursor's batch size
     *
     * @param size The batch size
     * @return this
     */
    public FindBuilder<T> withBatchSize(int size);

    /**
     * Set the projection for the query
     *
     * @param projection The projection
     * @return this
     */
    public FindBuilder<T> withProjection(Bson projection);

    /**
     * Set the cursor type for the query
     *
     * @param cursorType The cursor type
     * @return this
     */
    public FindBuilder<T> withCursorType(CursorType cursorType);

    /**
     * Set the limit for the cursor
     *
     * @param amount The limit - must be a positive integer
     * @return this
     */
    public FindBuilder<T> limit(int amount);

    /**
     * Set the filter for the query
     *
     * @param bson The filter
     * @return this
     */
    public FindBuilder<T> filter(Bson bson);

    /**
     * Set the modifiers for performing the query
     *
     * @param bson The modifiers
     * @return this
     */
    public FindBuilder<T> modifiers(Bson bson);

    /**
     * Set the sort for the query
     *
     * @param bson The sort parameters
     * @return this
     */
    public FindBuilder<T> sort(Bson bson);

    /**
     * Simplification of <code>sort()</code> for the common case of sorting
     * on a single property.
     *
     * @param name The name of the property
     * @return this
     */
    public FindBuilder<T> ascendingSortBy(String name);

    /**
     * Simplification of <code>sort()</code> for the common case of sorting
     * on a single property.
     *
     * @param name The name of the property
     * @return this
     */
    public FindBuilder<T> descendingSortBy(String name);

    /**
     * Set the maximum time before the query times out.
     *
     * @param amount The amount
     * @param units The units of the amount
     * @return this
     */
    public FindBuilder<T> maxTime(long amount, TimeUnit units);

    /**
     * Create FindBuilder with this one's parameters, which will use an
     * alternate element type.
     *
     * @param <R> The type
     * @param type The type
     * @return A new FindBuilder with this one's state
     */
    public <R> FindBuilder<R> withResultType(Class<R> type);

    /**
     * Execute the find, notifying the passed FindReceiver once for each
     * batch of results.
     *
     * @param receiver The callback which will be passed each batch of
     * results
     * @return A promise which will be notified when all results have been
     * received
     */
    public AsyncPromise<Bson, Void> find(FindReceiver<List<T>> receiver);

    /**
     * Execute the find, returning only the first collection element found.
     *
     * @return A promise
     */
    public AsyncPromise<Bson, T> findOne();
    
}
