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

    private ConcurrentHashMap<String, String> proxyToMap = new ConcurrentHashMap<>();

    @RequestMapping("/upload")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        // Read JSON from file
        InputStream inputStream = file.getInputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, String> tmpMap = objectMapper.readValue(inputStream, HashMap.class);
        for (Map.Entry<String, String> entry : tmpMap.entrySet()) {
            String src = entry.getKey();
            String dest = entry.getValue();
            try {
                this.setDest(src, dest);
            } catch (IOException e) {
                return "ERROR: The mapping table has not been set correctly.";
            }
        }

        return String.format("The mapping table has been updated successfully.");
    }

    @RequestMapping("/services/{graph}/sparql")
    public ResponseEntity<String> sendRequestToSPM(
            @PathVariable String graph,
            @RequestParam(required = true) Map<String, String> qparams,
            @RequestBody(required = false) String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {
        String proxyTo = proxyToMap.get(graph);
        return service.processProxyRequest(proxyTo, qparams, body, method, request, UUID.randomUUID().toString());
    }

    @RequestMapping("/mapping")
    @ResponseBody
    public String getMapping() {
        return StringUtils.join(proxyToMap);
    }

    @RequestMapping("/mapping/clear")
    @ResponseBody
    public String clearMapping() {
        proxyToMap.clear();
        return String.format("Successfully cleared mapping");
    }

    @RequestMapping("/mapping/{graph}")
    @ResponseBody
    public String getDest(@PathVariable String graph) {
        if (proxyToMap.containsKey(graph)) {
            return String.format("%s is mapped to %s", graph, proxyToMap.get(graph));
        } else {
            return String.format("%s is not mapped to any destination!", graph);
        }
    }

    @RequestMapping("/mapping/remove")
    @ResponseBody
    public String setDest(@RequestParam("proxyFrom") String src) throws IOException {
        if (proxyToMap.containsKey(src)) {
            proxyToMap.remove(src);
        }
        return String.format("The mapping table has been updated successfully.");
    }

    @RequestMapping("/mapping/set-destination")
    @ResponseBody
    public String setDest(@RequestParam("proxyFrom") String src, @RequestParam("proxyTo") String destination) throws IOException {

        File dockerenv = new File("/.dockerenv");
        String platform = "Not Docker:";
        if (dockerenv.isFile()) {
            platform = "Docker:";
            destination = destination.replace("localhost", "host.docker.internal");
        }
        proxyToMap.put(src, destination);

        return String.format(platform + "Proxy destination set to %s for path %s", destination, src);
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
