package org.elasticsearch.disk;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.AbstractITCase;
import org.elasticsearch.client.Response;
import org.junit.Test;

import java.io.IOException;


public class DiskKeeperActionTest extends AbstractITCase {

    @Test
    public void getDiskStats() throws Exception {
        Response response = null;
        int diskPercent = -1;

        try {
            response = client.performRequest("GET","/_cat/allocation");

            if (response.getStatusLine().getStatusCode() == 200) {
                String allocation = EntityUtils.toString(response.getEntity());
                String[] lines = allocation.split("\n");
                for (String line : lines) {
                    if (line.contains("10.230.135.128")) {
                        String[] tmp = line.split("\\s+");
                        diskPercent = Integer.valueOf(tmp[5]);
                    }
                }
            }
            System.out.println(diskPercent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}