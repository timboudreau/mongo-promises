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
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
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
     * @param collection The collection
     */
    public CollectionPromises(MongoCollection<T> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection null");
        }
        this.collection = collection;
    }

    /**
     * Get the underlying collection.
     *
     * @return The collection
     */
    public MongoCollection<T> collection() {
        return collection;
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
                try {
                    collection.deleteOne(data, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
            }
        });
    }

    public AsyncPromise<Bson, UpdateResult> replaceOne(final T replacement, final UpdateOptions opts) {
        return AsyncPromise.create(new Logic<Bson, UpdateResult>() {

            @Override
            public void run(Bson data, Trigger<UpdateResult> next, PromiseContext context) throws Exception {
                try {
                    collection.replaceOne(data, replacement, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
            }
        });
    }

    public QueryBuilder<T, ReplaceBuilder<AsyncPromise<Void, UpdateResult>, T>> replaceOne() {
        return QueryBuilderImpl.createForReplace(this);
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
                try {
                    collection.deleteMany(data, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
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
                try {
                    collection.insertOne(data, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
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
                try {
                    collection.insertMany(data, opts, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
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
        System.out.println("Do update one " + modification);
        AsyncPromise<Bson, UpdateResult> m = AsyncPromise.create(new Logic<Bson, UpdateResult>() {
            @Override
            public void run(Bson data, final Trigger<UpdateResult> next, PromiseContext context) throws Exception {
                System.out.println("Perform update query " + data);
                try {
                    collection.updateOne(data, modification, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
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
                try {
                    collection.updateMany(data, modification, opts, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
            }
        });
        return m;
    }

    /**
     * Create a promise for find one and update.
     *
     * @param modification The modification (the query will be passed to the
     * returned promise's start() method).
     * @param opts The options
     * @return A promise
     */
    public AsyncPromise<Bson, T> findOneAndUpdate(final Bson modification, final FindOneAndUpdateOptions opts) {
        AsyncPromise<Bson, T> m = AsyncPromise.create(new Logic<Bson, T>() {

            @Override
            public void run(Bson data, Trigger<T> next, PromiseContext context) throws Exception {
                try {
                    collection.findOneAndUpdate(data, modification, opts, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
            }
        });
        return m;
    }

    /**
     * Create a builder for find-and-update. The result of this is a chained set
     * of builders (you will call build() several times) - first you set up the
     * query, then the modification, then the find and update options.
     *
     * @return A query builder
     */
    public QueryBuilder<T, ModificationBuilder<FindOneAndUpdateBuilder<T, Void>>> findOneAndUpdate() {
        return QueryBuilderImpl.createForFindAndModify(this);
    }

    /**
     * Perform a find() or findOne() using a query builder to assemble the
     * query. first.
     *
     * @return
     */
    public QueryBuilder<T, FindBuilder<T, Void>> query() {
        return QueryBuilderImpl.create(this);
    }

    /**
     * Get a builder to configure and execute an update of one or many.
     * documents.
     *
     * @return The update builder
     */
    public UpdateBuilder<AsyncPromise<Bson, UpdateResult>> update() {
        return UpdateBuilderImpl.create(this);
    }

    /**
     * Create a promise for updating using a builder.
     *
     * @return A builder
     */
    public QueryBuilder<T, ModificationBuilder<UpdateBuilder<AsyncPromise<Void, UpdateResult>>>> updateWithQuery() {
        return QueryBuilderImpl.createForUpdate(this);
    }

    /**
     * Get a builder for bulk writes.
     *
     * @return A builder
     */
    public BulkWriteBuilder<T> bulkWrite() {
        return BulkWriteBuilderImpl.create(this);
    }

    /**
     * Perform a bulk write, passing the raw arguments.
     *
     * @param requests The requests
     * @param opts The options
     * @return A promise
     */
    public AsyncPromise<Void, BulkWriteResult> bulkWrite(List<WriteModel<? extends T>> requests, final BulkWriteOptions opts) {
        final List<WriteModel<? extends T>> reqs = new ArrayList<>(requests);
        return AsyncPromise.create(new Logic<Void, BulkWriteResult>() {

            @Override
            public void run(Void data, Trigger<BulkWriteResult> next, PromiseContext context) throws Exception {
                try {
                    collection.bulkWrite(reqs, opts, new SRC<BulkWriteResult>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
            }
        });
    }

    AsyncPromise<Bson, T> findOne(final FindBuilderImpl<T, ?> builder) {
        AsyncPromise<Bson, T> m = AsyncPromise.create(new SimpleLogic<Bson, T>() {
            @Override
            public void run(Bson data, Trigger<T> next) throws Exception {
                try {
                    FindIterable<T> find = builder.apply(collection.find(data));
                    find.first(new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
            }
        });
        return m;
    }

    /**
     * Count documents, supplying your own Bson to the promise.
     *
     * @return A CountBuilder
     */
    public CountBuilder<Bson> count() {
        return CountBuilderImpl.create(this);
    }

    /**
     * Count documents, using a QueryBuilder to build the query to match
     * against.
     *
     * @return A query builder
     */
    public QueryBuilder<T, CountBuilder<Void>> countWithQuery() {
        QueryBuilderImpl<T, CountBuilder<Void>> x = QueryBuilderImpl.createForCount(this);
        return x;
    }

    /**
     * Count documents, supplying your own Bson to the promise and using the
     * passed count options.
     *
     * @param opts The count options
     * @return A promise
     */
    public AsyncPromise<Bson, Long> count(final CountOptions opts) {
        return AsyncPromise.create(new SimpleLogic<Bson, Long>() {

            @Override
            public void run(Bson data, Trigger<Long> next) throws Exception {
                try {
                    collection.count(data, opts, new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
            }
        });
    }

    AsyncPromise<Bson, List<T>> find(final FindBuilderImpl<T, ?> builder, final FindReceiver<List<T>> withResults) {
        AsyncPromise<Bson, AsyncBatchCursor<T>> m = AsyncPromise.create(new Logic<Bson, AsyncBatchCursor<T>>() {
            @Override
            public void run(Bson data, Trigger<AsyncBatchCursor<T>> next, PromiseContext context) throws Exception {
                try {
                    builder.apply(collection.find(data)).batchCursor(new SRC<>(next));
                } catch (Exception e) {
                    next.trigger(null, e);
                }
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

    FindBuilderImpl<T, Bson> findImpl() {
        return FindBuilderImpl.create(this);
    }

    /**
     * Perform a find operation - use the builder to set up the parameters of
     * the query, then use one of its find*() methods to get a promise you can
     * pass the query itself to.
     *
     * @return A find builder
     */
    public FindBuilder<T, Bson> find() {
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
