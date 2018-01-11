package org.elasticsearch.disk;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiskKeeperThread extends Thread {
    private static String HOST_ADDR;
    private RestClient client;
    private Logger logger;



    /**
     * 保存按照时间格式(yyyy-MM-dd)排序后的索引，在删除过期索引时使用。
     */
    TreeMap<String, Set<String>> sortedMapIndices = new TreeMap<>();

   // 插件配置
    static int threadSleepPeriod = 30;
    static int indicesPersistenceDay = 14;
    static int diskWatermarkPercent = 80;
    static int HTTP_PORT = 9200;

    /** TODO
     * 测试使用，用于显示已删除的index-pattern;
     */
    static Set<String> deletedIndicesPattern = new TreeSet<>();

    public DiskKeeperThread(String name, Logger logger) {
        super(name);
        this.logger = logger;
        Thread.setDefaultUncaughtExceptionHandler(new DiskKeeperThreadExceptionHandler(logger));

        try {
            HOST_ADDR = DiskKeeperThread.getLocalHostLANAddress().getHostAddress();
            /**
             * 该类运行时user.dir值为：
             *      /home/sm01/elk5.2/elasticsearch-5.2.0
             * 实际文件路径：
             *     /home/sm01/elk5.2/elasticsearch-5.2.0/plugins/es_disk_keeper
             */
            Properties properties = new Properties();
            String filePath = System.getProperty("user.dir") + "/plugins/es_disk_keeper/plugin-settings.properties";
            properties.load(new FileInputStream(filePath));

            if (!properties.getProperty("threadSleepPeriod").isEmpty()) {
                threadSleepPeriod = Integer.valueOf(properties.getProperty("threadSleepPeriod"));
            }
            if (!properties.getProperty("indicesPersistenceDay").isEmpty()) {
                indicesPersistenceDay = Integer.valueOf(properties.getProperty("indicesPersistenceDay"));
            }
            if (!properties.getProperty("diskWatermarkPercent").isEmpty()) {
                diskWatermarkPercent = Integer.valueOf(properties.getProperty("diskWatermarkPercent"));
            }
            if (!properties.getProperty("httpPort").isEmpty()) {
                HTTP_PORT = Integer.valueOf(properties.getProperty("httpPort"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getId() + "[" + Thread.currentThread().getName() + "]";
        logger.info("DiskKeeper Thread " +  name +" is started");

        while (true) {
            try {
                Thread.sleep(threadSleepPeriod * 1000);
            } catch (InterruptedException e) {
                logger.info(name + "is interrupted!");
            }

            deleteOutDateIndices();
            diskUsageKeeper();
        }
    }

    /**
     *   shards disk.indices disk.used disk.avail disk.total disk.percent host            ip              node
     *    670       48.2gb    99.4gb     68.8gb    168.3gb           59   10.230.135.127  10.230.135.127  node-127
     669       35.4gb    73.4gb     94.8gb    168.3gb           43   10.230.135.128  10.230.135.128  node-128
     670       36.9gb    77.3gb     90.9gb    168.3gb           45   10.230.135.126  10.230.135.126  node-126
     *
     * NOTE: 只有数据节点才会有磁盘使用百分比，其他节点返回-1
     */
    public int calculateDiskUsage() {
        //NOTE: 必须改为本机的IP地址，否则会错误
        client = RestClient.builder(new HttpHost(HOST_ADDR, HTTP_PORT)).build();
        Response response = null;
        int diskPercent = -1;

        try {
            response = client.performRequest("GET","/_cat/allocation");

            if (response.getStatusLine().getStatusCode() == 200) {
                //0 0b 2.5gb 1.4gb 3.9gb 63 127.0.0.1 127.0.0.1 MH_Omy5
                String allocation = EntityUtils.toString(response.getEntity());
                String[] lines = allocation.split("\n");
                for (String line : lines) {
                    if (line.contains(HOST_ADDR)) {
//                        logger.info("Matched Line: " + line);
                        String[] tmp = line.trim().split("\\s+");
//                        logger.info("Splitted Line: "+ Arrays.toString(tmp) + "With length: " + tmp.length);

                        diskPercent = Integer.valueOf(tmp[5]);
                    }
                }
            }
            client.close();
            return diskPercent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diskPercent;
    }

    /**  0    1                  2                                             4             5 6       7 8     9        10
     * green open kafka_oauth_ebfoperationdetail-2017.11.23           P88hVsPnSJKzzx-y4bgPfQ 2 1      17 0  858.2kb  429.1kb
     * @return
     */
    public void refreshKeepedIndices() {
        String index;
        try {
            client = RestClient.builder(new HttpHost(HOST_ADDR, HTTP_PORT)).build();
            Response response = client.performRequest("GET","/_cat/indices");

            if (response.getStatusLine().getStatusCode() == 200) {
                String[] indicesInfo = EntityUtils.toString(response.getEntity()).split("\n");
//                logger.info("There are " + indicesInfo.length + " indices totally!");
                for (String indexInfo : indicesInfo) {
                    try {
                        index = indexInfo.split("\\s+")[2];
                        if (index.matches("^(.*-)(\\d{4,4}\\.\\d{2,2}\\.\\d{2,2})$")) {
                            String datePattern = index.substring(index.length() - 10);
                            if (sortedMapIndices.get(datePattern) == null) {
                                Set<String> indicesSet = new TreeSet<>();
                                indicesSet.add(index);
                                sortedMapIndices.put(datePattern, indicesSet);
                            } else {
                                sortedMapIndices.get(datePattern).add(index);
                            }
                        }
                    } catch (Exception e) {
//                        logger.info(index + " should NOT deleted!");
                    }
                }
            }
            client.close();
            // TEST
//            ArrayList<String> sortedIndices = new ArrayList<>();
//            for (Set<String> set : sortedMapIndices.values())
//                sortedIndices.addAll(set);
//            logger.info(sortedIndices);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteOutDateIndices() {
        // Do Not Delete indices(store indices as long as possible!)
        if (indicesPersistenceDay == 0) {
            return;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        GregorianCalendar calendar = new GregorianCalendar(Locale.CHINA);
        calendar.add(Calendar.DAY_OF_MONTH, -(indicesPersistenceDay+1));
        String indexPattern = "/*-" + format.format(calendar.getTime());
        try {
            client = client = RestClient.builder(new HttpHost(HOST_ADDR, HTTP_PORT)).build();
            // head /*-yyyy-MM-dd 检查结果为404，因为不存在这种名称的索引，因此该方法直接删除过期的索引！
            Response response = client.performRequest("DELETE", indexPattern);
            if (response.getStatusLine().getStatusCode() == 200) {
                    logger.info("IndexPattern: " + indexPattern + "[DELETED]");
                    deletedIndicesPattern.add(indexPattern);
            } else {
                logger.info("IndexPattern: " + indexPattern + "UNABLE to delete for " + response.getStatusLine().getReasonPhrase());
            }
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void diskUsageKeeper() {
        logger.info("##### Disk Usage Keeper Chech Start #####");

        int diskUsage = calculateDiskUsage();
        //更新sortedMapIndices
        refreshKeepedIndices();
        client = client = RestClient.builder(new HttpHost(HOST_ADDR, HTTP_PORT)).build();

        if (diskUsage != -1) {
        indics_clean:
            while (diskUsage > diskWatermarkPercent) {
                for (String indexPattern : sortedMapIndices.get(sortedMapIndices.firstKey())) {
                    try {
                        Response response = client.performRequest("DELETE","/" + indexPattern);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            logger.info(indexPattern + " DELETED for disk usage is above watermark!");
                            deletedIndicesPattern.add(indexPattern);
                        }
                        //该操作会持久化内存数据到磁盘缓冲区，会执行segment merge操作
                        response = client.performRequest("GET", "/_flush");
                        logger.info("GET /_flush http/1.1 \n"+ EntityUtils.toString(response.getEntity()));

                        diskUsage = calculateDiskUsage();
                        if (diskUsage < diskWatermarkPercent) {
                            break indics_clean;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                refreshKeepedIndices();
                diskUsage = calculateDiskUsage();
            }
        }
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("##### Disk Usage Keeper Check End #####");
    }

    public static InetAddress getLocalHostLANAddress() throws Exception {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String a = "0 0b 2.5gb 1.4gb 3.9gb 63 127.0.0.1 127.0.0.1 MH_Omy5";
        String[] tmp = a.split("\\s+");
        System.out.println(Integer.valueOf(tmp[5]));

        /**
         * 单节点，无索引数据时报错
         [2018-01-11T11:01:14,435][WARN ][o.e.b.ElasticsearchUncaughtExceptionHandler] [node_128] uncaught exception in thread [DiskKeeper]
         java.lang.ArrayIndexOutOfBoundsException: 2
         at org.elasticsearch.disk.DiskKeeperThread.refreshKeepedIndices(DiskKeeperThread.java:141) ~[?:?]
         at org.elasticsearch.disk.DiskKeeperThread.diskUsageKeeper(DiskKeeperThread.java:196) ~[?:?]
         at org.elasticsearch.disk.DiskKeeperThread.run(DiskKeeperThread.java:84) ~[?:?]


         */
    }
}

class DiskKeeperThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Logger logger;

    public DiskKeeperThreadExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.info("Exception Ignored: " + e.getMessage());
    }
}