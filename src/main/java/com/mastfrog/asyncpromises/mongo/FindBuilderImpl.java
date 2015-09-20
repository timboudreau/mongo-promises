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
import com.mastfrog.asyncpromises.PromiseContext.Key;
import com.mastfrog.asyncpromises.Trigger;
import com.mongodb.CursorType;
import com.mongodb.async.client.FindIterable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
final class FindBuilderImpl<T, I> implements FindBuilder<T, I> {

    private int batchSize;
    private Bson projection;
    private CursorType cursorType;
    private int limit = 0;
    private Bson filter;
    private Bson modifiers;
    private Bson sort;
    private long maxTime = 0;
    private TimeUnit unit;
    private final Factory<T, I> factory;

    public static final Key<Bson> QUERY_KEY = PromiseContext.newKey(Bson.class);

    FindBuilderImpl(Factory<T, I> promises) {
        this.factory = promises;
    }

    static <T> FindBuilderImpl<T, Bson> create(CollectionPromises<T> promises) {
        checkNull("promises", promises);
        return new FindBuilderImpl<>(new StandardFactory<>(promises));
    }

    static <T> FindBuilderImpl<T, Void> create(CollectionPromises<T> promises, Bson query) {
        checkNull("promises", promises);
        checkNull("query", query);
        return new FindBuilderImpl<>(new VoidFactory<T>(promises, query));
    }

    static class VoidFactory<T> implements Factory<T, Void> {

        private final Factory<T, Bson> standard;
        private final Bson query;

        public VoidFactory(CollectionPromises<T> promises, Bson query) {
            this(new StandardFactory<>(promises), query);
        }

        public VoidFactory(Factory<T, Bson> standard, Bson query) {
            this.standard = standard;
            this.query = query;
        }

        @Override
        public AsyncPromise<Void, T> findOne(FindBuilderImpl<T, ?> builder) {
            return AsyncPromise.create(new Logic<Void, Bson>() {

                @Override
                public void run(Void data, Trigger<Bson> next, PromiseContext context) throws Exception {
                    context.put(QUERY_KEY, query);
                    next.trigger(query, null);
                }
            }).then(standard.findOne(builder));
        }

        @Override
        public <R> Factory<R, Void> withType(Class<R> type) {
            return new VoidFactory<R>(standard.withType(type), query);
        }

        @Override
        public AsyncPromise<Void, Void> find(FindBuilderImpl<T, ?> builder, FindReceiver<List<T>> logic) {
            return AsyncPromise.create(new Logic<Void, Bson>() {

                @Override
                public void run(Void data, Trigger<Bson> next, PromiseContext context) throws Exception {
                    context.put(QUERY_KEY, query);
                    next.trigger(query, null);
                }
            }).then(standard.find(builder, logic));
        }

    }

    static class StandardFactory<T> implements Factory<T, Bson> {

        private final CollectionPromises<T> promises;

        public StandardFactory(CollectionPromises<T> promises) {
            checkNull("promises", promises);
            this.promises = promises;
        }

        @Override
        public AsyncPromise<Bson, Void> find(FindBuilderImpl<T, ?> builder, FindReceiver<List<T>> logic) {
            checkNull("logic", logic);
            AsyncPromise<Bson, List<T>> result = promises.find(builder, logic);
            return result.then(new Logic<List<T>, Void>() {
                @Override
                public void run(List<T> data, Trigger<Void> next, PromiseContext context) throws Exception {
                    next.trigger(null, null);
                }
            });
        }

        @Override
        public AsyncPromise<Bson, T> findOne(FindBuilderImpl<T, ?> builder) {
            return promises.findOne(builder);
        }

        @Override
        public <R> Factory<R, Bson> withType(Class<R> type) {
            return new StandardFactory<>(promises.withType(type));
        }
    }

    interface Factory<T, I> {

        AsyncPromise<I, Void> find(FindBuilderImpl<T, ?> builder, FindReceiver<List<T>> logic);

        AsyncPromise<I, T> findOne(FindBuilderImpl<T, ?> builder);

        <R> Factory<R, I> withType(Class<R> type);
    }

    @Override
    public <R> FindBuilder<R, I> withResultType(Class<R> type) {
        FindBuilderImpl<R, I> result = new FindBuilderImpl<>(factory.withType(type));
        result.batchSize = batchSize;
        result.projection = projection;
        result.cursorType = cursorType;
        result.limit = limit;
        result.filter = filter;
        result.modifiers = modifiers;
        result.sort = sort;
        result.maxTime = maxTime;
        result.unit = unit;
        return result;
    }

    FindIterable<T> apply(FindIterable<T> iter) {
        if (batchSize > 0) {
            iter = iter.batchSize(batchSize);
        }
        if (limit > 0) {
            iter = iter.limit(limit);
        }
        if (projection != null) {
            iter = iter.projection(projection);
        }
        if (cursorType != null) {
            iter = iter.cursorType(cursorType);
        }
        if (filter != null) {
            iter = iter.filter(filter);
        }
        if (modifiers != null) {
            iter = iter.modifiers(modifiers);
        }
        if (unit != null) {
            iter = iter.maxTime(maxTime, unit);
        }
        if (sort != null) {
            iter = iter.sort(sort);
        }
        return iter;
    }

    @Override
    public FindBuilder<T, I> withBatchSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Batch size must be at least one: " + size);
        }
        batchSize = size;
        return this;
    }

    @Override
    public FindBuilder<T, I> withProjection(Bson projection) {
        this.projection = projection;
        return this;
    }

    @Override
    public FindBuilder<T, I> withCursorType(CursorType cursorType) {
        this.cursorType = cursorType;
        return this;
    }

    @Override
    public FindBuilder<T, I> limit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Limit must be at least one: " + amount);
        }
        this.limit = amount;
        return this;
    }

    static void checkNull(String name, Object o) {
        if (o == null) {
            throw new IllegalArgumentException(name + " is null");
        }
    }

    @Override
    public FindBuilder<T, I> filter(Bson bson) {
        checkNull("filter", bson);
        this.filter = bson;
        return this;
    }

    @Override
    public FindBuilder<T, I> modifiers(Bson bson) {
        checkNull("modifiers", bson);
        this.modifiers = bson;
        return this;
    }

    @Override
    public FindBuilder<T, I> sort(Bson bson) {
        checkNull("sort", bson);
        this.sort = bson;
        return this;
    }

    @Override
    public FindBuilder<T, I> ascendingSortBy(String name) {
        checkNull("name", name);
        return sort(new Document(name, 1));
    }

    @Override
    public FindBuilder<T, I> descendingSortBy(String name) {
        checkNull("name", name);
        return sort(new Document(name, -1));
    }

    @Override
    public FindBuilder<T, I> maxTime(long amount, TimeUnit units) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        checkNull("units", units);
        this.maxTime = amount;
        this.unit = units;
        return this;
    }

    @Override
    public AsyncPromise<I, Void> find(FindReceiver<List<T>> logic) {
        checkNull("logic", logic);
        return factory.find(this, logic);
    }

    @Override
    public AsyncPromise<I, T> findOne() {
        return factory.findOne(this);
    }
}
