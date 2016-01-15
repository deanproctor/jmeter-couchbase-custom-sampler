package com.avalonconsult.handlers;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.Arrays;
import java.util.List;

import static com.avalonconsult.constants.CBArguments.*;

/**
 * @author moorejm
 */
public abstract class AbstractCouchbaseHandler implements ICouchbaseHandler {
    protected static final Logger LOGGER = LoggingManager.getLoggerForClass();
    protected static volatile DefaultCouchbaseEnvironment couchbaseEnv;
    protected static volatile Cluster cluster;
    protected static volatile Bucket bucket;
    protected final JavaSamplerContext context;
    protected Boolean debug = false;

    public AbstractCouchbaseHandler(JavaSamplerContext context) {
        this.context = context;
        this.debug = Boolean.valueOf(context.getParameter(DEBUG, "false"));

        setupCouchbaseEnv();
        setupCouchbaseCluster(getCouchbaseEnv());
        setupBucket();
    }

    protected static CouchbaseEnvironment getCouchbaseEnv() {
        return couchbaseEnv;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Bucket getBucket() {
        return bucket;
    }

    private synchronized void setupCouchbaseEnv() {
        if (debug) {
            LOGGER.info("Setting up Couchbase Environment");
        }

        int timeout = context.getIntParameter(TIMEOUT, 10000);
        int bootstrap_carrier_direct_port = context.getIntParameter(BOOTSTRAP_CARRIER_DIRECT_PORT, 11210);

        if (couchbaseEnv == null) {
            couchbaseEnv = DefaultCouchbaseEnvironment.builder()
                    .connectTimeout(timeout)
                    .kvTimeout(timeout)
                    .bootstrapCarrierDirectPort(bootstrap_carrier_direct_port)
                    .build();
        }
    }

    private synchronized void setupCouchbaseCluster(CouchbaseEnvironment env) {
        if (couchbaseEnv == null) {
            setupCouchbaseEnv();
        }

        if (debug) {
            LOGGER.info("Setting up Couchbase Cluster Connection");
        }

        String servers = context.getParameter(SERVERS);
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("The server list is empty!");
        }
        // (Subset of) nodes in the cluster to establish a connection
        List<String> hosts = Arrays.asList(servers.split("\\s*[,;]\\s*"));
        cluster = (env == null) ?
                CouchbaseCluster.create(couchbaseEnv, hosts) :
                CouchbaseCluster.create(env, hosts);
    }

    private synchronized void setupBucket() {
        String bucketName = context.getParameter(BUCKET);
        String bucketPass = context.getParameter(BUCKET_PASSWORD);

        if (cluster == null) {
            setupCouchbaseCluster(couchbaseEnv);
        }

        if (debug) {
            LOGGER.info("Opening bucket: " + bucketName);
        }

        String adminName = context.getParameter(ADMIN_USERNAME);
        String adminPass = context.getParameter(ADMIN_PASSWORD);

        if (adminName.isEmpty()) {
            bucket = bucketPass.isEmpty() ?
                    cluster.openBucket(bucketName) :
                    cluster.openBucket(bucketName, bucketPass);
        } else {
            // adminPass could be blank
            ClusterManager clusterManager = cluster.clusterManager(adminName, adminPass);

            if (clusterManager.hasBucket(bucketName)) {
                bucket = bucketPass.isEmpty() ?
                        cluster.openBucket(bucketName) :
                        cluster.openBucket(bucketName, bucketPass);
            } else {
                throw new IllegalArgumentException("Bucket " + bucketName + "does not exist!");
            }
        }


    }

}
