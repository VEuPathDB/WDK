package org.gusdb.wdk.cache;

import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.query.param.FilterParam.MetadataCache;
import org.gusdb.wdk.model.query.param.FilterParamNew.MetadataNewCache;
import org.gusdb.wdk.model.query.param.FilterParamNew.OntologyCache;

/**
 * This class manages WDK's subclasses of ItemCache.  For now it will simply
 * be a grouping, but eventually (TODO) we would like to enable, configure, and
 * monitor the caches, at least on app startup but perhaps dynamically.
 * 
 * @author rdoherty
 */
public class CacheMgr {

  private static CacheMgr _instance = new CacheMgr();

  public static CacheMgr get() {
    return _instance;
  }

  private final FilterSizeCache _filterSizeCache = new FilterSizeCache();
  private final StepCache _stepCache = new StepCache();
  private final ItemCache<String, EnumParamVocabInstance> _vocabCache = new ItemCache<>();
  private final MetadataCache _metadataCache = new MetadataCache();
  private final MetadataCache _metadataSpecCache = new MetadataCache();
  private final MetadataNewCache _metadataNewCache = new MetadataNewCache();
  private final OntologyCache _ontologyCache = new OntologyCache();


  private CacheMgr() { }

  public FilterSizeCache getFilterSizeCache() { return _filterSizeCache; }
  public StepCache getStepCache() { return _stepCache; }
  public ItemCache<String, EnumParamVocabInstance> getVocabCache() { return _vocabCache; }
  public MetadataCache getMetadataCache() { return _metadataCache; }
  public MetadataCache getOntologyCache() { return _metadataSpecCache; }
  public MetadataNewCache getMetadataNewCache() { return _metadataNewCache; }
  public OntologyCache getOntologyNewCache() { return _ontologyCache; }

}
