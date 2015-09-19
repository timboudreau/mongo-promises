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

package com.mastfrog.asyncpromises;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.mastfrog.asyncpromises.MongoAsyncTest.M;
import com.mastfrog.asyncpromises.mongo.CollectionPromises;
import com.mastfrog.asyncpromises.mongo.FindReceiver;
import com.mastfrog.giulius.mongodb.async.GiuliusMongoAsyncModule;
import com.mastfrog.giulius.mongodb.async.MongoAsyncInitializer;
import com.mastfrog.giulius.mongodb.async.MongoHarness;
import com.mastfrog.giulius.tests.GuiceRunner;
import com.mastfrog.giulius.tests.TestWith;
import com.mastfrog.util.Exceptions;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.bson.Document;
import org.bson.conversions.Bson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Tim Boudreau
 */
@RunWith(GuiceRunner.class)
@TestWith({M.class, MongoHarness.Module.class})
public class MongoAsyncTest {

    @Test
    public void testAsync(@Named("stuff") MongoCollection<Document> coll) throws InterruptedException {
        CollectionPromises<Document> p = new CollectionPromises<>(coll);
        final List<Document> all = new LinkedList<>();
        final AtomicBoolean nextWasRun = new AtomicBoolean();
        AsyncPromise<Bson, Long> promise = p.find().descendingSortBy("ix").withBatchSize(20).find(new FindReceiver<List<Document>>(){
            
            int total = 0;
            
            @Override
            public void withResults(List<Document> obj, Trigger<Boolean> trigger, PromiseContext context) throws Exception {
                total += obj.size();
                System.out.println("GET ONE BATCH " + obj.size() + " TOTAL " + total);
                all.addAll(obj);
                trigger.trigger(true, null);
            }
        }).then(new SimpleLogic<Void,Void>(){

            @Override
            public void run(Void data, Trigger<Void> next) throws Exception {
                nextWasRun.set(true);
                next.trigger(null, null);
            }
        }).then(new Document(), p.count().maxTime(10, TimeUnit.SECONDS).count());
        final CountDownLatch waitForAllResults = new CountDownLatch(1);
        final AtomicLong countHolder = new AtomicLong();
        promise.start(new Document(), new Trigger<Long>(){

            @Override
            public void trigger(Long count, Throwable thrown) {
                countHolder.set(count);
                waitForAllResults.countDown();
            }
        });
        waitForAllResults.await(10, TimeUnit.SECONDS);
        assertTrue(nextWasRun.get());
        assertEquals(200, all.size());
        assertEquals(200L, countHolder.get());
    }
    
    static final class M implements Module {

        @Override
        public void configure(Binder binder) {
            GiuliusMongoAsyncModule m = new GiuliusMongoAsyncModule()
                    .bindCollection("stuff").withInitializer(Ini.class);
            binder.install(m);
        }
        
        
        static class Ini extends MongoAsyncInitializer {

            @Inject
            public Ini(Registry reg) {
                super(reg);
            }

            @Override
            public void onCreateCollection(String name, MongoCollection<?> collection) {
                assertEquals(name, "stuff", name);
                List<Document> all = new LinkedList<>();
                for (int i = 0; i < 200; i++) {
                    Document d = new Document("ix", i).append("name", "Thing-" + i);
                    all.add(d);
                }
                final CountDownLatch latch = new CountDownLatch(1);
                collection.withDocumentClass(Document.class).insertMany(all, new SingleResultCallback<Void>(){

                    @Override
                    public void onResult(Void t, Throwable thrwbl) {
                        latch.countDown();
                    }
                });
                try {
                    latch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Exceptions.chuck(ex);
                }
            }
        }
    }
}
