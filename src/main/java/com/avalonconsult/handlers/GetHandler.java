package com.avalonconsult.handlers;

import com.avalonconsult.constants.CBArguments;
import com.couchbase.client.java.document.StringDocument;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

/**
 * @author moorejm
 */
public class GetHandler extends AbstractCouchbaseHandler {

    protected String key;

    public GetHandler(JavaSamplerContext context) {
        super(context);
        this.key = context.getParameter(CBArguments.KEY);
    }

    @Override
    public void handle() {
        if (debug) {
            LOGGER.debug(String.format("[%s] Getting key: %s",
                    getClass().getSimpleName(), key));
        }

        StringDocument doc = bucket.get(key, StringDocument.class);

        if (debug && doc != null) {
            LOGGER.info("GET Result: " + String.valueOf(doc.content()));
        }
    }
}
