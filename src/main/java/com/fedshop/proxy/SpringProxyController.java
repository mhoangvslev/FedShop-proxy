package com.fedshop.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SpringProxyController {
    @Autowired
    SpringProxyService service;

    @RequestMapping("/**")
    public ResponseEntity<String> sendRequestToSPM(
            @RequestParam(required = true) Map<String, String> qparams,
            @RequestBody(required = false) String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {
        return service.processProxyRequest(qparams, body, method, request, UUID.randomUUID().toString());
    }

    @RequestMapping("/reset")
    public String reset() {
        Info.reset();
        return "RESET";
    }

    @RequestMapping("/get-stats")
    public String getStats() throws JsonProcessingException {
        return Info.asString();
    }
}
