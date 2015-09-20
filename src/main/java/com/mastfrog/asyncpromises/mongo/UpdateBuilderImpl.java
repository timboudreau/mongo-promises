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
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
final class UpdateBuilderImpl<T> implements UpdateBuilder<T> {

    UpdateOptions opts = new UpdateOptions();
    private Bson update;
    private final Factory<T> factory;

    UpdateBuilderImpl(Factory<T> factory) {
        this.factory = factory;
    }
    
    static UpdateBuilderImpl<Bson> create(CollectionPromises<?> promises) {
        return new UpdateBuilderImpl<>(new StdFactory(promises));
    }

    interface Factory<T> {

        public AsyncPromise<T, UpdateResult> updateMany(UpdateBuilderImpl<?> builder);

        public AsyncPromise<T, UpdateResult> updateOne(UpdateBuilderImpl<?> builder);
    }

    static final class StdFactory implements Factory<Bson> {

        private final CollectionPromises<?> promises;

        public StdFactory(CollectionPromises<?> promises) {
            this.promises = promises;
        }

        @Override
        public AsyncPromise<Bson, UpdateResult> updateMany(UpdateBuilderImpl<?> builder) {
            if (builder.update == null) {
                throw new IllegalArgumentException("Modification not set");
            }
            return promises.updateMany(builder.update, builder.opts);
        }

        @Override
        public AsyncPromise<Bson, UpdateResult> updateOne(UpdateBuilderImpl<?> builder) {
            if (builder.update == null) {
                throw new IllegalArgumentException("Modification not set");
            }
            return promises.updateOne(builder.update);
        }
    }

    @Override
    public UpdateBuilderImpl<T> upsert(boolean upsert) {
        opts.upsert(upsert);
        return this;
    }

    @Override
    public UpdateBuilderImpl<T> upsert() {
        opts.upsert(true);
        return this;
    }

    @Override
    public ModificationBuilder<UpdateBuilder<T>> modification() {
        return ModificationBuilderImpl.create(this);
    }

    @Override
    public UpdateBuilderImpl<T> modification(Bson update) {
        this.update = update;
        return this;
    }

    @Override
    public AsyncPromise<T, UpdateResult> updateMany() {
        return factory.updateMany(this);
    }

    @Override
    public AsyncPromise<T, UpdateResult> updateOne() {
        return factory.updateOne(this);
    }
}
