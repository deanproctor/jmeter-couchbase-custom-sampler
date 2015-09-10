package com.avalonconsult.handlers;

import com.avalonconsult.constants.CBArguments;
import com.couchbase.client.java.document.StringDocument;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author moorejm
 */
public class PutHandler extends AbstractCouchbaseHandler {

    protected String putContents;
    protected String key;
    protected String value;

    public PutHandler(JavaSamplerContext context) {
        super(context);

        key = context.getParameter(CBArguments.KEY);
        value = context.getParameter(CBArguments.VALUE);

        String file = context.getParameter(CBArguments.LOCAL_FILE_PATH);
        try {
            this.putContents = Files.readAllBytes(Paths.get(file)).toString();
        } catch (IOException e) {
            LOGGER.error(getClass().getSimpleName() + ": File read error", e);
        }

    }

    @Override
    public void handle() {
        String key = context.getParameter(CBArguments.KEY);
        String value = context.getParameter(CBArguments.VALUE);

        if (value.isEmpty()) {
            value = this.putContents;
        }

        if (debug) {
            LOGGER.info(String.format("[%s] Putting key-value: (%s,%s)",
                    getClass().getSimpleName(), key, value));
        }

        bucket.upsert(StringDocument.create(key, value));

    }
}
