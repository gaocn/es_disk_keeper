package org.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.test.ESTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assume.assumeThat;

public class AbstractITCase extends ESTestCase{
    protected static final Logger staticLogger = ESLoggerFactory.getLogger("it");
    protected static final int HTTP_TEST_PORT = 9200;
    protected static RestClient client;

    @BeforeClass
    public static void startRestClient() {
        client = RestClient.builder(new HttpHost("10.230.135.128", HTTP_TEST_PORT)).build();

        try {
            Response response = client.performRequest("GET", "/?pretty");
            System.out.println("响应结果：" + EntityUtils.toString(response.getEntity()));
            staticLogger.info("Integeration tests ready to start... Cluster is running");
        } catch (IOException e) {
            staticLogger.warn("Integeration tests are skipped: [{}]", e.getMessage());
            assumeThat("Integration tests are skipped", e.getMessage(), not(containsString("Connection refused")));
            staticLogger.error("Full error is", e);
            fail("Something wrong is happening. REST Client seemed to raise an exception.");
        }
    }

    @AfterClass
    public static void stopRestClient() throws IOException {
        if (client != null) {
            client.close();
            client = null;
        }
        staticLogger.info("Stopping integration tests against an external cluster");
    }
}
