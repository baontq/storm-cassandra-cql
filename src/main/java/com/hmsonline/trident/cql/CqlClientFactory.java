package com.hmsonline.trident.cql;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class CqlClientFactory implements Serializable {

    private static final long serialVersionUID = 2L;
    private static final Logger LOG = LoggerFactory.getLogger(CqlClientFactory.class);
    private Map<String, Session> sessions = new HashMap<String, Session>();
    private Session defaultSession = null;
    private Cluster cluster = null;

    abstract Cluster.Builder getClusterBuilder();

    public synchronized Session getSession(String keyspace) {
        Session session = sessions.get(keyspace);
        if (session == null) {
            LOG.debug("Constructing session for keyspace [" + keyspace + "]");
            session = getCluster().connect(keyspace);
            sessions.put(keyspace, session);
        }
        return session;
    }

    public synchronized Session getSession() {
        if (defaultSession == null) {
            defaultSession = getCluster().connect();
        }
        return defaultSession;
    }

    protected Cluster getCluster() {
        if (cluster == null || cluster.isClosed()) {
            if (cluster != null && cluster.isClosed()){
                LOG.warn("Cluster closed, reconstructing cluster for [{}]", cluster.getClusterName());
            }

            cluster = getClusterBuilder().build();

            if (cluster == null) {
                throw new RuntimeException("Critical error: cluster is null after building.");
            }
        }

        return cluster;
    }
}
