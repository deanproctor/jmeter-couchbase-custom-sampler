package com.avalonconsult.handlers;

import com.avalonconsult.constants.CBArguments;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author moorejm
 */
public class N1QLQueryHandler extends AbstractQueryHandler {

    protected String n1qlQuery;

    public N1QLQueryHandler(JavaSamplerContext context) {
        super(context);

        this.n1qlQuery = context.getParameter(CBArguments.N1QL_QUERY);

    }

    @Override
    public void handle() {

        N1qlQueryResult queryResult =
                bucket.query(N1qlQuery.simple(this.n1qlQuery));

        if (debug) {
            LOGGER.info(String.format("[%s] Running Query \"%s\"", getClass().getSimpleName(), this.n1qlQuery));

            if (queryResult.parseSuccess()) {
                LOGGER.info("Parse Successful");
            }
            if (queryResult.finalSuccess()) {
                LOGGER.info("Query Successful");
            }

            LOGGER.info(String.valueOf(extractResultOrThrow(queryResult)));
        }
    }

    protected static List<Map<String, Object>> extractResultOrThrow(N1qlQueryResult result) {
        if (!result.finalSuccess()) {
            LOGGER.warn("Query returned with errors: " + result.errors());
            throw new RuntimeException("Query error: " + result.errors());
        }

        List<Map<String, Object>> content = new ArrayList<Map<String, Object>>();
        for (N1qlQueryRow row : result) {
            content.add(row.value().toMap());
        }
        return content;
    }
}
