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

import com.mastfrog.asyncpromises.PromiseContext;
import com.mastfrog.asyncpromises.Trigger;

/**
 * Receives batches of results from the cursor.
 *
 * @param <T> The type of the results
 */
public interface FindReceiver<T> {

    /**
     * Implement this to do something with each batch of results
     *
     * @param obj The results batch
     * @param trigger Call this trigger with <code>true</code> if you want
     * to receive further batches of results, false otherwise.
     * @param context The context, which can be used to retrieve objects put
     * there by earlier Logic instances in the chain, or to pass objects to
     * later ones.
     */
    void withResults(T obj, Trigger<Boolean> trigger, PromiseContext context) throws Exception;
    
}
