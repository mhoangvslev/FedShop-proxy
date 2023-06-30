package com.fedshop.proxy;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Info {
    public static AtomicLong NB_ASK = new AtomicLong(0) ;
    public static AtomicLong NB_HTTP_REQ = new AtomicLong(0);
    public static AtomicLong DATA_TRANSFER = new AtomicLong(0);

    public static void reset() {
        NB_ASK.set(0);
        NB_HTTP_REQ.set(0);
        DATA_TRANSFER.set(0);
    }

    public static String asString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("NB_ASK", NB_ASK.get());
        rootNode.put("NB_HTTP_REQ", NB_HTTP_REQ.get());
        rootNode.put("DATA_TRANSFER", DATA_TRANSFER.get());

        String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        return result;
    }
}
