package org.elasticsearch.disk;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;

import java.io.IOException;

public class DiskKeeperAction extends BaseRestHandler {
    public static RestClient client;

    @Inject
    public DiskKeeperAction(Settings settings, RestController controller) {
        super(settings);

        // 返回DiskKeeper相关的参数设置情况
        controller.registerHandler(RestRequest.Method.GET, "/_disk_keeper", this);

        //运行DiskKeeper相关线程，用于周期
        logger.info("[" + getClass().getName() + "] is Loaded, Run DiskKeeperThread" );
        try {
            client = RestClient.builder(new HttpHost("localhost", 9200)).build();
            getDiskStats();
        } catch (Exception e) {
            logger.info("RestClient For DiskKeeper is Stop for some reason, Check It Out!");
        }
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient nodeClient) throws IOException {

        return restChannel -> {
            DiskKeeperMsg msg = new DiskKeeperMsg();
            XContentBuilder builder = restChannel.newBuilder();
            builder.startObject();
            msg.toXContent(builder, restRequest);
            builder.endObject();
            logger.info("endpoint /_disk_keeper is called!!");
            restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        };
    }

    public static void getDiskStats() {
        try {
            Response response = client.performRequest("GET","/_cat/allocation");
            if (response.getStatusLine().getStatusCode() == 200) {
                System.out.println(EntityUtils.toByteArray(response.getEntity()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class DiskKeeperMsg implements ToXContent {
    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, Params params) throws IOException {
        return xContentBuilder.field("threadsleep.period", "1m")
                .field("indices.persistence.day", "14d")
                .field("disk.watermark.percent", "80" + "%");
    }
}