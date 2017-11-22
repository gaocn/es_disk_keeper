package org.elasticsearch.disk;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DiskKeeperThread extends Thread {
    private String hostAddress;
    private int port;
    private RestClient client;
    private Logger logger;

    public DiskKeeperThread(String name, Logger logger) {
        super(name);
        this.logger = logger;
        try {
            hostAddress = DiskKeeperThread.getLocalHostLANAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        port = 9200;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getId() + "[" + Thread.currentThread().getName() + "]";
        logger.info("DiskKeeper Thread " +  name +" is started");

        while (true) {
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                logger.info(name + "is interrupted!");
            }

            try {
                //NOTE: 必须改为本机的IP地址，否则会错误
                client = RestClient.builder(new HttpHost(hostAddress, port)).build();
                Response response = client.performRequest("GET","/_cat/allocation");

                //0 0b 2.5gb 1.4gb 3.9gb 63 127.0.0.1 127.0.0.1 MH_Omy5
                String allocation = EntityUtils.toString(response.getEntity());
//                        logger.info(allocation);
                logger.info("Disks Percent: " + DiskKeeperThread.calculateDiskUsage(allocation, ""));

                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *   shards disk.indices disk.used disk.avail disk.total disk.percent host            ip              node
     *    670       48.2gb    99.4gb     68.8gb    168.3gb           59   10.230.135.127  10.230.135.127  node-127
     669       35.4gb    73.4gb     94.8gb    168.3gb           43   10.230.135.128  10.230.135.128  node-128
     670       36.9gb    77.3gb     90.9gb    168.3gb           45   10.230.135.126  10.230.135.126  node-126
     *
     *
     * NOTE: 只有数据节点才会有磁盘使用百分比，其他节点返回-1
     * @param allocation
     */
    public static int  calculateDiskUsage(String allocation, String host) {
        host = "10.233.87.241";
        String[] lines = allocation.split("\n");

        for (String line : lines) {
            if(line.contains(host)) {
                String[] tmp = line.split("\\s+");
                return Integer.valueOf(tmp[5]);
            }
        }
        return -1;
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

    public static void main(String[] args) throws Exception {
        String allocation = "669       35.4gb    73.4gb     94.8gb    168.3gb           43   10.233.87.241  10.230.135.128  node-128\n670       36.9gb    77.3gb     90.9gb    168.3gb           45   10.230.135.126  10.230.135.126  node-126";
        System.out.println(DiskKeeperThread.getLocalHostLANAddress().getHostAddress());
    }

}
