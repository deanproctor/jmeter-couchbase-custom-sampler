package com.avalonconsult.handlers.async;

import com.avalonconsult.handlers.GetHandler;
import com.couchbase.client.java.document.StringDocument;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import rx.functions.Action1;

import java.util.concurrent.TimeUnit;

/**
 * @author moorejm
 */
public class AsyncGetHandler extends GetHandler {

    public AsyncGetHandler(JavaSamplerContext context) {
        super(context);
    }

    @Override
    public void handle() {
        bucket.async()
                .get(key, StringDocument.class)
                .timeout(2, TimeUnit.SECONDS)
                .subscribe(new Action1<StringDocument>() {
                    @Override
                    public void call(StringDocument doc) {
                        if (debug && doc != null) {
                            LOGGER.info(String.valueOf(doc.content()));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        LOGGER.error("ASYNC GET error: ", t);
                    }
                });
    }
}
