package org.gusdb.wdk.model.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.test.sanity.OptionallyTestable;

/**
 * An object representation of the {@code <querySet>} in the model. A query set
 * can only contain queries.
 * You can define isCacheable on querySet to provide default value to the contained queries.
 * 
 * @author jerric
 */
public class QuerySet extends WdkModelBase implements ModelSetI<Query>, OptionallyTestable {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(Query.class);

  private List<Query> _queryList = new ArrayList<Query>();
  private Map<String, Query> _queries = new LinkedHashMap<String, Query>();
  private String _name;
  private List<PostCacheUpdateSql> _postCacheUpdateSqls = new ArrayList<PostCacheUpdateSql>();
  private boolean _cacheable = false;

  /* for sanity testing */
  public static enum QueryType {
    VOCAB,        // sanity testable
    ATTRIBUTE,    // sanity testable
    TABLE,        // sanity testable
    ID,           // not sanity testable
    SUMMARY,      // not sanity testable
    TRANSFORM,    // not sanity testable
    UNSPECIFIED,  // default value if not set in model; not sanity testable
    TABLE_TOTAL;  // special type used only by sanity tester; not a valid query type
  }
  private QueryType _queryType = QueryType.UNSPECIFIED;
  private boolean _doNotTest = false;
  private List<ParamValuesSet> _unexcludedDefaultParamValuesSets = new ArrayList<ParamValuesSet>();
  private ParamValuesSet _defaultParamValuesSet;
  private List<WdkModelText> _unexcludedTestRowCountSqls = new ArrayList<WdkModelText>();
  private String _testRowCountSql;
  /* end sanity testing fields */

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

  /**
   * @return the default cacheable flag for the contained SqlQuery. If the
   *         contained SqlQuery doesn't define its own flag, this value will be
   *         used. This flag won't affect ProcessQuery, since it is always
   *         cached.
   */
  public boolean isCacheable() {
    return _cacheable;
  }

  public void setCacheable(boolean cacheable) {
    _cacheable = cacheable;
  }

  /**
   * This method is to match with the property of the xml
   * @param cacheable
   */
  public void setIsCacheable(boolean cacheable) {
    _cacheable = cacheable;
  }

  public Query getQuery(String name) throws WdkModelException {
    Query q = _queries.get(name);
    if (q == null)
      throw new WdkModelException("Query Set " + getName()
          + " does not include query " + name);
    return q;
  }

  @Override
  public Query getElement(String name) {
    return _queries.get(name);
  }

  public Query[] getQueries() {
    Query[] array = new Query[_queries.size()];
    _queries.values().toArray(array);
    return array;
  }

  public void setQueryType(String type) {
    QueryType queryTypeLocal = QueryType.valueOf(type.toUpperCase());
    if (queryTypeLocal.equals(QueryType.TABLE_TOTAL)) {
      throw new IllegalArgumentException("QueryType TABLE_TOTAL " +
          "is an internal value and cannot be specified in the model");
    }
    _queryType = queryTypeLocal;
  }

  // needed for digester to recognize bean property
  public String getQueryType() {
    return _queryType.name();
  }

  // proper method to use to retrive query type
  public QueryType getQueryTypeEnum() {
    return _queryType;
  }

  public List<PostCacheUpdateSql> getPostCacheUpdateSqls() {
    return Collections.unmodifiableList(_postCacheUpdateSqls);
  }

  public void addPostCacheUpdateSql(PostCacheUpdateSql postCacheUpdateSql) {
    _postCacheUpdateSqls.add(postCacheUpdateSql);
  }

  public void setDoNotTest(boolean doNotTest) {
    _doNotTest = doNotTest;
  }

  @Override
  public boolean getDoNotTest() {
    return _doNotTest;
  }

  public void addDefaultParamValuesSet(ParamValuesSet paramValuesSet) {
    _unexcludedDefaultParamValuesSets.add(paramValuesSet);
  }

  public ParamValuesSet getDefaultParamValuesSet() {
    return _defaultParamValuesSet;
  }

  // sql that returns number of rows expected by all queries in this query set
  public void addTestRowCountSql(WdkModelText text) {
    _unexcludedTestRowCountSqls.add(text);
  }

  public String getTestRowCountSql() {
    return _testRowCountSql;
  }

  public boolean contains(String queryName) {
    return _queries.containsKey(queryName);
  }

  public void addQuery(Query query) {
    query.setQuerySet(this);
    if (_queryList != null)
      _queryList.add(query);
    else {
      // if (queries.containsKey(query.getName()))
      // throw new WdkModelException("query [" + query.getFullName()
      // + "] already exists in the set.");
      _queries.put(query.getName(), query);
    }
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);
    for (Query query : _queries.values()) {
      query.resolveReferences(model);
    }

    for (PostCacheUpdateSql postCacheUpdateSql : _postCacheUpdateSqls)
      if (postCacheUpdateSql != null && (postCacheUpdateSql.getSql() == null ||
          !postCacheUpdateSql.getSql().contains(Utilities.MACRO_CACHE_TABLE) ||
          !postCacheUpdateSql.getSql().contains(Utilities.MACRO_CACHE_INSTANCE_ID)))
        throw new WdkModelException(
            "Invalid PostCacheUpdateSql. <sql> must be provided, and include the macros: " +
                Utilities.MACRO_CACHE_TABLE + " and " + Utilities.MACRO_CACHE_INSTANCE_ID);
  }

  /*
   * (non-Javadoc) do nothing
   * 
   * @see
   * org.gusdb.wdk.model.ModelSetI#setResources(org.gusdb.wdk.model.WdkModel)
   */
  @Override
  public void setResources(WdkModel model) throws WdkModelException {}

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer("QuerySet: name='" + _name + "'");
    buf.append(newline);
    for (Query query : _queries.values()) {
      buf.append(newline);
      buf.append(":::::::::::::::::::::::::::::::::::::::::::::");
      buf.append(newline);
      buf.append(query);
      buf.append("----------------");
      buf.append(newline);
    }
    return buf.toString();
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude queries
    for (Query query : _queryList) {
      if (query.include(projectId)) {
        query.excludeResources(projectId);
        String queryName = query.getName();
        if (_queries.containsKey(queryName))
          throw new WdkModelException("Query named " + queryName
              + " already exists in query set " + getName());
        _queries.put(queryName, query);
      }
    }
    _queryList = null;

    // exclude paramValuesSets
    for (ParamValuesSet paramValuesSet : _unexcludedDefaultParamValuesSets) {
      if (paramValuesSet.include(projectId)) {
        if (_defaultParamValuesSet != null)
          throw new WdkModelException(
              "Duplicate <defaultTestParamValues> included in query set "
                  + getName() + " for projectId " + projectId);
        _defaultParamValuesSet = paramValuesSet;

      }
    }

    // exclude textRowCountSqls
    for (WdkModelText text : _unexcludedTestRowCountSqls) {
      if (text.include(projectId)) {
        if (_testRowCountSql != null)
          throw new WdkModelException(
              "Duplicate <testRowCountSql> included in query set " + getName()
                  + " for projectId " + projectId);
        _testRowCountSql = text.getText();

      }
    }

    // exclude postCacheUpdateSqls
    _postCacheUpdateSqls = _postCacheUpdateSqls.stream()
        .filter(sql -> sql.include(projectId))
        .collect(Collectors.toList());

  }

  // ///////////////////////////////////////////////////////////////
  // ///// protected
  // ///////////////////////////////////////////////////////////////

}
