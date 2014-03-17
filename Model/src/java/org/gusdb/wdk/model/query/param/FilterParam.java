/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 * 
 *         The filter param is similar to FlatVocabParam in many aspects, but the most important difference
 *         between these two are that the stable value of filter param is a JSON string with values for the
 *         metadata and terms for the param itself, but the stable value of a flatVocabParam is just a string
 *         representation of comma separated list of terms.
 *         
 *         raw value: a JSON string of both term list and metadata list.
 *         
 *         stable value: same as raw value, a JSON string of both term list and metadata list.
 *         
 *         signature: a checksum of the JSON string, but values are sorted
 *         
 *         internal: the internal value of the param, same as the flatVocabParam aspect of the param. 
 * 
 */
public class FilterParam extends FlatVocabParam {

  private static final String COLUMN_PROPERTY = "property";
  private static final String COLUMN_VALUE = "value";

  private static final String COLUMN_SPEC_PROPERTY = "spec_property";
  private static final String COLUMN_SPEC_VALUE = "spec_value";

  private String metadataQueryRef;
  private Query metadataQuery;

  private String metadataSpecQueryRef;
  private Query metadataSpecQuery;

  /**
   * 
   */
  public FilterParam() {
    super();

    // register handlers
    setHandler(new FilterParamHandler());
  }

  /**
   * @param param
   */
  public FilterParam(FilterParam param) {
    super(param);
    this.metadataQueryRef = param.metadataQueryRef;
    if (param.metadataQuery != null)
      this.metadataQuery = param.metadataQuery.clone();
    this.metadataSpecQueryRef = param.metadataSpecQueryRef;
    if (param.metadataSpecQuery != null)
      this.metadataSpecQuery = param.metadataSpecQuery.clone();
  }

  /**
   * @return the propertyQueryName
   */
  public String getMetadataQueryRef() {
    return metadataQueryRef;
  }

  /**
   * @param metadataQueryRef
   *          the propertyQueryName to set
   */
  public void setMetadataQueryRef(String metadataQueryRef) {
    this.metadataQueryRef = metadataQueryRef;
  }

  /**
   * @return the propertyQuery
   */
  public Query getMetadataQuery() {
    return metadataQuery;
  }

  /**
   * @param propertyQuery
   *          the propertyQuery to set
   */
  public void setMetadataQuery(Query metadataQuery) {
    this.metadataQuery = metadataQuery;
  }

  /**
   * @return the metadataSpec Query Name
   */
  public String getMetadataSpecQueryRef() {
    return metadataSpecQueryRef;
  }

  /**
   * @param metadataSpecQueryRef
   *          the metadataQueryName to set
   */
  public void setMetadataSpecQueryRef(String metadataSpecQueryRef) {
    this.metadataSpecQueryRef = metadataSpecQueryRef;
  }

  /**
   * @return the metadataQuery
   */
  public Query getMetadataSpecQuery() {
    return metadataSpecQuery;
  }

  /**
   * @param metadataSpecQuery
   *          the metadataQuery to set
   */
  public void setMetadataSpecQuery(Query metadataSpecQuery) {
    this.metadataSpecQuery = metadataSpecQuery;
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    // resolve property query, the property query should have the same params as vocab query
    if (metadataQueryRef != null) {
      this.metadataQuery = resolveQuery(model, metadataQueryRef, "property query");
      // the propertyQuery must have exactly 3 columns: term, property, and value.
      Map<String, Column> columns = metadataQuery.getColumnMap();
      if (columns.size() != 3 || !columns.containsKey(COLUMN_TERM) || !columns.containsKey(COLUMN_PROPERTY) ||
          !columns.containsKey(COLUMN_VALUE))
        throw new WdkModelException("The propertyQuery " + metadataQueryRef + " in flatVocabParam " +
            getFullName() + " must have exactly three columns: " + COLUMN_TERM + ", " + COLUMN_PROPERTY +
            ", and " + COLUMN_VALUE + ".");
    }

    // resolve metadata query, which should not have any param
    if (metadataSpecQueryRef != null) {
      // no need to clone metadata query, since it's not overriden in any way here.
      this.metadataSpecQuery = (Query) model.resolveReference(metadataSpecQueryRef);
      // make sure metadata query don't have any params
      Param[] params = metadataSpecQuery.getParams();
      if (params.length > 2 || (params.length == 0 && !params[0].getName().equals(Utilities.PARAM_USER_ID)))
        throw new WdkModelException("The metadata query " + metadataSpecQueryRef + " in FlatVocabParam " +
            getFullName() + " cannot have any params.");

      // the metadata query must have exactly 3 columns: property, info, data.
      Map<String, Column> columns = metadataSpecQuery.getColumnMap();
      if (columns.size() != 3 || !columns.containsKey(COLUMN_PROPERTY) ||
          !columns.containsKey(COLUMN_SPEC_PROPERTY) || !columns.containsKey(COLUMN_SPEC_VALUE))
        throw new WdkModelException("The metadataQuery " + metadataSpecQueryRef + " in flatVocabParam " +
            getFullName() + " must have exactly three columns: " + COLUMN_PROPERTY + ", " +
            COLUMN_SPEC_PROPERTY + ", and " + COLUMN_SPEC_VALUE + ".");
    }
  }

