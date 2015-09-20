MongoDB Async Promises
======================

This library provides wrappers around MongoDB's asynchronous Java API to make programming
to it easier - particularly chaining together chunks of work that should happen in sequence,
asynchronously.

Read [the Javadoc](http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/mongo-promises/target/apidocs/index.html)

It uses the [async promises](https://github.com/timboudreau/async-promises) library under the
hood, and also provides builder classes for things like updates, which have fairly fussy and
non-typesafe syntax in MongoDB.

To use, simply create a `CollectionPromises<T>` over a native MongoDB `MongoCollection<T>`,
and use the instance methods on that.

Example
-------

```java
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


promise.start(new Document(), new Trigger<Long>(){

    @Override
    public void trigger(Long count, Throwable thrown) {
        countHolder.set(count);
        waitForAllResults.countDown();
    }
});
```

(if you're using Java 8, the above code can be made simpler with lambdas, but this library
is compatible with Java 7).

Builders
--------

A number of builder classes are included to make things easy - in each case, they return an
AsyncPromise you can start to execute the operation or chain it together with other operations.

For example, querying can use a nice `QueryBuilder` (which lets you specify things like $in, $gt,
partial or complete subdocument matches, etc.):

```java

CollectionPromises<Document> p = new CollectionPromises<>(someCollection);

p.query().equal("skidoo", 23).embedded("foo").embedded("bar").equal("meaning", 42)
                .build()
                .build()
                .elemMatch("array").equal("name", "Joe").greaterThan("age", 18).lessThan("age", 65).build()
                .in("city", "Boston", "New York", "Shutesbury").build()

```

results in a MongoDB query as follows:

```
{
	skidoo : 23, 
	foo.bar.meaning : 42, 
	logins : { 
		country : USA 
	}, 
	city : { 
		$in : [Boston, New York, Shutesbury] 
	}, 
	people : {
		$elemMatch : {
			name : Joe, 
			age : {
				$gt : 18, $lt : 65
			}
		}
	}
}
```

This then drops you into a `FindBuilder` which lets you configure cursor attributes like limit
and sort order, which in turn builds a promise you can run.
