/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamReference;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * The query in WDK defines how the data is accessed from the resource. There
 * are currently two kinds of query, SQL based query, and web service based
 * query. The query is not exposed to the user, only the question are visible on
 * the web sites as searches.
 * </p>
 * 
 * <p>
 * A Query holds only the definition of query, such as params, SQL template, or
 * information about the web service etc. It can be used to create
 * QueryInstance, which will hold param values, and does the real work of
 * executing a query and retrieve data.
 * </p>
 * 
 * <p>
 * Dependending on how many answerParams a query might have, a query can be
 * called as a normal query (without any answerParam), or a combined query (with
 * one or more answerParams). If a query has exactly one answerParam, it is also
 * called a transform query; in the transform query, the type of the answerParam
 * can be different from the type of the results the query returns. And there is
 * another special kind of combined query, called BooleanQuery, which has
 * exactly two answerParams, and the types of the answerParam are the same as
 * the result of the query.
 * </p>
 * 
 * <p>
 * A query can be used in four contexts in WDK, as ID query, attribute query,
 * table query, and param query. and SqlQuery can be used in all four contexts,
 * but ProcessQuery (web service query) can only be used in ID and param
 * queries.
 * </p>
 * 
 * <p>
 * An ID query is a query referenced by a question, and the parameters for the
 * search (the visual name of the question) are defined in the queries. An ID
 * query should return all the primary key columns of the recordClass type the
 * associated question linked to. The the primary key values returned by ID
 * query should be unique, and cannot have duplicate rows. If duplicate primary
 * key occurs, WDK will fail when joining it with the attribute query. An ID
 * query can have other columns other than the primary key columns, and those
 * columns are usually used for the dynamic attributes.
 * </p>
 * 
 * <p>
 * An attribute query is a query referenced by a recordClass, in the
 * <attributeQueryRef> tag. An attribute query has to be SqlQuery, and it does
 * not normally have params, although you can define an internal wdk user param
 * in some rare case where the content of the result is user-dependent. The
 * attribute query should return all possible records of a given record type,
 * and the records in the result has to be unique, and the attribute query has
 * to return all the primary key columns, although the corresponding
 * ColumnAttributeField is optional for those columns. The attribute query will
 * be used in two contexts, for single records, and for records in an answer.
 * When used in single record, the attribute SQL is wrapped with the primary key
 * values to return only one row for the record. When used in answer, the
 * attribute SQL is used for sorting the result on the columns in the attribute
 * query, and then the paged id SQL will be used to join with the attribute SQL,
 * to return a page of attributes for the records.
 * <p>
 * 
 * <p>
 * An table query is query referenced by recordClass, in the &lt;table&gt; tag.
 * A table query has to be SqlQuery, and it doesn't normally have params,
 * although you can define an internal wdk user param same way as in attribute
 * query. The table query should return the results for all possible records of
 * a given record type, and each record can have zero or more rows in the
 * result. The table query also must return all the primary key columns,
 * although the ColumnAttributeField of those is optional. The table can be used
 * in two contexts, in single record, or in an answer. In a single record, the
 * table query is used in the similar way as attribute query, and it will be
 * wrapped with the primary key values of the record, to get zero or more rows.
 * In the context of an answer, the table SQL can be used to be combined with
 * the paged ID SQL to get a page of the results for the records.
 * </p>
 * 
 * @author Jerric Gao
 * 
 */
public abstract class Query extends WdkModelBase {

  protected static final Logger logger = Logger.getLogger(Query.class);

  private String name;
  protected boolean cached = false;
  /**
   * A flag to check if the cached has been set. if not set, the value from
   * parent querySet will be used.
   */
  private boolean setCache = false;

  // temp list, will be discarded after resolve references
  private List<ParamReference> paramRefList;
  protected Map<String, Param> paramMap;

  // temp list, will be discarded after resolve references
  private List<Column> columnList;
  protected Map<String, Column> columnMap;

  // for sanity testing
  private boolean doNotTest = false;
  private List<ParamValuesSet> paramValuesSets = new ArrayList<ParamValuesSet>();

  protected WdkModel wdkModel;
  private QuerySet querySet;

  private String[] indexColumns;

  private boolean hasWeight;

  private Question contextQuestion;

  // =========================================================================
  // Abstract methods
  // =========================================================================

  protected abstract void appendJSONContent(JSONObject jsQuery, boolean extra)
      throws JSONException;

  public abstract QueryInstance makeInstance(User user,
      Map<String, String> values, boolean validate, int assignedWeight,
      Map<String, String> context) throws WdkModelException;

