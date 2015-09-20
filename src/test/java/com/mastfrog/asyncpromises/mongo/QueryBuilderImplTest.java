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

import com.mastfrog.asyncpromises.mongo.QueryBuilderImpl.Factory;
import java.util.Arrays;
import org.bson.Document;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * Additional tests are in the adjacent mongo-promises-tests project since
 * they would otherwise create a circular dependency on giulius-async-mongodb's
 * MongoHarness.
 *
 * @author Tim Boudreau
 */
public class QueryBuilderImplTest {
    
    @Test
    public void test() {
        QueryBuilderImpl<String,Document> q = new QueryBuilderImpl<>(f);
        Document d = q.equal("skidoo", 23).embedded("foo").embedded("bar").equal("meaning", 42)
                .build()
                .build()
                .elemMatch("array").equal("name", "Joe").greaterThan("age", 18).lessThan("age", 65).build()
                .in("city", "Boston", "New York", "Shutesbury").build();
        
        System.out.println("DOCUMENT: " + d);
        assertEquals(Integer.valueOf(23), d.get("skidoo"));
        assertEquals(Integer.valueOf(42), d.get("foo.bar.meaning"));
        Document in = (Document) d.get("city");
        assertNotNull(in);
        assertEquals(in.get("$in"), Arrays.asList("Boston", "New York", "Shutesbury"));
    }
    
    Factory<String, Document> f = new Factory<String,Document>() {

        @Override
        public Document create(Document document) {
            return document;
        }
        
    };
}
