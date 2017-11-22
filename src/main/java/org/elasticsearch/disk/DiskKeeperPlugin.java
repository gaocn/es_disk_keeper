package org.elasticsearch.disk;

import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;

import java.util.Collections;
import java.util.List;

public class DiskKeeperPlugin extends Plugin implements ActionPlugin {

    @Override
    public List<Class<? extends RestHandler>> getRestHandlers() {
        return Collections.singletonList(DiskKeeperAction.class);
    }
}
