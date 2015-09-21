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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bson.Document;

/**
 *
 * @author Tim Boudreau
 */
final class ModificationBuilderImpl<T> implements ModificationBuilder<T> {

    private final Map<String, Object> set = new HashMap<>();
    private final Map<String, Object> inc = new HashMap<>();
    private final Map<String, Object> push = new HashMap<>();
    private final Map<String, Object> pull = new HashMap<>();
    private final Map<String, Object> setOnInsert = new HashMap<>();
    private final Map<String, String> rename = new HashMap<>();
    private final Set<String> unset = new HashSet<>();
    final Factory<T> factory;

    ModificationBuilderImpl(Factory<T> factory) {
        this.factory = factory;
    }

    Document toDocument() {
        Document result = new Document();
        if (!set.isEmpty()) {
            result.append("$set", new Document(set));
        }
        if (!setOnInsert.isEmpty()) {
            result.append("$setOnInsert", new Document(setOnInsert));
        }
        if (!inc.isEmpty()) {
            result.append("$inc", new Document(inc));
        }
        if (!push.isEmpty()) {
            result.append("$push", new Document(push));
        }
        if (!pull.isEmpty()) {
            result.append("$pull", new Document(push));
        }
        if (!unset.isEmpty()) {
            Document un = new Document();
            for (String key : unset) {
                un.append(key, "");
            }
            result.append("$unset", un);
        }
        if (!rename.isEmpty()) {
            Document rn = new Document();
            for (Map.Entry<String, String> e : rename.entrySet()) {
                rn.append(e.getKey(), e.getValue());
            }
            result.append("$rename", rn);
        }
        return result;
    }

    static <T> ModificationBuilder<UpdateBuilder<T>> create(UpdateBuilderImpl<T> impl) {
        return new ModificationBuilderImpl<>(new UpdateFactory<>(impl));
    }

    interface Factory<R> {

        R build(Document document);
    }

    static final class UpdateFactory<T> implements Factory<UpdateBuilder<T>> {

        private final UpdateBuilderImpl<T> update;

        public UpdateFactory(UpdateBuilderImpl<T> update) {
            this.update = update;
        }

        @Override
        public UpdateBuilder<T> build(Document document) {
            return update.modification(document);
        }
    }

    public T build() {
        return factory.build(toDocument());
    }

    public ModificationBuilder<T> rename(String old, String nue) {
        if (old.equals(nue)) {
            throw new IllegalArgumentException("Renaming " + old + " to " + nue);
        }
        rename.put(old, nue);
        return this;
    }

    public ModificationBuilder<T> setOnInsert(String key, Object value) {
        setOnInsert.put(key, value);
        return this;
    }

    public ModificationBuilder<T> unset(String name) {
        unset.add(name);
        return this;
    }

    public ModificationBuilder<T> set(String name, Object value) {
        set.put(name, value);
        return this;
    }

    public ModificationBuilder<T> decrement(String name) {
        return increment(name, -1);
    }

    public ModificationBuilder<T> decrement(String name, int amount) {
        return increment(name, -amount);
    }

    public ModificationBuilder<T> decrement(String name, long amount) {
        return increment(name, -amount);
    }

    public ModificationBuilder<T> increment(String name) {
        return increment(name, 1);
    }

    public ModificationBuilder<T> increment(String name, int amount) {
        inc.put(name, amount);
        return this;
    }

    public ModificationBuilder<T> increment(String name, long amount) {
        inc.put(name, amount);
        return this;
    }

    public ModificationBuilder<T> push(String name, Object val) {
        push.put(name, val);
        return this;
    }

    public ModificationBuilder<T> pull(String name, Object val) {
        pull.put(name, val);
        return this;
    }
}
