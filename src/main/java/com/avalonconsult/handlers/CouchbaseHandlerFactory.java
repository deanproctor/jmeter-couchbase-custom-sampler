package com.avalonconsult.handlers;

import com.avalonconsult.constants.CBArguments;
import com.avalonconsult.handlers.async.AsyncGetHandler;
import com.avalonconsult.handlers.async.AsyncN1QLQueryHandler;
import com.avalonconsult.handlers.async.AsyncPutHandler;
import com.avalonconsult.handlers.async.AsyncViewQueryHandler;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author moorejm
 */
public class CouchbaseHandlerFactory {

    private static Map<String, Class<? extends ICouchbaseHandler>> handlerMap;
    private static Map<String, Class<? extends ICouchbaseHandler>> asyncHandlerMap;

    static {
        handlerMap = new HashMap<String, Class<? extends ICouchbaseHandler>>();
        handlerMap.put("GET", GetHandler.class);
        handlerMap.put("PUT", PutHandler.class);
        handlerMap.put("VIEWQUERY", ViewQueryHandler.class);
        handlerMap.put("N1QL", N1QLQueryHandler.class);
    }

    static {
        asyncHandlerMap = new HashMap<String, Class<? extends ICouchbaseHandler>>();
        asyncHandlerMap.put("GET", AsyncGetHandler.class);
        asyncHandlerMap.put("PUT", AsyncPutHandler.class);
        asyncHandlerMap.put("VIEWQUERY", AsyncViewQueryHandler.class);
        asyncHandlerMap.put("N1QL", AsyncN1QLQueryHandler.class);
    }

    public static ICouchbaseHandler getHandler(JavaSamplerContext context) throws Exception {
        String method = context.getParameter(CBArguments.METHOD);
        Boolean async = Boolean.valueOf(context.getParameter(CBArguments.ASYNC));

        if (async) {
            if (asyncHandlerMap.containsKey(method)) {
                Class<? extends ICouchbaseHandler> cls = asyncHandlerMap.get(method.toUpperCase());
                if (cls != null) {
                    return cls.getDeclaredConstructor(JavaSamplerContext.class).newInstance(context);
                } else {
                    throw new Exception("Unable to instantiate method handler class");
                }
            } else {
                throw new IllegalArgumentException(method + " (async) is not supported as a method");
            }
        } else {
            if (handlerMap.containsKey(method)) {
                Class<? extends ICouchbaseHandler> cls = handlerMap.get(method.toUpperCase());
                if (cls != null) {
                    return cls.getDeclaredConstructor(JavaSamplerContext.class).newInstance(context);
                } else {
                    throw new Exception("Unable to instantiate method handler class");
                }
            } else {
                throw new IllegalArgumentException(method + " is not supported as a method");
            }
        }

    }
}
