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

import org.bson.types.ObjectId;

/**
 * Builder for MongoDB query documents, which supports querying subdocuments and
 * MongoDB's specific syntax for things like element matches, matching one of a
 * number of values, greater/less than and so forth.
 *
 * @author Tim Boudreau
 */
public interface QueryBuilder<T, R> {

    /**
     * The query includes that the passed key and value must be an exact match.
     *
     * @param key The property name
     * @param value The value
     * @return this
     */
    public QueryBuilder<T, R> equal(String key, Object value);

    /**
     * The query includes that the passed property name has a value which is one
     * of the passed values.
     *
     * @param key The key
     * @param values The values
     * @return this
     */
    public QueryBuilder<T, R> in(String key, Object... values);

    /**
     * Test that the named property is greater than the passed value
     *
     * @param key The property name
     * @param value The value
     * @return this
     */
    public QueryBuilder<T, R> greaterThan(String key, Number value);

    /**
     * Test that the named property is less than the passed value
     *
     * @param key The property name
     * @param value The value
     * @return this
     */
    public QueryBuilder<T, R> lessThan(String key, Number value);

    /**
     * Exactly match a document's ID.
     *
     * @param id The id
     * @return this
     */
    public QueryBuilder<T, R> id(Object id);

    /**
     * Build the output of this QueryBuilder (in the case of calls to
     * embedded(), elemMatch() and exactSubdocument(), this returns you to the
     * outer QueryBuilder).
     *
     * @return The output
     */
    public R build();

    /**
     * Get a sub query builder to construct an embedded document which should
     * match the property of the passed name. The resulting query will use
     * dot-notation to do partial matching on subdocuments.
     *
     * @param elem The element name
     * @return a sub query builder whose build() method returns you to this one
     */
    public QueryBuilder<T, QueryBuilder<T, R>> embedded(String elem);

    /**
     * Get a sub query builder to construct a document which should match on an
     * array element.
     *
     * @param elem The name of the array
     * @return a sub query builder whose build() method returns you to this one
     */
    public QueryBuilder<T, QueryBuilder<T, R>> elemMatch(final String elem);

    /**
     * Create a sub query builder to create a document which should exactly
     * match.
     *
     * @param elem The property name
     * @return a sub query builder whose build() method returns you to this one
     */
    public QueryBuilder<T, QueryBuilder<T, R>> exactSubdocument(final String elem);
}
