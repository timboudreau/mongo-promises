MongoDB Async Promises
======================

This library provides wrappers around MongoDB's asynchronous Java API to make programming
to it easier - particularly chaining together chunks of work that should happen in sequence,
asynchronously.

It uses the [async promises](https://github.com/timboudreau/async-promises) library under the
hood, and also provides builder classes for things like updates, which have fairly fussy and
non-typesafe syntax in MongoDB.

To use, simply create a `CollectionPromises<T>` over a native MongoDB `MongoCollection<T>`,
and use the instance methods on that.

Example:
```
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

