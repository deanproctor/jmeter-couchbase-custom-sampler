package com.avalonconsult.handlers;

import com.avalonconsult.constants.CBArguments;
import com.couchbase.client.java.view.ViewQuery;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

/**
 * @author moorejm
 */
public class ViewQueryHandler extends AbstractQueryHandler {

    protected String designDoc;
    protected String viewName;

    protected ViewQuery query;

    public ViewQueryHandler(JavaSamplerContext context) {
        super(context);

        this.designDoc = context.getParameter(CBArguments.DESIGN_DOC);
        this.viewName = context.getParameter(CBArguments.VIEW_NAME);

        ViewQuery query = ViewQuery.from(designDoc, viewName);
        if (limit != null && limit > 0) {
            query.limit(limit);
        }
    }

    @Override
    public void handle() {
        if (designDoc.isEmpty() || viewName.isEmpty()) {
            throw new IllegalArgumentException("Must provide DesignDoc and View name");
        }

        if (debug) {
            LOGGER.info(String.format("[%s] Querying %s/%s",
                    getClass().getSimpleName(), designDoc, viewName));
        }

        bucket.query(query);
    }
}
