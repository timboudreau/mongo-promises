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
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import java.util.LinkedList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
final class BulkWriteBuilderImpl<T> implements BulkWriteBuilder<T> {

    private final BulkWriteOptions opts = new BulkWriteOptions();
    private final List<WriteModel<? extends T>> requests = new LinkedList<>();
    private final Factory<T> factory;

    BulkWriteBuilderImpl(Factory<T> factory) {
        this.factory = factory;
    }

    public AsyncPromise<Void, BulkWriteResult> build() {
        return factory.createPromise(this);
    }

    static <T> BulkWriteBuilderImpl<T> create(CollectionPromises<T> promises) {
        return new BulkWriteBuilderImpl<>(new StdFactory<>(promises));
    }

    @Override
    public BulkWriteBuilder<T> ordered() {
        return ordered(true);
    }

    @Override
    public BulkWriteBuilder<T> unordered() {
        return ordered(false);
    }

    interface Factory<T> {

        AsyncPromise<Void, BulkWriteResult> createPromise(BulkWriteBuilderImpl<T> builder);
    }

    static final class StdFactory<T> implements Factory<T> {

        private final CollectionPromises<T> promises;

        public StdFactory(CollectionPromises<T> promises) {
            this.promises = promises;
        }

        @Override
        public AsyncPromise<Void, BulkWriteResult> createPromise(BulkWriteBuilderImpl<T> builder) {
            return promises.bulkWrite(builder.requests, builder.opts);
        }
    }

    @Override
    public BulkWriteBuilder<T> ordered(boolean ordered) {
        opts.ordered(ordered);
        return this;
    }

    @Override
    public BulkWriteBuilder<T> insert(T doc) {
        requests.add(new InsertOneModel<>(doc));
        return this;
    }

    @Override
    public BulkWriteBuilder<T> deleteMany(Bson filter) {
        requests.add(new DeleteManyModel<T>(filter));
        return this;
    }

    @Override
    public BulkWriteBuilder<T> deleteOne(Bson filter) {
        requests.add(new DeleteOneModel<T>(filter));
        return this;
    }

    @Override
    public QueryBuilder<T, BulkWriteBuilder<T>> deleteMany() {
        return new QueryBuilderImpl<>(new QueryBuilderImpl.Factory<T, BulkWriteBuilder<T>>() {

            @Override
            public BulkWriteBuilderImpl<T> create(Document document) {
                requests.add(new DeleteManyModel<T>(document));
                return BulkWriteBuilderImpl.this;
            }
        });
    }

    @Override
    public QueryBuilder<T, BulkWriteBuilder<T>> deleteOne() {
        return new QueryBuilderImpl<>(new QueryBuilderImpl.Factory<T, BulkWriteBuilder<T>>() {

            @Override
            public BulkWriteBuilderImpl<T> create(Document document) {
                requests.add(new DeleteOneModel<T>(document));
                return BulkWriteBuilderImpl.this;
            }
        });
    }

    public BulkWriteBuilder<T> updateMany(Bson filter, Bson update, UpdateOptions options) {
        requests.add(new UpdateManyModel<T>(filter, update, options));
        return this;
    }

    public BulkWriteBuilder<T> updateOne(Bson filter, Bson update, UpdateOptions options) {
        requests.add(new UpdateOneModel<T>(filter, update, options));
        return this;
    }

    @Override
    public QueryBuilder<T, ModificationBuilder<UpdateBuilder<BulkWriteBuilder<T>>>> update() {
        return QueryBuilderImpl.createForBulkWrite(this);
    }
}
