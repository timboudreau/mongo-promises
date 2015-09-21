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
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.UpdateOptions;
import org.bson.conversions.Bson;

/**
 * Builder for bulk writes
 *
 * @author Tim Boudreau
 * @param <T> The collection's document type
 */
public interface BulkWriteBuilder<T> {
    /**
     * Build the promise.
     * 
     * @return A promise
     */
    AsyncPromise<Void,BulkWriteResult> build() ;
    /**
     * Delete many documents matching the filter.
     *
     * @param filter The query
     * @return this
     */
    BulkWriteBuilder<T> deleteMany(Bson filter);

    /**
     * Delete many using a query builder
     *
     * @return this
     */
    QueryBuilder<T, BulkWriteBuilder<T>> deleteMany();

    /**
     * Delete one.
     *
     * @param filter The query
     * @return this
     */
    BulkWriteBuilder<T> deleteOne(Bson filter);

    /**
     * Delete one using a query builder
     *
     * @return this
     */
    QueryBuilder<T, BulkWriteBuilder<T>> deleteOne();

    /**
     * Insert a document
     *
     * @param doc The document
     * @return this
     */
    BulkWriteBuilder<T> insert(T doc);

    /**
     * If true, operations stop if one fails.
     *
     * @param ordered Whether or not operations should stop on failure
     * @return this
     */
    BulkWriteBuilder<T> ordered(boolean ordered);

    /**
     * Operations should cease if any one operation fails.
     *
     * @return this
     */
    BulkWriteBuilder<T> ordered();

    /**
     * Operations should continue in the event of failure.
     *
     * @return this
     */
    BulkWriteBuilder<T> unordered();

    /**
     * Add an update, using a builder.
     * 
     * @return A query builder
     */
    QueryBuilder<T, ModificationBuilder<UpdateBuilder<BulkWriteBuilder<T>>>> update();

    /**
     * Update many.
     * 
     * @param filter The query
     * @param update The update
     * @param options The options
     * @return this
     */
    BulkWriteBuilder<T> updateMany(Bson filter, Bson update, UpdateOptions options);

    /**
     * Update one.
     * 
     * @param filter The query
     * @param update The update
     * @param options The options
     * @return this
     */
    BulkWriteBuilder<T> updateOne(Bson filter, Bson update, UpdateOptions options);
}
