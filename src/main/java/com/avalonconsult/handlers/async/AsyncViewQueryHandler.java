package com.avalonconsult.handlers.async;

import com.avalonconsult.handlers.ViewQueryHandler;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.AsyncViewResult;
import com.couchbase.client.java.view.AsyncViewRow;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

/**
 * @author moorejm
 */
public class AsyncViewQueryHandler extends ViewQueryHandler {

    public AsyncViewQueryHandler(JavaSamplerContext context) {
        super(context);
    }

    @Override
    public void handle() {
        bucket.async()
                .query(query)
                .timeout(2, TimeUnit.SECONDS)
                .subscribe(new Action1<AsyncViewResult>() {
                               @Override
                               public void call(AsyncViewResult asyncViewResult) {
                                   asyncViewResult.rows()
                                           .flatMap(new Func1<AsyncViewRow, Observable<? extends JsonDocument>>() {
                                               @Override
                                               public Observable<? extends JsonDocument> call(AsyncViewRow asyncViewRow) {
                                                   return asyncViewRow.document();
                                               }
                                           }).map(new Func1<JsonDocument, JsonObject>() {
                                       @Override
                                       public JsonObject call(JsonDocument doc) {
                                           return doc.content();
                                       }
                                   }).subscribe(new Action1<JsonObject>() {
                                       @Override
                                       public void call(JsonObject jsonObject) {
                                           if (debug) {
                                               LOGGER.info(String.valueOf(jsonObject));
                                           }
                                       }
                                   }, new Action1<Throwable>() {
                                       @Override
                                       public void call(Throwable throwable) {
                                           LOGGER.error("Async ViewQuery Error: ", throwable);
                                       }
                                   });
                               }
                           },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                LOGGER.error("Async ViewQuery Error: ", throwable);
                            }
                        });
    }
}
