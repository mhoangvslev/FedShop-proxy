package com.fedshop.proxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Map;

@Service
public class SpringProxyService {
    private final static Logger logger = LogManager.getLogger(SpringProxyService.class);

    public ResponseEntity<String> processProxyRequest(String proxyTo, Map<String, String> qparams, String body,
            HttpMethod method, HttpServletRequest request, String traceId)
            throws URISyntaxException {
        ThreadContext.put("traceId", traceId);
        String requestUrl = request.getRequestURI();

        // log if required in this line
        // logger.info(String.format("requestURI: %s, proxyTo: %s", requestUrl, proxyTo));

        // qparams.forEach((key, value) -> {
        //    logger.info(String.format("qparam_key: %s, qparam_value: %s", key, value));
        // });

        // replacing context path form urI to match actual gateway URI
        URI uri = UriComponentsBuilder.fromUriString(proxyTo)
                .path(requestUrl)
                .query(request.getQueryString())
                .build(true).toUri();

        // logger.info(String.format("finalUri: %s", uri));

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        headers.set("TRACE", traceId);
        headers.remove(HttpHeaders.ACCEPT_ENCODING);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        RestTemplate restTemplate = new RestTemplate(factory);
        try {
            // logger.info(qparams.toString());
            if (qparams.containsKey("query")) {
                String queryString = qparams.get("query");
                // logger.info(queryString);
                if (queryString.toLowerCase().matches("select.*limit 1") || queryString.toLowerCase().matches("ask\\s+\\{")) {
                    Info.NB_ASK.incrementAndGet();
                }
            }

            ResponseEntity<String> serverResponse = restTemplate.exchange(uri, method, httpEntity, String.class);

            if (qparams.size() > 0) {
                Info.NB_HTTP_REQ.incrementAndGet();

                long contentLength = serverResponse.getHeaders().getContentLength();
                if (contentLength == -1) {
                    String responseBody = serverResponse.getBody();
                    if (responseBody != null) {
                        contentLength = responseBody.getBytes().length;
                    }
                }
                Info.DATA_TRANSFER.addAndGet(contentLength);
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.put(HttpHeaders.CONTENT_TYPE, serverResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));

            // logger.info(serverResponse);
            return serverResponse;

        } catch (HttpStatusCodeException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }

    }
}
