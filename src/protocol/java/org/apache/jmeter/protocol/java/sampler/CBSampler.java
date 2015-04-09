package com.bigstep;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.StringDocument;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

import java.io.Serializable;
import java.io.File;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
 
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
 
public class CBSampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();
    private Cluster cluster = null;
    private Bucket bucket = null;
    private String putContents = null;
    private Boolean async = false;
 
    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("method", "GET");
        defaultParameters.addArgument("servers", "127.0.0.1");
        defaultParameters.addArgument("bucket", "default");
        defaultParameters.addArgument("password", "");
        defaultParameters.addArgument("key", "");
        defaultParameters.addArgument("local_file_path", "");
        defaultParameters.addArgument("value", "");
        defaultParameters.addArgument("bootstrap_carrier_direct_port", "11210");
        defaultParameters.addArgument("timeout", "10000");
        defaultParameters.addArgument("async", "false");
        defaultParameters.addArgument("debug", "true");

        return defaultParameters;
    }
   	
    @Override 
    public void setupTest(JavaSamplerContext context)
    {
	String debug = context.getParameter( "debug" );
	if(debug=="false")	
	{
		java.util.logging.Logger.getLogger("com.couchbase.client").setLevel(Level.SEVERE);
	}
		
	try
	{
		String servers = context.getParameter( "servers" );
		String password = context.getParameter( "password" );
		String mybucket = context.getParameter( "bucket" );
		String method = context.getParameter( "method" );
		String file = context.getParameter( "local_file_path" );
		int bootstrap_carrier_direct_port = Integer.parseInt(context.getParameter( "bootstrap_carrier_direct_port" ));
		int timeout = Integer.parseInt(context.getParameter( "timeout" ));

		async = Boolean.valueOf(context.getParameter( "async" ));

		if(method.equals("PUT"))
			putContents = Files.readAllBytes(Paths.get(file)).toString();	

	   	// (Subset) of nodes in the cluster to establish a connection
		List<String> hosts= new ArrayList<String>();
		
		String[] arrServers=servers.split(","); 
		for(String server: arrServers)
			hosts.add(new String(server));
		
		CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
		  .bootstrapCarrierDirectPort(bootstrap_carrier_direct_port)
		  .kvTimeout(timeout)
		  .connectTimeout(timeout)
		  .build();
	
		cluster = CouchbaseCluster.create(env, hosts);
		bucket = cluster.openBucket(mybucket, password);
	}
	catch(Exception ex)
	{
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            ex.printStackTrace( new java.io.PrintWriter( stringWriter ) );

	    log.error("setupTest:"+ex.getMessage()+stringWriter.toString());
	}
    }
    
    @Override	
    public void teardownTest(JavaSamplerContext context)
    {
	if(null!=cluster)
		cluster.disconnect();		
    }
 
    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        String key = context.getParameter("key");
        String value = context.getParameter("value");
        String method = context.getParameter("method");
        
	SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch

        try {
	    if(null==bucket)
	       throw new Exception("CB Client not initialised");

	    long startTime=System.nanoTime();	

	    if(method.equals("GET"))	
	      if(async)
		bucket
		  .async()
		  .get(key,StringDocument.class)
		  .timeout(2, TimeUnit.SECONDS)
		  .subscribe();
	      else
		bucket.get(key,StringDocument.class);
	    else 
		if(method.equals("PUT"))
		{
		  if(value == "")
		    value = putContents;

		    if(async)
		      bucket
			.async()
			.upsert(StringDocument.create(key, value))
			.timeout(2, TimeUnit.SECONDS)
			.subscribe();
		    else
		      bucket.upsert(StringDocument.create(key, value));
		}

	    long endTime=System.nanoTime();
			
    
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful( true );
            result.setResponseMessage( Long.toString(endTime-startTime) );
            result.setResponseCodeOK(); // 200 code

        } catch (Exception e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful( false );
            result.setResponseMessage( "Exception: " + e );
 
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace( new java.io.PrintWriter( stringWriter ) );
            result.setResponseData( stringWriter.toString() );
            result.setDataType( org.apache.jmeter.samplers.SampleResult.TEXT );
            result.setResponseCode( "500" );

	     log.error("runTest:"+e.getMessage()+" "+stringWriter.toString());
        }
 
        return result;
    }
}

