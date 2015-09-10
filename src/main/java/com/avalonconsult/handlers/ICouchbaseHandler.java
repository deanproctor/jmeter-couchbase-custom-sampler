package com.avalonconsult.handlers;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;

/**
 * @author moorejm
 */
public interface ICouchbaseHandler {
    Cluster getCluster();

    Bucket getBucket();

    void handle();
}
