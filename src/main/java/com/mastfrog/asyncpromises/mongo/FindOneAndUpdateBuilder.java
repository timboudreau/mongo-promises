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
import com.mongodb.client.model.ReturnDocument;
import java.util.concurrent.TimeUnit;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
public interface FindOneAndUpdateBuilder<T, R> {

    AsyncPromise<R, T> build();

    FindOneAndUpdateBuilder<T, R> maxTime(long maxTime, TimeUnit timeUnit);

    FindOneAndUpdateBuilder<T, R> projection(Bson projection);

    FindOneAndUpdateBuilder<T, R> returnDocument(ReturnDocument returnDocument);

    FindOneAndUpdateBuilder<T, R> sort(Bson sort);

    FindOneAndUpdateBuilder<T, R> upsert(boolean upsert);
    FindOneAndUpdateBuilder<T, R> upsert();
    
    ProjectionBuilder<FindOneAndUpdateBuilder<T,R>> projection();
    
}
