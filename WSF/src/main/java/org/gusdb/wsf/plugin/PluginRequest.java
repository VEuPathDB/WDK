package org.gusdb.wsf.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wsf.common.WsfRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 */
public class PluginRequest implements WsfRequest {

  public static final String PROJECT_KEY = "project";
  public static final String COLUMNS_ARRAY_KEY = "ordered-columns";
  public static final String PARAMETER_MAP_KEY = "parameters";
  public static final String CONTEXT_MAP_KEY = "context";

  private String _projectId;
  private Map<String, String> _params;
  private List<String> _orderedColumns;
  private Map<String, String> _context = new HashMap<>();

  public PluginRequest() {
    this._params = new HashMap<>();
    this._orderedColumns = new ArrayList<>();
    this._context = new HashMap<>();
  }

  public PluginRequest(PluginRequest request) {
    this._projectId = request.getProjectId();
    this._params = new HashMap<>(request.getParams());
    this._orderedColumns = new ArrayList<>(Arrays.asList(request.getOrderedColumns()));
    this._context = new HashMap<>(request.getContext());
  }

  public PluginRequest(String jsonString) throws PluginModelException {
    try {
      parseJSON(new JSONObject(jsonString));
    }
    catch (JSONException ex) {
      throw new PluginModelException(ex);
    }
  }

  public int getChecksum() {
    String content = toString();
    int checksum = 0;
    for (int i = 0; i < content.length(); i++) {
      checksum ^= content.charAt(i);
    }
    return checksum;
  }

  protected JSONObject getJSON() throws JSONException {
    JSONObject jsRequest = new JSONObject();
    jsRequest.put(PROJECT_KEY, getProjectId());

    // output columns
    JSONArray jsColumns = new JSONArray();
    for (String column : getOrderedColumns()) {
      jsColumns.put(column);
    }
    jsRequest.put(COLUMNS_ARRAY_KEY, jsColumns);

    // output params
    JSONObject jsParams = new JSONObject();
    Map<String, String> params = getParams();
    for (String paramName : params.keySet()) {
      jsParams.put(paramName, params.get(paramName));
    }
    jsRequest.put(PARAMETER_MAP_KEY, jsParams);

    // output request context
    JSONObject jsContext = new JSONObject();
    Map<String, String> context = getContext();
    for (String contextKey : context.keySet()) {
      jsContext.put(contextKey, context.get(contextKey));
    }
    jsRequest.put(CONTEXT_MAP_KEY, jsContext);
    return jsRequest;
  }

  @Override
  public String toString() {
    try {
      return getJSON().toString();
    }
    catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void parseJSON(JSONObject jsRequest) throws JSONException {
    if (jsRequest.has(PROJECT_KEY))
      setProjectId(jsRequest.getString(PROJECT_KEY));

    JSONArray jsColumns = jsRequest.getJSONArray(COLUMNS_ARRAY_KEY);
    List<String> columns = new ArrayList<>();
    for (int i = 0; i < jsColumns.length(); i++) {
      columns.add(jsColumns.getString(i));
    }
    setOrderedColumns(columns.toArray(new String[0]));

    Map<String, String> params = new LinkedHashMap<>();
    addToMap(params, jsRequest.getJSONObject(PARAMETER_MAP_KEY));
    setParams(params);

    Map<String, String> context = new LinkedHashMap<>();
    addToMap(context, jsRequest.getJSONObject(CONTEXT_MAP_KEY));
    setContext(context);
  }

  private static void addToMap(Map<String, String> map, JSONObject newValues) throws JSONException {
    for (String key : JsonUtil.getKeys(newValues)) {
      map.put(key, newValues.getString(key));
    }
  }

  /**
   * @return the projectId
   */
  @Override
  public String getProjectId() {
    return _projectId;
  }

  /**
   * @param projectId
   *          the projectId to set
   */
  public void setProjectId(String projectId) {
    this._projectId = projectId;
  }

  /**
   * @return the params
   */
  @Override
  public Map<String, String> getParams() {
    return new HashMap<>(_params);
  }

  /**
   * @param params
   *   the params to set
   */
  public void setParams(Map<String, String> params) {
    this._params = new HashMap<>(params);
  }

  public void putParam(String name, String value) {
    this._params.put(name, value);
  }

  /**
   * @return the orderedColumns
   */
  @Override
  public String[] getOrderedColumns() {
    String[] array = new String[_orderedColumns.size()];
    _orderedColumns.toArray(array);
    return array;
  }

  /**
   * @return a map of ordered columns, where the key is the column name, and the
   *   value is the zero-based order of that column.
   */
  @Override
  public Map<String, Integer> getColumnMap() {
    Map<String, Integer> map = new LinkedHashMap<>();
    for (int i = 0; i < _orderedColumns.size(); i++) {
      map.put(_orderedColumns.get(i), i);
    }
    return map;
  }

  /**
   * @param orderedColumns
   *   the orderedColumns to set
   */
  public void setOrderedColumns(String[] orderedColumns) {
    this._orderedColumns = new ArrayList<>(orderedColumns.length);
    for (String column : orderedColumns) {
      this._orderedColumns.add(column);
    }
  }

  /**
   * The context can be used to hold additional information, such as user id,
   * calling query name, etc, which can be used by plugins.
   *
   * @return the context
   */
  @Override
  public Map<String, String> getContext() {
    return new HashMap<>(_context);
  }

  /**
   * @param context
   *   the context to set
   */
  public void setContext(Map<String, String> context) {
    this._context = new HashMap<>(context);
  }

}