  @Override
  public abstract Query clone();

  public abstract void resolveQueryReferences(WdkModel wdkModel)
      throws WdkModelException;

  // =========================================================================
  // Constructors
  // =========================================================================

  protected Query() {
    paramRefList = new ArrayList<ParamReference>();
    paramMap = new LinkedHashMap<String, Param>();
    columnList = new ArrayList<Column>();
    columnMap = new LinkedHashMap<String, Column>();
    hasWeight = false;
  }

  /**
   * clone the query object
   * 
   * @param query
   */
  protected Query(Query query) {
    super(query);

    // logger.debug("clone query: " + query.getFullName());
    this.name = query.name;
    this.cached = query.cached;
    this.setCache = query.setCache;
    this.paramMap = new LinkedHashMap<String, Param>();
    this.columnMap = new LinkedHashMap<String, Column>();
    this.wdkModel = query.wdkModel;
    this.querySet = query.querySet;
    this.doNotTest = query.doNotTest;
    this.paramValuesSets = new ArrayList<ParamValuesSet>(query.paramValuesSets);
    this.wdkModel = query.wdkModel;
    this.hasWeight = query.hasWeight;
    this.contextQuestion = query.getContextQuestion();

    // clone columns
    for (String columnName : query.columnMap.keySet()) {
      Column column = new Column(query.columnMap.get(columnName));
      column.setQuery(this);
      columnMap.put(columnName, column);
    }

    // clone params
    for (String paramName : query.paramMap.keySet()) {
      Param param = query.paramMap.get(paramName).clone();
      param.setContextQuery(this);
      paramMap.put(paramName, param);
    }
  }

  /**
   * @return the cached
   */
  public boolean isCached() {
    return this.cached;
  }

  /**
   * @param cached
   *          the cached to set
   */
  public void setIsCacheable(boolean cached) {
    this.cached = cached;
    setCache = true;
  }

  public Question getContextQuestion() {
    return contextQuestion;
  }

  public void setContextQuestion(Question contextQuestion) {
    this.contextQuestion = contextQuestion;
    for (Param param : paramMap.values()) {
      param.setContextQuestion(contextQuestion);
    }
  }

  public void setIndexColumns(String[] indexColumns) {
    this.indexColumns = indexColumns;
  }

