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
import com.mongodb.client.model.CountOptions;
import java.util.concurrent.TimeUnit;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
final class CountBuilderImpl<I> implements CountBuilder<I> {

    private CountOptions opts = new CountOptions();
    private final Factory<I> factory;

    public CountBuilderImpl(Factory<I> factory) {
        this.factory = factory;
    }

    static CountBuilderImpl<Void> create(CollectionPromises<?> promises, Bson query) {
        return new CountBuilderImpl<Void>(new VoidFactory(new StandardFactory(promises), query));
    }

    static CountBuilderImpl<Bson> create(CollectionPromises<?> promises) {
        return new CountBuilderImpl<Bson>(new StandardFactory(promises));
    }

    interface Factory<I> {

        AsyncPromise<I, Long> count(CountBuilderImpl<?> builder);
    }

    static final class VoidFactory implements Factory<Void> {

        private final StandardFactory std;
        private final Bson query;

        public VoidFactory(StandardFactory std, Bson query) {
            this.std = std;
            this.query = query;
        }

        @Override
        public AsyncPromise<Void, Long> count(CountBuilderImpl<?> builder) {
            return AsyncPromise.create(new Logic<Void, Bson>() {

                @Override
                public void run(Void data, Trigger<Bson> next, PromiseContext context) throws Exception {
                    next.trigger(query, null);
                }
            }).then(std.count(builder));
        }

    }

    static final class StandardFactory implements Factory<Bson> {

        private final CollectionPromises<?> promises;

        public StandardFactory(CollectionPromises<?> promises) {
            this.promises = promises;
        }

        @Override
        public AsyncPromise<Bson, Long> count(CountBuilderImpl<?> builder) {
            return promises.count(builder.opts);
        }
    }

    public CountBuilder<I> hint(Bson hint) {
        opts = opts.hint(hint);
        return this;
    }

    public CountBuilder<I> hintString(String hint) {
        opts = opts.hintString(hint);
        return this;
    }

    public CountBuilder<I> limit(int limit) {
        opts = opts.limit(limit);
        return this;
    }

    public CountBuilder<I> skip(int skip) {
        opts = opts.skip(skip);
        return this;
    }

    public CountBuilder<I> maxTime(long maxTime, TimeUnit timeUnit) {
        opts = opts.maxTime(maxTime, timeUnit);
        return this;
    }

    @Override
    public AsyncPromise<I, Long> count() {
        return factory.count(this);
    }
}
