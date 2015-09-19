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
import com.mongodb.client.model.CountOptions;
import java.util.concurrent.TimeUnit;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
final class CountBuilderImpl implements CountBuilder {
    private final CountOptions opts = new CountOptions();
    private final CollectionPromises<?> promises;

    public CountBuilderImpl(CollectionPromises<?> promises) {
        this.promises = promises;
    }

    public CountBuilder hint(Bson hint) {
        opts.hint(hint);
        return this;
    }

    public CountBuilder hintString(String hint) {
        opts.hintString(hint);
        return this;
    }

    public CountBuilder limit(int limit) {
        opts.limit(limit);
        return this;
    }

    public CountBuilder skip(int skip) {
        opts.skip(skip);
        return this;
    }

    public CountBuilder maxTime(long maxTime, TimeUnit timeUnit) {
        opts.maxTime(maxTime, timeUnit);
        return this;
    }

    @Override
    public AsyncPromise<Bson, Long> count() {
        return promises.count(opts);
    }

}