  /**
   * @return <propertyName, <infoKey, infoValue>>, or null if metadataQuery is not specified
   * @throws WdkModelException
   * @throws WdkUserException 
   */
  public Map<String, Map<String, String>> getMetadataSpec(User user) throws WdkModelException, WdkUserException {
    if (metadataSpecQuery == null)
      return null;

    // run metadataQuery. By definition, it doesn't have any params
    Map<String, String> params = new HashMap<>();
    QueryInstance instance = metadataSpecQuery.makeInstance(user, params, true, 0,
        new HashMap<String, String>());
    Map<String, Map<String, String>> metadata = new LinkedHashMap<>();
    ResultList resultList = instance.getResults();
    try {
      while (resultList.next()) {
        String property = (String) resultList.get(COLUMN_PROPERTY);
        String info = (String) resultList.get(COLUMN_SPEC_PROPERTY);
        String data = (String) resultList.get(COLUMN_SPEC_VALUE);
        Map<String, String> propertyMeta = metadata.get(property);
        if (propertyMeta == null) {
          propertyMeta = new LinkedHashMap<>();
          metadata.put(property, propertyMeta);
        }
        propertyMeta.put(info, data);
      }
    }
    finally {
      resultList.close();
    }
    return metadata;
  }

  /**
   * @return <term, <propertyName, propertyValue>>, or null if propertyQuery is not specified.
   * @throws WdkModelException
   * @throws WdkUserException 
   */
  public Map<String, Map<String, String>> getMetadata(User user, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    if (metadataQuery == null)
      return null;

    QueryInstance instance = metadataQuery.makeInstance(user, contextValues, true, 0,
        new HashMap<String, String>());
    Map<String, Map<String, String>> properties = new LinkedHashMap<>();
    ResultList resultList = instance.getResults();
    try {
      while (resultList.next()) {
        String term = (String) resultList.get(COLUMN_TERM);
        String property = (String) resultList.get(COLUMN_PROPERTY);
        String value = (String) resultList.get(COLUMN_VALUE);
        Map<String, String> termProp = properties.get(term);
        if (termProp == null) {
          termProp = new LinkedHashMap<>();
          properties.put(term, termProp);
        }
        termProp.put(property, value);
      }
    }
    finally {
      resultList.close();
    }
    return properties;
  }

  @Override
  public JSONObject getJsonValues(User user, Map<String, String> contextValues, EnumParamCache cache)
      throws WdkModelException, WdkUserException {
    JSONObject jsParam = super.getJsonValues(user, contextValues, cache);
    try { // add additional info into the json
      appendJsonFilterValue(jsParam, user, contextValues);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsParam;
  }

  private void appendJsonFilterValue(JSONObject jsParam, User user, Map<String, String> contextValues)
      throws JSONException, WdkModelException, WdkUserException {
    if (metadataSpecQuery == null)
      return;

    // create json for the metadata
    JSONObject jsMetadataSpec = new JSONObject();
    Map<String, Map<String, String>> metadataSpec = getMetadataSpec(user);
    for (String property : metadataSpec.keySet()) {
      JSONObject jsSpec = new JSONObject();
      Map<String, String> spec = metadataSpec.get(property);
      for (String specProp : spec.keySet()) {
        jsSpec.put(specProp, spec.get(specProp));
      }
      jsMetadataSpec.put(property, jsSpec);
    }
    jsParam.put("metadataSpec", jsMetadataSpec);

    // create json for the properties
    JSONObject jsMetadata = new JSONObject();
    Map<String, Map<String, String>> metadata = getMetadata(user, contextValues);
    for (String term : metadata.keySet()) {
      JSONObject jsProperty = new JSONObject();
      Map<String, String> property = metadata.get(term);
      for (String propName : property.keySet()) {
        jsProperty.put(propName, property.get(propName));
      }
      jsMetadata.put(term, jsProperty);
    }
    jsParam.put("metadata", jsMetadata);
  }

}
