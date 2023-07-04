package com.fedshop.proxy;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// @ExtendWith(SpringExtension.class)
// @WebMvcTest(SpringProxyController.class)

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SpringProxyTest {

    private static final int CONCURRENT_REQUESTS = 500;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void testConcurrentConnections() throws Exception {
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executorService.execute(() -> {
                try {
                    MvcResult result = this.mockMvc.perform(
                            get("/sparql")
                                    .param("default-graph-uri", "")
                                    .param("query", "SELECT * WHERE { ?s ?p ?o } LIMIT 1")
                                    .param("format", "text/html")
                                    .param("timeout", "0")
                                    .param("signal_void", "on"))
                            .andExpect(status().isOk())
                            .andReturn();

                    int dataTransferred = result.getResponse().getContentAsByteArray().length;
                    assertEquals(dataTransferred, 1481);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Assert the total data transfer size
        assertEquals(CONCURRENT_REQUESTS, Info.NB_ASK.get());
        assertEquals(CONCURRENT_REQUESTS, Info.NB_HTTP_REQ.get());
        assertEquals(CONCURRENT_REQUESTS * 1481, Info.DATA_TRANSFER.get());

    }
}
