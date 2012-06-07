package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class StepCache implements Runnable {

    /**
     * The interval, in milliseconds, of the purge thread.
     */
    private static final long INTERVAL = 1000;

    /**
     * The maximum life, in milliseconds, of a cached step since last access.
     */
    private static final long MAX_LIFE = 1000 * 60 * 1;

    private static final Logger logger = Logger.getLogger(StepCache.class);

    private static class StepKey {
        private final int userId;
        private final int displayId;

        public StepKey(int userId, int displayId) {
            this.userId = userId;
            this.displayId = displayId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return userId ^ displayId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof StepKey) {
                StepKey key = (StepKey) obj;
                return (key.userId == userId && key.displayId == displayId);
            } else return false;
        }
    }

    private static class StepNode {

        private final Step step;
        private long lastAccessed;

        public StepNode(Step step) {
            super();
            this.step = step;
        }

        /**
         * @return the step
         */
        public Step getStep() {
            return step;
        }

        /**
         * @return the lastAccessed
         */
        public long getLastAccessed() {
            return lastAccessed;
        }

        /**
         * @param lastAccessed
         *            the lastAccessed to set
         */
        public void setLastAccessed(long lastAccessed) {
            this.lastAccessed = lastAccessed;
        }
    }

    private final Map<StepKey, StepNode> steps;

    private boolean running;

    public StepCache() {
        this.steps = new HashMap<StepKey, StepNode>();
    }

    public synchronized void stop() {
        running = false;
    }

    public synchronized Step getStep(int userId, int displayId) {
        StepKey key = new StepKey(userId, displayId);
        StepNode node = steps.get(key);
        if (node == null) return null;
        node.setLastAccessed(System.currentTimeMillis());

        logger.debug("Step #" + displayId + " of user #" + userId
                + " retrieved from cache.");
        return node.getStep();
    }

    public synchronized void addStep(Step step) {
        User user = step.getUser();
        StepKey key = new StepKey(user.getUserId(), step.getDisplayId());
        StepNode node = new StepNode(step);
        node.setLastAccessed(System.currentTimeMillis());
        steps.put(key, node);
    }

    public synchronized void removeStep(int userId, int displayId) {
        steps.remove(new StepKey(userId, displayId));
    }

    public synchronized void removeSteps(int userId) {
        List<StepKey> keys = new ArrayList<StepKey>();
        for (StepKey key : steps.keySet()) {
            StepNode node = steps.get(key);
            if (node.getStep().getUser().getUserId() == userId) keys.add(key);
        }
        for (StepKey key : keys) {
            steps.remove(key);
        }
    }

    public void run() {
        while (running) {
            purgeCache();
            try {
                Thread.sleep(INTERVAL);
            }
            catch (InterruptedException e) {}
        }
    }

    private synchronized void purgeCache() {
        List<StepKey> keys = new ArrayList<StepKey>();
        long threshold = System.currentTimeMillis() - MAX_LIFE;
        // find expired steps
        for (StepKey key : steps.keySet()) {
            StepNode node = steps.get(key);
            if (node.getLastAccessed() < threshold) keys.add(key);
        }
        // remove expired steps
        logger.debug(keys.size() + " steps expired.");
        for (StepKey key : keys) {
            steps.remove(key);
        }
    }
}
