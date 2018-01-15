package org.elasticsearch.disk;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.observer.DiskKeeperThreadListener;
import org.elasticsearch.rest.*;

import java.io.IOException;

public class DiskKeeperAction extends BaseRestHandler {


    @Inject
    public DiskKeeperAction(Settings settings, RestController controller) {
        super(settings);

        // 返回DiskKeeper相关的参数设置情况
        controller.registerHandler(RestRequest.Method.GET, "/_disk_keeper", this);

        //运行DiskKeeper相关线程，用于周期
        logger.info(getClass().getName() + "is Loaded" );

        DiskKeeperThread run = new DiskKeeperThread("DiskKeeper", logger);
        //观察者模式，添加观察者
        DiskKeeperThreadListener listener = new DiskKeeperThreadListener("DiskKeeper", logger);
        run.addObserver(listener);
        new Thread(run).start();
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient nodeClient) throws IOException {

        return restChannel -> {
            DiskKeeperMsg msg = new DiskKeeperMsg();
            XContentBuilder builder = restChannel.newBuilder();
            builder.startObject();
            msg.toXContent(builder, restRequest);
            builder.endObject();
//            logger.info("endpoint /_disk_keeper is called!!");
            restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        };
    }

}

class DiskKeeperMsg implements ToXContent {
    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, Params params) throws IOException {
        return xContentBuilder
                    .startObject("plugin-settings")
                        .field("threadSleepPeriod", DiskKeeperThread.threadSleepPeriod +"s")
                        .field("indicesPersistenceDay", DiskKeeperThread.indicesPersistenceDay+"d")
                        .field("diskWatermarkPercent", DiskKeeperThread.diskWatermarkPercent + "%")
                        .field("httpPort", DiskKeeperThread.HTTP_PORT)
                    .endObject()
                    .startObject("status")
                        .field("deleted_index_pattern", DiskKeeperThread.deletedIndicesPattern)
                    .endObject()
                ;
    }
}