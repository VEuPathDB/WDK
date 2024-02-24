package org.gusdb.wdk.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

/**
 * Manages a map of user-scoped short-term information.  Traditionally,
 * this data was stored in the user's session object; instead, we store
 * it now in a userId-keyed map, whose values time out some duration
 * after last access (currently 60 minutes).  If instance() is called
 * within the application, shutDown() should also be called to clean
 * up the expiration thread threadpool.
 */
public class TemporaryUserDataStore {

  private static final Logger LOG = Logger.getLogger(TemporaryUserDataStore.class);

  public static class TemporaryUserData extends ConcurrentHashMap<String,Object> {

    private final TemporaryUserDataStore _parent;
    private final Long _owner;

    private TemporaryUserData(TemporaryUserDataStore parent, Long owner) {
      _parent = parent;
      _owner = owner;
    }

    public void invalidate() {
      clear();
      _parent.remove(_owner);
    }

  }

  // singleton pattern
  private static TemporaryUserDataStore _instance;

  public static synchronized TemporaryUserDataStore instance() {
    return _instance == null ? (_instance = new TemporaryUserDataStore()) : _instance;
  }

  public static void shutDown() {
    if (_instance != null)
      _instance._threadPool.shutdown();
    _instance = null;
  }

  private static final RemovalListener<Long,Map<String,Object>> LISTENER =
      (k,v,cause) -> LOG.info("User " + k + "'s temporary user data store has expired with " + v.size() + " entries; Reason: " + cause);

  private final ExecutorService _threadPool;
  private final Cache<Long,TemporaryUserData> _data;

  private TemporaryUserDataStore() {
    _threadPool = Executors.newCachedThreadPool();
    _data = Caffeine.newBuilder()
        .executor(_threadPool)
        .recordStats()
        .removalListener(LISTENER)
        .expireAfterAccess(60, TimeUnit.MINUTES)
        .build();
  }

  public TemporaryUserData get(Long userId) {
    return _data.get(userId, id -> new TemporaryUserData(this, id));
  }

  public void remove(Long userId) {
    _data.invalidate(userId);
  }

  public CacheStats getStats() {
    return _data.stats();
  }

}
