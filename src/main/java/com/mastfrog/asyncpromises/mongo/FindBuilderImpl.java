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
final class FindBuilderImpl<T> implements FindBuilder<T> {
    private int batchSize;
    private Bson projection;
    private CursorType cursorType;
    private int limit = 0;
    private Bson filter;
    private Bson modifiers;
    private Bson sort;
    private long maxTime = 0;
    private TimeUnit unit;
    private final CollectionPromises<T> promises;

    public FindBuilderImpl(CollectionPromises<T> promises) {
        this.promises = promises;
    }

    @Override
    public <R> FindBuilder<R> withResultType(Class<R> type) {
        FindBuilderImpl<R> result = promises.withType(type).findImpl();
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
    public FindBuilder<T> withBatchSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Batch size must be at least one: " + size);
        }
        batchSize = size;
        return this;
    }

    @Override
    public FindBuilder<T> withProjection(Bson projection) {
        this.projection = projection;
        return this;
    }

    @Override
    public FindBuilder<T> withCursorType(CursorType cursorType) {
        this.cursorType = cursorType;
        return this;
    }

    @Override
    public FindBuilder<T> limit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Limit must be at least one: " + amount);
        }
        this.limit = amount;
        return this;
    }

    private void checkNull(String name, Object o) {
        if (o == null) {
            throw new IllegalArgumentException(name + " is null");
        }
    }

    @Override
    public FindBuilder<T> filter(Bson bson) {
        checkNull("filter", bson);
        this.filter = bson;
        return this;
    }

    @Override
    public FindBuilder<T> modifiers(Bson bson) {
        checkNull("modifiers", bson);
        this.modifiers = bson;
        return this;
    }

    @Override
    public FindBuilder<T> sort(Bson bson) {
        checkNull("sort", bson);
        this.sort = bson;
        return this;
    }

    @Override
    public FindBuilder<T> ascendingSortBy(String name) {
        checkNull("name", name);
        return sort(new Document(name, 1));
    }

    @Override
    public FindBuilder<T> descendingSortBy(String name) {
        checkNull("name", name);
        return sort(new Document(name, -1));
    }

    @Override
    public FindBuilder<T> maxTime(long amount, TimeUnit units) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        checkNull("units", units);
        this.maxTime = amount;
        this.unit = units;
        return this;
    }

    @Override
    public AsyncPromise<Bson, Void> find(FindReceiver<List<T>> logic) {
        checkNull("logic", logic);
        AsyncPromise<Bson, List<T>> result = promises.find(this, logic);
        return result.then(new Logic<List<T>, Void>() {
            @Override
            public void run(List<T> data, Trigger<Void> next, PromiseContext context) throws Exception {
                next.trigger(null, null);
            }
        });
    }

    @Override
    public AsyncPromise<Bson, T> findOne() {
        return promises.findOne(this);
    }

}
