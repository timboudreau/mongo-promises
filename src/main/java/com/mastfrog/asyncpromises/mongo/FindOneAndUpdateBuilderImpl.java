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
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
final class FindOneAndUpdateBuilderImpl<T, R> implements FindOneAndUpdateBuilder<T, R> {

    final FindOneAndUpdateOptions opts = new FindOneAndUpdateOptions();
    private final Factory<T, R> factory;

    public FindOneAndUpdateBuilderImpl(Factory<T, R> factory) {
        this.factory = factory;
    }

    static <T> FindOneAndUpdateBuilderImpl<T, Bson> create(CollectionPromises<T> promises, Bson modification) {
        return new FindOneAndUpdateBuilderImpl<>(new StandardFactory<>(promises, modification));
    }

    @Override
    public ProjectionBuilder<FindOneAndUpdateBuilder<T, R>> projection() {
        return new ProjectionBuilderImpl<>(new ProjectionBuilderImpl.Factory<FindOneAndUpdateBuilder<T, R>>() {

            @Override
            public FindOneAndUpdateBuilder<T, R> build(Document projection) {
                FindOneAndUpdateBuilderImpl.this.projection(projection);
                return FindOneAndUpdateBuilderImpl.this;
            }
        });
    }

    @Override
    public FindOneAndUpdateBuilder<T, R> upsert() {
        return upsert(true);
    }

    interface Factory<T, R> {

        AsyncPromise<R, T> build(FindOneAndUpdateBuilderImpl<T, R> builder);
    }

    static class StandardFactory<T> implements Factory<T, Bson> {

        private final CollectionPromises<T> promises;
        private final Bson modification;

        public StandardFactory(CollectionPromises<T> promises, Bson modification) {
            this.promises = promises;
            this.modification = modification;
        }

        @Override
        public AsyncPromise<Bson, T> build(FindOneAndUpdateBuilderImpl<T, Bson> builder) {
            return promises.findOneAndUpdate(modification, builder.opts);
        }
    }

    @Override
    public FindOneAndUpdateBuilderImpl<T, R> projection(Bson projection) {
        opts.projection(projection);
        return this;
    }

    @Override
    public FindOneAndUpdateBuilderImpl<T, R> sort(Bson sort) {
        opts.sort(sort);
        return this;
    }

    @Override
    public FindOneAndUpdateBuilderImpl<T, R> upsert(boolean upsert) {
        opts.upsert(upsert);
        return this;
    }

    @Override
    public FindOneAndUpdateBuilderImpl<T, R> returnDocument(ReturnDocument returnDocument) {
        opts.returnDocument(returnDocument);
        return this;
    }

    @Override
    public FindOneAndUpdateBuilderImpl<T, R> maxTime(long maxTime, TimeUnit timeUnit) {
        opts.maxTime(maxTime, timeUnit);
        return this;
    }

    @Override
    public AsyncPromise<R, T> build() {
        return factory.build(this);
    }
}
