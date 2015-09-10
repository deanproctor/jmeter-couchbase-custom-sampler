package com.avalonconsult.handlers.async;

import com.avalonconsult.handlers.N1QLQueryHandler;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.AsyncN1qlQueryRow;
import com.couchbase.client.java.query.N1qlQuery;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author moorejm
 */
public class AsyncN1QLQueryHandler extends N1QLQueryHandler {


    public AsyncN1QLQueryHandler(JavaSamplerContext context) {
        super(context);
    }

    @Override
    public void handle() {

        bucket.async()
                .query(N1qlQuery.simple(this.n1qlQuery))
                .subscribe(new Action1<AsyncN1qlQueryResult>() {
                    @Override
                    public void call(AsyncN1qlQueryResult result) {
                        // subscribe to N1QL errors
                        result.errors().subscribe(new Action1<JsonObject>() {
                            @Override
                            public void call(JsonObject e) {
                                LOGGER.error("N1QL Error/Warning: " + e);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable runtimeError) {
                                LOGGER.error("N1QL Error/Warning: ", runtimeError);
                            }
                        });

                        // subscribe to N1QL results
                        result.rows().map(new Func1<AsyncN1qlQueryRow, JsonObject>() {
                            @Override
                            public JsonObject call(AsyncN1qlQueryRow row) {
                                return row.value();
                            }
                        }).subscribe(
                                new Action1<JsonObject>() {
                                    @Override
                                    public void call(JsonObject rowContent) {
                                        if (debug) {
                                            LOGGER.info(String.valueOf(rowContent));
                                        }
                                    }
                                },
                                new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable runtimeError) {
                                        LOGGER.error("N1QL Query Error: ", runtimeError);
                                    }
                                }
                        );
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        LOGGER.error("N1QL Query Error: ", t);
                    }
                });
    }
}
