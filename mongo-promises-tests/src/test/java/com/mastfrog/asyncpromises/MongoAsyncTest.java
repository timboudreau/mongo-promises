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
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        AsyncPromise<Bson, Long> promise = p.find().descendingSortBy("ix").withBatchSize(20).find(new FindReceiver<List<Document>>() {

            int total = 0;

            @Override
            public void withResults(List<Document> obj, Trigger<Boolean> trigger, PromiseContext context) throws Exception {
                total += obj.size();
                all.addAll(obj);
                trigger.trigger(true, null);
            }
        }).then(new SimpleLogic<Void, Void>() {

            @Override
            public void run(Void data, Trigger<Void> next) throws Exception {
                nextWasRun.set(true);
                next.trigger(null, null);
            }
        }).then(new Document(), p.count().maxTime(10, TimeUnit.SECONDS).count());
        final CountDownLatch waitForAllResults = new CountDownLatch(1);
        final AtomicLong countHolder = new AtomicLong();
        promise.start(new Document(), new Trigger<Long>() {

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
        final List<Document> specific = new ArrayList<>();
        final CountDownLatch afterQuery = new CountDownLatch(1);
        p.query().in("ix", 1, 5, 10, 20).build()
                .descendingSortBy("ix")
                .limit(20).withBatchSize(10).find(new FindReceiver<List<Document>>() {

            @Override
            public void withResults(List<Document> obj, Trigger<Boolean> trigger, PromiseContext context) throws Exception {
                specific.addAll(obj);
                afterQuery.countDown();
            }
        }).onFailure(fh).start();
        afterQuery.await(10, TimeUnit.SECONDS);
        fh.assertNotThrown();
        assertFalse(specific.isEmpty());
        List<Integer> expect = new ArrayList<>(Arrays.<Integer>asList(1, 5, 10, 20));
        for (Document d : specific) {
            Integer ix = (Integer) d.get("ix");
            expect.remove(ix);
        }
        assertTrue("Did not find " + expect, expect.isEmpty());

        final CountDownLatch countWait = new CountDownLatch(1);
        final AtomicLong subcount = new AtomicLong();
        p.countWithQuery().greaterThan("ix", 100).build().skip(4).count().then(new Logic<Long, Void>() {

            @Override
            public void run(Long data, Trigger<Void> next, PromiseContext context) throws Exception {
                subcount.set(data);
                countWait.countDown();
                next.trigger(null, null);
            }
        }).onFailure(fh).start();
        countWait.await(10, TimeUnit.SECONDS);
        fh.assertNotThrown();
        assertEquals(95L, subcount.get());

        final Document[] oldAndNew = new Document[2];
        final CountDownLatch updateLatch = new CountDownLatch(1);
        p.findOneAndUpdate().equal("ix", 0).build().set("name", "Updated name").build().maxTime(10, TimeUnit.SECONDS)
                .projection().ignore("_id").build().build().onFailure(fh).then(new SimpleLogic<Document,Void>(){

            @Override
            public void run(Document data, Trigger<Void> next) throws Exception {
                oldAndNew[0] = data;
                next.trigger(null, null);
            }
        }).then(null, p.query().equal("ix", 0).build().findOne().then(new Logic<Document,Void>(){

            @Override
            public void run(Document data, Trigger<Void> next, PromiseContext context) throws Exception {
                oldAndNew[1] = data;
                next.trigger(null,null);
                updateLatch.countDown();
            }
        })).start();
        updateLatch.await(10, TimeUnit.SECONDS);
        assertNotNull(oldAndNew[1]);
        assertEquals("Updated name", oldAndNew[1].get("name"));
        assertNull("Projection not applied", oldAndNew[0].get("_id"));
        Assert.assertNotEquals(oldAndNew[0], oldAndNew[1]);
        final UpdateResult[] update = new UpdateResult[1];
        final CountDownLatch manyLatch = new CountDownLatch(1);
        p.updateWithQuery().lessThan("ix", 25).build().set("foo", "bar").build().updateMany().onFailure(fh).then(new SimpleLogic<UpdateResult, Void>(){

            @Override
            public void run(UpdateResult data, Trigger<Void> next) throws Exception {
                update[0] = data;
                next.trigger(null, null);
                manyLatch.countDown();
            }
        }).start();
        
        manyLatch.await(10, SECONDS);
        fh.assertNotThrown();
        assertNotNull(update[0]);
        assertEquals(25, update[0].getMatchedCount());
        assertEquals(25, update[0].getModifiedCount());
        final Document[] modif = new Document[1];
        final CountDownLatch modifLatch = new CountDownLatch(1);
        p.query().equal("ix", 0).build().findOne().onFailure(fh).then(new SimpleLogic<Document,Void>(){

            @Override
            public void run(Document data, Trigger<Void> next) throws Exception {
                modif[0] = data;
                next.trigger(null, null);
                modifLatch.countDown();
            }
        }).start();
        modifLatch.await(10, SECONDS);
        fh.assertNotThrown();
        assertNotNull(modif[0]);
        assertEquals("bar", modif[0].get("foo"));
    }

    static FH fh = new FH();
    static class FH implements FailureHandler {

        private Throwable thrown;
        @Override
        public <T> boolean onFailure(PromiseContext.Key<T> key, T input, Throwable thrown, PromiseContext context) {
            this.thrown = thrown;
            thrown.printStackTrace();
            return true;
        }

        void assertNotThrown() {
            Throwable old = thrown;
            thrown = null;
            if (old != null) {
                Exceptions.chuck(old);
            }
        }

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
                collection.withDocumentClass(Document.class).insertMany(all, new SingleResultCallback<Void>() {

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
