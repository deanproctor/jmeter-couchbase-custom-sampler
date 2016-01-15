package com.avalonconsult.handlers;

import com.avalonconsult.constants.CBArguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

/**
 * @author moorejm
 */
public abstract class AbstractQueryHandler extends AbstractCouchbaseHandler {
    protected Integer limit;

    public AbstractQueryHandler(JavaSamplerContext context) {
        super(context);

        this.limit = context.getIntParameter(CBArguments.QUERY_LIMIT, 10);
        // No limit is designated by -1
        this.limit = (this.limit == 0) ? -1 : Math.max(-1, this.limit);
    }
}
