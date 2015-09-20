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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Tim Boudreau
 */
class QueryBuilderImpl<T, R> implements QueryBuilder<T, R> {

    private final Map<String, Object> equal = new HashMap<>();
    private final Map<String, Object[]> in = new HashMap<>();
    private final Map<String, Number> greaterThan = new HashMap<>();
    private final Map<String, Number> lessThan = new HashMap<>();
    private final Map<String, Number> greaterThanOrEqual = new HashMap<>();
    private final Map<String, Number> lessThanOrEqual = new HashMap<>();
    private final Map<String, Bson> elemMatch = new HashMap<>();
    private final Factory<T, R> factory;

    QueryBuilderImpl(Factory<T, R> factory) {
        this.factory = factory;
    }

    static <T> QueryBuilder<T, Document> create() {
        return new QueryBuilderImpl<T, Document>(new BsonFactory<T>());
    }

    static <T> QueryBuilderImpl<T, FindBuilder<T, Void>> create(CollectionPromises<T> promises) {
        return new QueryBuilderImpl<>(new FindFactory<>(promises));
    }

    static <T> QueryBuilderImpl<T, CountBuilder<Void>> createForCount(CollectionPromises<T> promises) {
        return new QueryBuilderImpl<>(new CountFactory<T>(promises));
    }

    static class BsonFactory<T> implements Factory<T, Document> {

        @Override
        public Document create(Document document) {
            return document;
        }
    }

    static class FindFactory<T> implements Factory<T, FindBuilder<T, Void>> {

        private final CollectionPromises<T> promises;

        public FindFactory(CollectionPromises<T> promises) {
            this.promises = promises;
        }

        @Override
        public FindBuilder<T, Void> create(Document document) {
            return FindBuilderImpl.create(promises, document);
        }
    }

    static class CountFactory<T> implements Factory<T, CountBuilder<Void>> {

        private final CollectionPromises<T> promises;

        public CountFactory(CollectionPromises<T> promises) {
            this.promises = promises;
        }

        @Override
        public CountBuilder<Void> create(Document document) {
            return CountBuilderImpl.create(promises, document);
        }
    }

    @Override
    public QueryBuilder<T, R> equal(String key, Object value) {
        equal.put(key, value);
        return this;
    }

    @Override
    public QueryBuilder<T, R> in(String key, Object... values) {
        in.put(key, values);
        return this;
    }

    @Override
    public QueryBuilder<T, R> greaterThan(String key, Number value) {
        greaterThan.put(key, value);
        return this;
    }

    @Override
    public QueryBuilder<T, R> lessThan(String key, Number value) {
        lessThan.put(key, value);
        return this;
    }

    @Override
    public QueryBuilder<T, R> greaterThanOrEqual(String key, Number value) {
        greaterThanOrEqual.put(key, value);
        return this;
    }

    @Override
    public QueryBuilder<T, R> lessThanOrEqual(String key, Number value) {
        lessThanOrEqual.put(key, value);
        return this;
    }

    @Override
    public QueryBuilder<T, R> id(Object id) {
        return equal("_id", id);
    }

    public QueryBuilder<T, QueryBuilder<T, R>> exactSubdocument(final String elem) {
        Factory<T, QueryBuilder<T, R>> f = new Factory<T, QueryBuilder<T, R>>() {

            @Override
            public QueryBuilder<T, R> create(Document document) {
                equal.put(elem, document);
                return QueryBuilderImpl.this;
            }

        };
        return new QueryBuilderImpl<>(f);
    }

    public QueryBuilder<T, QueryBuilder<T, R>> embedded(final String elem) {
        Factory<T, QueryBuilder<T, R>> f = new Factory<T, QueryBuilder<T, R>>() {

            @Override
            public QueryBuilder<T, R> create(Document document) {
                for (Map.Entry<String, Object> e : document.entrySet()) {
                    equal.put(elem + "." + e.getKey(), e.getValue());
                }
                return QueryBuilderImpl.this;
            }
        };
        return new QueryBuilderImpl<>(f);
    }

    public QueryBuilder<T, QueryBuilder<T, R>> elemMatch(final String elem) {
        return new QueryBuilderImpl<>(new Factory<T, QueryBuilder<T, R>>() {

            @Override
            public QueryBuilder<T, R> create(Document document) {
                QueryBuilderImpl.this.elemMatch.put(elem, new Document("$elemMatch", document));
                return QueryBuilderImpl.this;
            }
        });
    }

    interface Factory<T, R> {

        R create(Document document);
    }

    public R build() {
        return factory.create(toDocument());
    }

    Document toDocument() {
        Document result = new Document(equal);
        if (!in.isEmpty()) {
            for (Map.Entry<String, Object[]> e : in.entrySet()) {
                Document sub = new Document();
                sub.append("$in", Arrays.asList(e.getValue()));
                result.append(e.getKey(), sub);
            }
        }
        Set<String> keys = new HashSet<>(greaterThan.keySet());
        keys.addAll(lessThan.keySet());
        keys.addAll(lessThanOrEqual.keySet());
        keys.addAll(greaterThanOrEqual.keySet());
        for (String k : keys) {
            Document d = new Document();
            Number gt = greaterThan.get(k);
            Number lt = lessThan.get(k);
            Number gte = greaterThanOrEqual.get(k);
            Number lte = lessThanOrEqual.get(k);
            if (gt != null) {
                d.append("$gt", gt);
            }
            if (lt != null) {
                d.append("$lt", lt);
            }
            if (gte != null) {
                d.append("$gte", gte);
            }
            if (lte != null) {
                d.append("$lte", lte);
            }
            result.append(k, d);
        }
        for (Map.Entry<String, Bson> e : elemMatch.entrySet()) {
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }
}
