package org.gusdb.wdk.cache;

import java.util.Map;

import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;

/**
 * This class manages WDK's subclasses of ItemCache.  For now it will simply
 * be a grouping, but eventually (TODO) we would like to enable, configure, and
 * monitor the caches, at least on app startup but perhaps dynamically.
 * 
 * @author rdoherty
 */
public class InMemoryCaches {

  private static InMemoryCaches _instance = new InMemoryCaches();

  public static InMemoryCaches get() {
    return _instance;
  }

  private InMemoryCaches() { }

  private final FilterSizeCache _filterSizeCache = new FilterSizeCache();
  public FilterSizeCache getFilterSizeCache() { return _filterSizeCache; }

  private final ItemCache<String, EnumParamVocabInstance> _vocabCache = new ItemCache<>();
  public ItemCache<String, EnumParamVocabInstance> getVocabCache() { return _vocabCache; }

  private final ItemCache<String, Map<String, Map<String, String>>> _metadataCache = new ItemCache<>();
  public ItemCache<String, Map<String, Map<String, String>>> getMetadataCache() { return _metadataCache; }

  private final ItemCache<String, Map<String, Map<String, String>>> _metadataSpecCache = new ItemCache<>();
  public ItemCache<String, Map<String, Map<String, String>>> getMetadataSpecCache() { return _metadataSpecCache; }

}
