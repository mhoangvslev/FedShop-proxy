package com.fedshop.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
public class SpringProxyController {
    @Autowired
    SpringProxyService service;

    private String proxyTo;

    @RequestMapping("/sparql")
    public ResponseEntity<String> sendRequestToSPM(
            @RequestParam(required = true) Map<String, String> qparams,
            @RequestBody(required = false) String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {
        return service.processProxyRequest(proxyTo, qparams, body, method, request, UUID.randomUUID().toString());
    }

    @RequestMapping("/set-destination")
    @ResponseBody
    public String setDest(@RequestParam("proxyTo") String destination) throws IOException {
        proxyTo = destination;

        if (Files.lines(Paths.get("/proc/1/cgroup")).anyMatch(l -> l.contains("docker"))) {
            proxyTo = proxyTo.replace("localhost", "host.docker.internal");
        }
        return "Proxy destination set to: " + destination;
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
