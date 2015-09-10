package com.avalonconsult.handlers.async;

import com.avalonconsult.handlers.PutHandler;
import com.couchbase.client.java.document.StringDocument;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import rx.functions.Action1;

import java.util.concurrent.TimeUnit;

/**
 * @author moorejm
 */
public class AsyncPutHandler extends PutHandler {
    public AsyncPutHandler(JavaSamplerContext context) {
        super(context);
    }

    @Override
    public void handle() {
        bucket.async()
                .upsert(StringDocument.create(key, value))
                .timeout(2, TimeUnit.SECONDS)
                .subscribe(new Action1<StringDocument>() {
                    @Override
                    public void call(StringDocument stringDocument) {
                        if (debug) {
                            LOGGER.info(String.valueOf(stringDocument));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LOGGER.error("Async PUT Error: ", throwable);
                    }
                });


    }
}
