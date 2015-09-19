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
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;

/**
 * Builder to make it easy to perform updates.
 *
 * @author Tim Boudreau
 */
public interface UpdateBuilder<T> {

    /**
     * Set whether or not this is an upsert.
     * @param upsert True if an upsert, false if not
     * @return this
     */
    UpdateBuilder<T> upsert(boolean upsert);

    /**
     * Set this to be an upsert.
     * @return this
     */
    UpdateBuilder<T> upsert();

    /**
     * Get a ModificationBuilder which makes it easy to correctly set
     * up MongoDB modification options.
     * 
     * @return this
     */
    ModificationBuilder<T> modification();

    /**
     * Manually set the modification to make as BSON.
     * @param update The update
     * @return this
     */
    UpdateBuilder<T> modification(Bson update);

    /**
     * Create a promise to update all records that match the
     * query you pass to the returned promise.
     * 
     * @return The promise
     */
    AsyncPromise<Bson, UpdateResult> updateMany();

    /**
     * Create a promise to update all records that match the
     * query you pass to the returned promise.
     * 
     * @return The promise
     */
    AsyncPromise<Bson, UpdateResult> updateOne();
}
