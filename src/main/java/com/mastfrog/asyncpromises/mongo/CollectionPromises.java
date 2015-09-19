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
import com.mastfrog.asyncpromises.Logic;
import com.mastfrog.asyncpromises.PromiseContext;
import com.mastfrog.asyncpromises.SimpleLogic;
import com.mastfrog.asyncpromises.Trigger;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.async.AsyncBatchCursor;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import org.bson.conversions.Bson;

/**
 * Promise wrapper for a MongoCollection.
 *
 * @author Tim Boudreau
 */
public class CollectionPromises<T> {

    private final MongoCollection<T> collection;

    /**
     * Create a wrapper.
     *
     * @param coll The collection
     */
    public CollectionPromises(MongoCollection<T> coll) {
        if (coll == null) {
            throw new IllegalArgumentException("Collection null");
        }
        this.collection = coll;
    }

    /**
     * Create a copy over a the collection with an alternate element type.
     *
     * @param <R> The type
     * @param type The type
     * @return A promise wrapper
     */
    public <R> CollectionPromises<R> withType(Class<R> type) {
        return new CollectionPromises<R>(collection.withDocumentClass(type));
    }

    /**
     * Set the write concern and return a new object
     *
     * @param concern The write concern
     * @return A new CollectionPromises
     */
    public CollectionPromises<T> withWriteConcern(WriteConcern concern) {
        return new CollectionPromises<>(collection.withWriteConcern(concern));
    }

    /**
     * Set the read concern and return a new object
     *
     * @param pref The write preference
     * @return A new CollectionPromises
     */
    public CollectionPromises<T> withReadPreference(ReadPreference pref) {
        return new CollectionPromises<>(collection.withReadPreference(pref));
    }

    /**
     * Create a promise to delete one element when passed the query.
     *
     * @return a promise
     */
    public AsyncPromise<Bson, DeleteResult> deleteOne() {
        return AsyncPromise.create(new Logic<Bson, DeleteResult>() {
            @Override
            public void run(Bson data, Trigger<DeleteResult> next, PromiseContext context) throws Exception {
                collection.deleteOne(data, new SRC<>(next));
            }
        });
    }

    /**
     * Create a promsie to delete many elements when passed the query.
     *
     * @return A promise
     */
    public AsyncPromise<Bson, DeleteResult> deleteMany() {
        return AsyncPromise.create(new Logic<Bson, DeleteResult>() {
            @Override
            public void run(Bson data, Trigger<DeleteResult> next, PromiseContext context) throws Exception {
                collection.deleteMany(data, new SRC<>(next));
            }
        });
    }

    /**
     * Create a promsie to insert one element when passed the element to insert.
     *
     * @return A promise
     */
    public AsyncPromise<T, Void> insertOne() {
        return AsyncPromise.create(new Logic<T, Void>() {
            @Override
            public void run(final T data, final Trigger<Void> next, PromiseContext context) throws Exception {
                collection.insertOne(data, new SRC<>(next));
            }
        });
    }

    /**
     * Create a promise to insert many elements when passed the elements to
     * insert.
     *
     * @return A promise
     */
    public AsyncPromise<List<T>, Void> insertMany() {
        return insertMany(new InsertManyOptions());
    }

    /**
     * Create a promise to insert many elements when passed the elements to
     * insert.
     *
     * @param opts The insert options
     * @return A promise
     */
    public AsyncPromise<List<T>, Void> insertMany(final InsertManyOptions opts) {
        return AsyncPromise.create(new Logic<List<T>, Void>() {
            @Override
            public void run(List<T> data, Trigger<Void> next, PromiseContext context) throws Exception {
                collection.insertMany(data, opts, new SRC<>(next));
            }
        });
    }

    /**
     * Update one element when the returned promise is passed the query.
     *
     * @param modification The modification to make (note that UpdateBuilder
     * offers a simpler way to correctly set MongoDB's update options.
     * @return A promise
     */
    public AsyncPromise<Bson, UpdateResult> updateOne(final Bson modification) {
        AsyncPromise<Bson, UpdateResult> m = AsyncPromise.create(new Logic<Bson, UpdateResult>() {
            @Override
            public void run(Bson data, Trigger<UpdateResult> next, PromiseContext context) throws Exception {
                collection.updateOne(data, modification, new SRC<>(next));
            }
        });
        return m;
    }

