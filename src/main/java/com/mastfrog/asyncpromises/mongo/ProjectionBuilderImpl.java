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

import java.util.HashSet;
import java.util.Set;
import org.bson.Document;

/**
 *
 * @author Tim Boudreau
 */
final class ProjectionBuilderImpl<T> implements ProjectionBuilder<T> {

    private final Factory<T> factory;
    private final Set<String> include = new HashSet<>();
    private final Set<String> ignore = new HashSet<>();

    ProjectionBuilderImpl(Factory<T> factory) {
        this.factory = factory;
    }

    @Override
    public ProjectionBuilderImpl<T> include(String... strings) {
        for (String s : strings) {
            include.add(s);
            ignore.remove(s);
        }
        return this;
    }

    @Override
    public ProjectionBuilderImpl<T> ignore(String... strings) {
        for (String s : strings) {
            ignore.add(s);
            include.remove(s);
        }
        return this;
    }

    interface Factory<T> {

        T build(Document projection);
    }

    Document toDocument() {
        Document result = new Document();
        for (String s : include) {
            result.append(s, 1);
        }
        for (String s : ignore) {
            result.append(s, 0);
        }
        return result;
    }

    @Override
    public T build() {
        return factory.build(toDocument());
    }
}
