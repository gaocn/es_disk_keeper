package org.elasticsearch.observer;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.disk.DiskKeeperThread;

import java.util.Observable;
import java.util.Observer;

/**
 * 观察者模式，用于监听DiskKeeperThread是否退出，若退出则重启；
 * DiskKeeperThread线程需要继承Observable类实现doBussiness方法
 */
public class DiskKeeperThreadListener implements Observer {

    private String threadName;
    private Logger logger;

    public DiskKeeperThreadListener(String threadName, Logger logger) {
        this.threadName = threadName;
        this.logger = logger;
    }

    @Override
    public void update(Observable o, Object arg) {
        logger.warn(this.threadName + "exits unexpected, trying to restart it");

        DiskKeeperThread run = new DiskKeeperThread(threadName, logger);
        Thread keeperThread = new Thread(run);
        run.addObserver(this);
        keeperThread.start();

    }
}