    /**
     * Update one element when the returned promise is passed the query.
     *
     * @param modification The modification to make (note that UpdateBuilder
     * offers a simpler way to correctly set MongoDB's update options.
     * @return A promise
     */
    public AsyncPromise<Bson, UpdateResult> updateMany(final Bson modification) {
        return updateMany(modification, new UpdateOptions());
    }

    /**
     * Update one element when the returned promise is passed the query.
     *
     * @param modification The modification to make (note that UpdateBuilder
     * offers a simpler way to correctly set MongoDB's update options.
     * @param opts The update options
     * @return A promise
     */
    public AsyncPromise<Bson, UpdateResult> updateMany(final Bson modification, final UpdateOptions opts) {
        AsyncPromise<Bson, UpdateResult> m = AsyncPromise.create(new Logic<Bson, UpdateResult>() {
            @Override
            public void run(Bson data, Trigger<UpdateResult> next, PromiseContext context) throws Exception {
                collection.updateMany(modification, data, opts, new SRC<>(next));
            }
        });
        return m;
    }

    /**
     * Get a builder to configure and execute an update of one or many
     * documents.
     *
     * @return The update builder
     */
    public UpdateBuilder updateMany() {
        return new UpdateBuilderImpl<>(this);
    }

    AsyncPromise<Bson, T> findOne(final FindBuilderImpl<T> builder) {
        AsyncPromise<Bson, T> m = AsyncPromise.create(new SimpleLogic<Bson, T>() {
            @Override
            public void run(Bson data, Trigger<T> next) throws Exception {
                FindIterable<T> find = builder.apply(collection.find(data));
                find.first(new SRC<>(next));
            }
        });
        return m;
    }

    public CountBuilder count() {
        return new CountBuilderImpl(this);
    }

    public AsyncPromise<Bson, Long> count(CountOptions opts) {
        return AsyncPromise.create(new SimpleLogic<Bson, Long>() {

            @Override
            public void run(Bson data, Trigger<Long> next) throws Exception {
                collection.count(data, new SRC<>(next));
            }
        });
    }

    AsyncPromise<Bson, List<T>> find(final FindBuilderImpl<T> builder, final FindReceiver<List<T>> withResults) {
        AsyncPromise<Bson, AsyncBatchCursor<T>> m = AsyncPromise.create(new Logic<Bson, AsyncBatchCursor<T>>() {
            @Override
            public void run(Bson data, Trigger<AsyncBatchCursor<T>> next, PromiseContext context) throws Exception {
                builder.apply(collection.find(data)).batchCursor(new SRC<>(next));
            }
        });
        final ContinueTrigger cont = new ContinueTrigger();
        AsyncPromise<AsyncBatchCursor<T>, List<T>> cursorPromise = AsyncPromise.create(new Logic<AsyncBatchCursor<T>, List<T>>() {
            @Override
            public void run(final AsyncBatchCursor<T> cursor, final Trigger<List<T>> next, final PromiseContext context) throws Exception {
                cursor.next(new SRC<>(new Trigger<List<T>>() {
                    @Override
                    public void trigger(List<T> obj, Throwable thrown) {
                        if (thrown != null) {
                            cursor.close();
                            next.trigger(obj, thrown);
                            return;
                        }
                        if (obj != null) {
                            try {
                                withResults.withResults(obj, cont, context);
                            } catch (Exception ex) {
                                next.trigger(obj, ex);
                                return;
                            }
                        }
                        if (cont.get()) {
                            cursor.next(new SRC<List<T>>(this));
                        } else {
                            cursor.close();
                            next.trigger(obj, thrown);
                        }
                    }
                }));
            }
        });
        return m.then(cursorPromise);
    }

    FindBuilderImpl<T> findImpl() {
        return new FindBuilderImpl<T>(this);
    }

    /**
     * Perform a find operation - use the builder to set up the parameters of
     * the query, then use one of its find*() methods to get a promise you can
     * pass the query itself to.
     *
     * @return A find builder
     */
    public FindBuilder<T> find() {
        return findImpl();
    }
    
    private static class SRC<T> implements SingleResultCallback<T> {

        private final Trigger<T> trigger;

        public SRC(Trigger<T> trigger) {
            this.trigger = trigger;
        }

        @Override
        public void onResult(T t, Throwable thrwbl) {
            trigger.trigger(t, thrwbl);
        }
    }
}
