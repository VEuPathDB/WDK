package org.gusdb.wdk.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.cache.ManagedMap;
import org.gusdb.fgputil.db.cache.SqlCountCache;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamNewCache;
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
  private final InMemoryCache<String, EnumParamVocabInstance> _vocabCache = new InMemoryCache<>();

  private final InMemoryCache<String, List<Map<String,Object>>> _attributeMetaQueryCache = new InMemoryCache<>();
  private final MetadataNewCache _metadataNewCache = new MetadataNewCache();
  private final OntologyCache _ontologyCache = new OntologyCache();
  private final FilterParamNewCache _filterParamNewCache = new FilterParamNewCache();
  private final ManagedMap<String, AnswerRequest> _answerRequestCache = new ManagedMap<>();

  private final Map<String,InMemoryCache<?,?>> _cacheRepo =
      new MapBuilder<String,InMemoryCache<?,?>>(new LinkedHashMap<String,InMemoryCache<?,?>>())
      .put("Filter Size Cache", _filterSizeCache.getCache())
      .put("Vocab Instance Cache", _vocabCache)
      .put("Dynamic Attribute Cache", _attributeMetaQueryCache)
      .put("FilterParamNew Metadata Cache", _metadataNewCache)
      .put("FilterParamNew Ontology Cache", _filterParamNewCache)
      .put("FilterParamNew Cache", _filterParamNewCache)
      .put("AnswerRequest Cache", _answerRequestCache)
      .toMap();

  private CacheMgr() { }

  public FilterSizeCache getFilterSizeCache() { return _filterSizeCache; }
  public InMemoryCache<String, EnumParamVocabInstance> getVocabCache() { return _vocabCache; }
  public InMemoryCache<String, List<Map<String,Object>>> getAttributeMetaQueryCache() { return _attributeMetaQueryCache; }
  public MetadataNewCache getMetadataNewCache() { return _metadataNewCache; }
  public OntologyCache getOntologyNewCache() { return _ontologyCache; }
  public FilterParamNewCache getFilterParamNewCache() { return _filterParamNewCache; }
  public ManagedMap<String, AnswerRequest> getAnswerRequestCache() { return _answerRequestCache; }

  // special getter lazily populates the repo with db-specific count caches
  public synchronized SqlCountCache getSqlCountCache(DatabaseInstance db) {
    String key = "Sql Count Cache - " + db.getIdentifier();
    if (!_cacheRepo.containsKey(key)) {
      _cacheRepo.put(key, new SqlCountCache(db.getDataSource()));
    }
    return (SqlCountCache)_cacheRepo.get(key);
  }

  public synchronized Map<String,InMemoryCache<?,?>> getAllCaches() {
    return new LinkedHashMap<>(_cacheRepo);
  }
}