  public String[] getIndexColumns() {
    return indexColumns;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the querySet
   */
  public QuerySet getQuerySet() {
    return querySet;
  }

  /**
   * @param querySet
   *          the querySet to set
   */
  public void setQuerySet(QuerySet querySet) {
    this.querySet = querySet;
  }

  public String getFullName() {
    return querySet.getName() + "." + name;
  }

  public void addParamRef(ParamReference paramRef) {
    this.paramRefList.add(paramRef);
  }

  /**
   * Add a param into the query
   * 
   * @param param
   */
  public void addParam(Param param) {
    param.setContextQuery(this);
    paramMap.put(param.getName(), param);
  }

  public Map<String, Param> getParamMap() {
    return new LinkedHashMap<String, Param>(paramMap);
  }

  public Param[] getParams() {
    Param[] array = new Param[paramMap.size()];
    paramMap.values().toArray(array);
    return array;
  }

  public void addColumn(Column column) {
    column.setQuery(this);
    if (columnList != null)
      this.columnList.add(column);
    else
      columnMap.put(column.getName(), column);
  }

  public Map<String, Column> getColumnMap() {
    return new LinkedHashMap<String, Column>(columnMap);
  }

  public Column[] getColumns() {
    Column[] array = new Column[columnMap.size()];
    columnMap.values().toArray(array);
    return array;
  }

  // exclude this query from sanity testing
  public void setDoNotTest(boolean doNotTest) {
    this.doNotTest = doNotTest;
  }

  public boolean getDoNotTest() {
    return doNotTest;
  }

  public void addParamValuesSet(ParamValuesSet paramValuesSet) {
    paramValuesSets.add(paramValuesSet);
  }

  public List<ParamValuesSet> getParamValuesSets() throws WdkModelException {
    updateParamValuesSetsWithDefaults();
    return paramValuesSets;
  }

  @Override
  public WdkModel getWdkModel() {
    return wdkModel;
  }

  public String getChecksum(boolean extra) throws WdkModelException {
    try {
      JSONObject jsQuery = getJSONContent(extra);
      return Utilities.encrypt(jsQuery.toString());
    } catch (JSONException e) {
      throw new WdkModelException("Unable to get JSON content for checksum.", e);
    }
  }

  /**
   * @param extra
   *          if extra is true, then column names are also includes, plus the
   *          extra info from param.
   * @return
   * @throws JSONException
   *           if unable to create JSON object
   */
  private JSONObject getJSONContent(boolean extra) throws JSONException {
    // use JSON to construct the string content
    JSONObject jsQuery = new JSONObject();
    jsQuery.put("name", getFullName());
    jsQuery.put("project", wdkModel.getProjectId());

    // add context question name
    if (contextQuestion != null)
      jsQuery.put("contextQuestion", contextQuestion.getFullName());

    // construct params; ordered by paramName
    String[] paramNames = new String[paramMap.size()];
    paramMap.keySet().toArray(paramNames);
    Arrays.sort(paramNames);

    JSONArray jsParams = new JSONArray();
    for (String paramName : paramNames) {
      Param param = paramMap.get(paramName);
      jsParams.put(param.getJSONContent(extra));
    }
    jsQuery.put("params", jsParams);

    // construct columns; ordered by columnName
    if (extra) {
      String[] columnNames = new String[columnMap.size()];
      columnMap.keySet().toArray(columnNames);
      Arrays.sort(columnNames);

      JSONArray jsColumns = new JSONArray();
      for (String columnName : columnNames) {
        Column column = columnMap.get(columnName);
        jsColumns.put(column.getJSONContent());
      }
      jsQuery.put("columns", jsColumns);
    }

    // append child-specific data
    appendJSONContent(jsQuery, extra);

    return jsQuery;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude paramRefs
    List<ParamReference> paramRefs = new ArrayList<ParamReference>();
    for (ParamReference paramRef : paramRefList) {
      if (paramRef.include(projectId)) {
        paramRef.excludeResources(projectId);
        paramRefs.add(paramRef);
      }
    }
    paramRefList = paramRefs;

    // exclude columns
    for (Column column : columnList) {
      if (column.include(projectId)) {
        column.excludeResources(projectId);
        String columnName = column.getName();
        if (columnMap.containsKey(columnName)) {
          throw new WdkModelException("The column '" + columnName
              + "' is duplicated in query " + getFullName());
        } else
          columnMap.put(columnName, column);
      }
    }
    columnList = null;

    // exclude paramValuesSets
    List<ParamValuesSet> tempList = new ArrayList<ParamValuesSet>();
    for (ParamValuesSet paramValuesSet : paramValuesSets) {
      if (paramValuesSet.include(projectId)) {
        tempList.add(paramValuesSet);
      }
    }
    paramValuesSets = tempList;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // logger.debug("Resolving " + getFullName() + " - " + resolved);
    if (resolved)
      return;

    this.wdkModel = wdkModel;

    // check if we need to use querySet's cache flag
    if (!setCache)
      cached = getQuerySet().isCacheable();

    // resolve the params
    for (ParamReference paramRef : paramRefList) {
      Param param = ParamReference.resolveReference(wdkModel, paramRef,
          getFullName());
      String paramName = param.getName();
      if (paramMap.containsKey(paramName)) {
        throw new WdkModelException("The param '" + paramName
            + "' is duplicated in query " + getFullName());
      } else {
        param.setContextQuery(this);
        paramMap.put(paramName, param);
      }
    }
    paramRefList = null;

    // FIXME - this cause problems with some params, need to investigate.
    // comment out temporarily
    // apply the default values to depended params
    // Map<String, String> valueStub = new LinkedHashMap<String, String>();
    // resolveDependedParams(valueStub)

    // resolve columns
    for (Column column : columnMap.values()) {
      String sortingColumn = column.getSortingColumn();
      if (sortingColumn == null)
        continue;
      if (!columnMap.containsKey(sortingColumn))
        throw new WdkModelException("Query [" + getFullName()
            + "] has a column [" + column.getName() + "] with sortingColumn ["
            + sortingColumn + "], but the sorting column doesn't exist in "
            + "the same query.");
    }

    // if the query is a transform, it has to return weight column.
    // this applies to both explicit transform and filter queries.
    if (isTransform()) {
      if (!columnMap.containsKey(Utilities.COLUMN_WEIGHT))
        throw new WdkModelException("Transform query [" + getFullName()
            + "] doesn't define the required " + Utilities.COLUMN_WEIGHT
            + " column.");
    }

    resolveQueryReferences(wdkModel);
    resolved = true;
  }

  private void updateParamValuesSetsWithDefaults() throws WdkModelException {
    ParamValuesSet querySetDefaults = querySet.getDefaultParamValuesSet();
    if (paramValuesSets.isEmpty()) {
      paramValuesSets.add(new ParamValuesSet());
    }
    for (ParamValuesSet paramValuesSet : paramValuesSets) {
      paramValuesSet.updateWithDefaults(querySetDefaults);

      for (Param param : getParams()) {
        String paramName = param.getName();
        String defaultValue = param.getDefault();
        paramValuesSet.updateWithDefault(paramName, defaultValue);
      }
    }
  }

  /**
   * @return the combined
   */
  public boolean isCombined() {
    return (getAnswerParamCount() > 0);
  }

  public boolean isBoolean() {
    return (this instanceof BooleanQuery);
  }

  public boolean isTransform() {
    return (getAnswerParamCount() == 1);
  }

  public int getAnswerParamCount() {
    int count = 0;
    for (Param param : paramMap.values()) {
      if (param instanceof AnswerParam)
        count++;
    }
    return count;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer(getFullName());
    buffer.append(": params{");
    boolean firstParam = true;
    for (Param param : paramMap.values()) {
      if (firstParam)
        firstParam = false;
      else
        buffer.append(", ");
      buffer.append(param.getName()).append("[");
      buffer.append(param.getClass().getSimpleName()).append("]");
    }
    buffer.append("} columns{");
    boolean firstColumn = true;
    for (Column column : columnMap.values()) {
      if (firstColumn)
        firstColumn = false;
      else
        buffer.append(", ");
      buffer.append(column.getName());
    }
    buffer.append("}");
    return buffer.toString();
  }

  public Map<String, String> rawOrDependentValuesToDependentValues(User user,
      Map<String, String> rawValues) throws WdkModelException {
    Map<String, String> dependentValues = new LinkedHashMap<String, String>();
    for (String paramName : rawValues.keySet()) {
      Param param = paramMap.get(paramName);
      if (param == null) {
        // instead of throwing an error, wdk will silently ignore it
        // throw new WdkModelException("Invalid param name '" +
        // paramName
        // + "' in query " + getFullName());
        logger.warn("Param " + paramName + " does not exist in query "
            + getFullName());
        continue;
      }
      String rawValue = rawValues.get(paramName);
      String dependentValue = param.rawOrDependentValueToDependentValue(user,
          rawValue);
      dependentValues.put(paramName, dependentValue);
    }
    if (paramMap.containsKey(Utilities.PARAM_USER_ID)) {
      if (!dependentValues.containsKey(Utilities.PARAM_USER_ID))
        dependentValues.put(Utilities.PARAM_USER_ID,
            Integer.toString(user.getUserId()));
    }
    return dependentValues;
  }

  public Map<String, String> dependentValuesToIndependentValues(User user,
      Map<String, String> dependentValues) throws WdkModelException {
    Map<String, String> independentValues = new LinkedHashMap<String, String>();
    for (String paramName : dependentValues.keySet()) {
      Param param = paramMap.get(paramName);
      if (param == null) {
        // instead of throwing an error, wdk will silently ignore it
        // throw new WdkModelException("Invalid param name '" +
        // paramName
        // + "' in query " + getFullName());
        logger.warn("Param " + paramName + " does not exist in query "
            + getFullName());
        continue;
      }
      String dependentValue = dependentValues.get(paramName);
      String independentValue = param.dependentValueToIndependentValue(user,
          dependentValue);
      independentValues.put(paramName, independentValue);
    }
    return independentValues;
  }

  /**
   * @param hasWeight
   *          the hasWeight to set
   */
  public void setHasWeight(boolean hasWeight) {
    this.hasWeight = hasWeight;
  }

  /**
   * @return the hasWeight
   */
  public boolean isHasWeight() {
    return hasWeight;
  }

  /**
   * for reviseStep action, validate all the values, and if it's invalid,
   * substitute it with default. if the value doesn't exist in the map, I will
   * add default into it.
   * 
   * @param contextParamValues
   * @throws WdkModelException
   */
  public void fillContextParamValues(User user,
      Map<String, String> contextParamValues) throws WdkModelException {
    for (Param param : paramMap.values()) {
      if (param instanceof AbstractEnumParam) {
        // for enum/flatVocab params, call a special method to process it
        Map<String, EnumParamCache> caches = new HashMap<>();
        ((AbstractEnumParam) param).fetchCorrectValue(user, contextParamValues,
            caches);
      } else { // for other params, just fill it with default value
        if (!contextParamValues.containsKey(param.getName())) {
          contextParamValues.put(param.getName(), param.getDefault());
        }
      }
    }
  }

}
