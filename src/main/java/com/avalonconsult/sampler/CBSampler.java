package com.avalonconsult.sampler;

import com.avalonconsult.handlers.CouchbaseHandlerFactory;
import com.avalonconsult.handlers.ICouchbaseHandler;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.logging.Level;

import static com.avalonconsult.constants.CBArguments.*;

public class CBSampler extends AbstractJavaSamplerClient implements Serializable {

    public static final String ENCODING = "UTF-8";
    private static final long serialVersionUID = 3L;
    private static final org.apache.log.Logger LOGGER = LoggingManager.getLoggerForClass();

    private ICouchbaseHandler cbHandler;

    // set up default arguments for the JMeter GUI
    @Override
    public org.apache.jmeter.config.Arguments getDefaultParameters() {
        org.apache.jmeter.config.Arguments defaultParameters = new org.apache.jmeter.config.Arguments();
        defaultParameters.addArgument(METHOD, "GET");
        defaultParameters.addArgument(SERVERS, "127.0.0.1");
        defaultParameters.addArgument(ADMIN_USERNAME, "Administrator");
        defaultParameters.addArgument(ADMIN_PASSWORD, "couchbase");
        defaultParameters.addArgument(TIMEOUT, "10000");
        defaultParameters.addArgument(BUCKET, "beer-sample");
        defaultParameters.addArgument(BUCKET_PASSWORD, "");
        defaultParameters.addArgument(KEY, "");
        defaultParameters.addArgument(VALUE, "");
        defaultParameters.addArgument(LOCAL_FILE_PATH, "");
        defaultParameters.addArgument(DESIGN_DOC, "");
        defaultParameters.addArgument(VIEW_NAME, "");
        defaultParameters.addArgument(QUERY_LIMIT, "10");
        defaultParameters.addArgument(N1QL_QUERY, "");
        defaultParameters.addArgument(ASYNC, "false");
        defaultParameters.addArgument(DEBUG, "true");

        defaultParameters.addArgument(BOOTSTRAP_CARRIER_DIRECT_PORT, "11210");

        return defaultParameters;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {

        Boolean debug = Boolean.valueOf(context.getParameter("debug"));
        if (debug) {
            java.util.logging.Logger.getLogger("com.couchbase.client").setLevel(Level.SEVERE);
        }

        try {
            this.cbHandler = CouchbaseHandlerFactory.getHandler(context);
        } catch (Exception ex) {
            LOGGER.error("setupTest: ", ex);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (this.cbHandler != null) {
            Cluster cluster = this.cbHandler.getCluster();
            Bucket bucket = this.cbHandler.getBucket();

            if (bucket != null) {
                bucket.close();
            }

            if (cluster != null) {
                cluster.disconnect();
            }
            this.cbHandler = null;
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult result = newSampleResult();
        result.sampleStart(); // start stopwatch

        try {
            if (cbHandler == null || cbHandler.getBucket() == null) {
                throw new Exception("CB Client not initialized");
            }

            long startTime = System.nanoTime();

            cbHandler.handle();

            long endTime = System.nanoTime();

            sampleResultSuccess(result, Long.toString(endTime - startTime));

        } catch (Exception e) {
            sampleResultFailed(result, e);

            // get stack trace as a String to return as document data
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), ENCODING);
            result.setDataType(SampleResult.TEXT);
            result.setResponseCode("500");

            LOGGER.error("runTest: ", e);
        }

        return result;
    }

    public SampleResult newSampleResult() {
        SampleResult result = new SampleResult();
        result.setDataEncoding(ENCODING);
        result.setDataType(SampleResult.TEXT);
        return result;
    }

    /**
     * Set the sample result as <code>sampleEnd()</code>,
     * <code>setSuccessful(true)</code>, <code>setResponseCodeOK()</code> and if
     * the response is not <code>null</code> then
     * <code>setResponseData(response.toString(), ENCODING)</code> otherwise it is
     * marked as not requiring a response.
     *
     * @param result   sample result to change
     * @param response the successful result message, may be null.
     */
    protected void sampleResultSuccess(SampleResult result,
                                       String response) {
        result.sampleEnd();
        result.setSuccessful(true);
        result.setResponseCodeOK();
        if (response != null) {
            result.setResponseData(response, ENCODING);
            result.setResponseMessage(response);
        } else {
            result.setResponseData("No response required", ENCODING);
        }
    }

    /**
     * Mark the sample result as <code>sampleEnd</code>,
     * <code>setSuccessful(false)</code> and the <code>setResponseCode</code> to
     * reason.
     *
     * @param result the sample result to change
     * @param reason the failure reason
     */
    protected void sampleResultFailed(SampleResult result, String reason) {
        result.sampleEnd();
        result.setSuccessful(false);
        result.setResponseCode(reason);
        result.setResponseData(reason, ENCODING);
        result.setResponseMessage(reason);
    }

    /**
     * Equivalent to
     * <code>sampleResultFailed(result, "Exception raised: " + cause)</code>
     *
     * @param result the result to modify
     * @param cause  the cause of the failure
     */
    protected void sampleResultFailed(SampleResult result, Exception cause) {
        sampleResultFailed(result, "Exception raised: " + cause);
    }
}

