// Generated by com.dv.sourcetreetool.impl.App
open module com.mastfrog.mongo.promises {
    exports com.mastfrog.asyncpromises.mongo;
    
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;

    // Sibling com.mastfrog/async-promises-3.0.0-dev
    // Transitive detected by source scan
    requires com.mastfrog.async.promises;

    // Inferred from test-source-scan
    requires junit;

    // derived from org.mongodb/mongodb-driver-async-0.0.0-? in org/mongodb/mongodb-driver-async/3.12.11/mongodb-driver-async-3.12.11.pom
    requires org.mongodb.driver.async.client;
}