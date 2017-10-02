package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {
 *   "filters": [
 *     {
 *       "value": {
 *         "min": 1.82,
 *         "max": 2.19,
 *       },
 *       "field": "age"
 *       "includeUnknowns": true    (optional)
 *     },
 *     {
 *       "value": [
 *         "female"
 *       ],
 *       "field": "biological sex"
 *       "includeUnknowns": true    (optional)
 *     }
 *   ]
 * }
 */
public class FilterParamNewStableValue {

  private static final Logger LOG = Logger.getLogger(FilterParamNewStableValue.class);

  public static final String FILTERS_KEY = "filters";
  public static final String FILTERS_FIELD = "field";
  public static final String FILTERS_VALUE = "value";
  public static final String FILTERS_MIN = "min";
  public static final String FILTERS_MAX = "max";
  public static final String FILTERS_INCLUDE_UNKNOWN = "includeUnknown";
  public static final String FILTERS_IS_RANGE = "isRange";
  public static final String FILTERS_TYPE = "type";

  private FilterParamNew _param;
  private JSONObject _stableValueJson;
  private List<Filter> _filters = null;

  public FilterParamNewStableValue(String stableValueString, FilterParamNew param) throws WdkModelException {
    _param = param;
    try {
      _stableValueJson = new JSONObject(stableValueString);
    }
    catch (JSONException e) {
      throw new WdkModelException(e);
    }
  }

  public FilterParamNewStableValue(JSONObject stableValueJson, FilterParamNew param) {
    _param = param;
    _stableValueJson = stableValueJson;
  }

  public List<Filter> getFilters() throws WdkModelException {
    initWithThrow();
    return Collections.unmodifiableList(_filters);
  }

  /**
   * validate the syntax and, for semantics, compare the field names and values against ontology and metadata
   * 
   * @return err message if any. null if valid
   */
   public String validateSyntaxAndSemantics(User user, Map<String, String> contextParamValues) throws WdkModelException {

    
    // validate syntax
    List<String> errors = new ArrayList<String>();
    String errmsg = validateSyntax();

    if (errmsg != null) errors.add(errmsg);
    
    // validate fields against ontology; collect fields that are not isRange
    Map<String, OntologyItem> ontology = _param.getOntology(user, contextParamValues);
    Set<MembersFilter> memberFilters = new HashSet<MembersFilter>();
    for (Filter filter : _filters) {
      String field = filter.getField();
      if (!ontology.containsKey(field)) {
        errors.add("'" + field + "'" + " is not a recognized category");
        continue;
      }
      if (!ontology.get(field).getIsRange()) memberFilters.add((MembersFilter)filter);
    }
    
    // run metadata query to find distinct values for each member field
    if (memberFilters.size() != 0) {
      Map<String, Map<String, Boolean>> metadataMembers = getDistinctMetaDataMembers(user, contextParamValues,
          memberFilters);

      // iterate through our member filters, validating the values of each
      for (MembersFilter mf : memberFilters) {
        String err = mf.validateValues(metadataMembers.get(mf.getField()));
        if (err != null)
          errors.add(err);
      }
    }

    if (errmsg != null) return errmsg;
    return null;
  }
   
   /**
    * return map of ontology field name to member values (as strings).
    * @param user
    * @param contextParamValues
    * @param memberFilters the set of MembersFilters used in this stable value
    * @return a map from field name -> a map of valid member values -> a don't care value.  (We convert number values to strings)
    * @throws WdkModelException
    */
   private Map<String, Map<String, Boolean>> getDistinctMetaDataMembers(User user,
       Map<String, String> contextParamValues, Set<MembersFilter> memberFilters) throws WdkModelException {
     
     Query metadataQuery = _param.getMetadataQuery();
     String metadataSql;
     try {
       QueryInstance<?> instance = metadataQuery.makeInstance(user, contextParamValues, true, 0,
           new HashMap<String, String>());
       metadataSql = instance.getSql();
     }
     catch (WdkUserException e) {
       throw new WdkModelException(e);
     }

     String metadataTableName = "md";
     String filterSelectSql = "SELECT distinct md.internal FROM (" + metadataSql + ") md";
     return null;
   }


  /**
   * validate the syntax. does not validate semantics (ie, compare against ontology).
   * 
   * @return err message if any. null if valid
   */
  public String validateSyntax() {
    return init();
  }

  public String toSignatureString() throws WdkModelException {
    initWithThrow();
    List<String> filterSigs = new ArrayList<String>();
    for (Filter filter : getFilters()) {
      filterSigs.add(filter.getSignature());
    }
    Collections.sort(filterSigs);
    return "[" + FormatUtil.join(filterSigs, ",") + "]";
  }

  private void initWithThrow() throws WdkModelException {
    String errmsg = init();
    if (errmsg != null) throw new WdkModelException(errmsg);
  }

  /**
   * @return error message if any; null if valid
   */
  private String init() {

    if (_filters == null) {
      _filters = new ArrayList<Filter>();
      try {

        JSONArray jsFilters = _stableValueJson.getJSONArray(FILTERS_KEY);
        if (jsFilters == null) {
          return "Stable value for parameter " + _param.getFullName() + " missing the array: " + FILTERS_KEY;
        }

        for (int i = 0; i < jsFilters.length(); i++) {

          JSONObject jsFilter = jsFilters.getJSONObject(i);

          if (!jsFilter.has(FILTERS_INCLUDE_UNKNOWN) && !jsFilter.has(FILTERS_VALUE)) {
            return "A value filter must have at minimum one of" + " the following properties: '" +
                FILTERS_INCLUDE_UNKNOWN + "', '" + FILTERS_VALUE + "'.";
          }

          String field = jsFilter.getString(FILTERS_FIELD);
          if (field == null) {
            return "Stable value for parameter " + _param.getFullName() + " does not specify an ontology term";
          }

          boolean isRange = inferIsRange(jsFilter);
          OntologyItemType type = (isRange ?
              inferRangeType(jsFilter.getJSONObject(FILTERS_VALUE)) :
              inferMemberType(jsFilter.getJSONArray(FILTERS_VALUE)));

          Filter filter = null;
          Boolean includeUnknowns = jsFilter.isNull(FILTERS_INCLUDE_UNKNOWN) ? false : jsFilter.getBoolean(FILTERS_INCLUDE_UNKNOWN);

          try {
            switch (type) {
              case DATE:
                filter = new DateRangeFilter(jsFilter.getJSONObject(FILTERS_VALUE), includeUnknowns, field);
                break;
              case NUMBER:
                filter = (isRange ?
                    new NumberRangeFilter(jsFilter.getJSONObject(FILTERS_VALUE), includeUnknowns, field) :
                    new NumberMembersFilter(jsFilter.getJSONArray(FILTERS_VALUE), includeUnknowns, field));
                break;
              case STRING:
                filter = new StringMembersFilter(jsFilter.getJSONArray(FILTERS_VALUE), includeUnknowns, field);
                break;
              default:
                throw new WdkModelException("Unsupported filter type: " + type.name());
            }
          }
          catch (WdkModelException e) {
            LOG.error("Could not create filter from JSON value.", e);
            return e.getMessage();
          }

          _filters.add(filter);
        }

      }
      catch (JSONException e) {
        return "Invalid stable value. Can't parse JSON. " + e.getMessage();
      }
    }
    return null;
  }

  // temporary hack
  private boolean inferIsRange(JSONObject jsFilter) {
    boolean isRange = true;
    try {
      if (!jsFilter.isNull(FILTERS_VALUE)) jsFilter.getJSONObject(FILTERS_VALUE);
    }
    catch (JSONException e) {
      isRange = false;
    }
    return isRange;
  }

  // temporary hack
  private OntologyItemType inferRangeType(JSONObject jsValue) {
    OntologyItemType type = OntologyItemType.NUMBER;
    try {
      // use JSONException to expose date type (i.e. not number type)
      if (!jsValue.isNull(FILTERS_MIN)) jsValue.getDouble(FILTERS_MIN);
      if (!jsValue.isNull(FILTERS_MAX)) jsValue.getDouble(FILTERS_MAX);
    }
    catch (JSONException e) {
      type = OntologyItemType.DATE;
    }
    return type;
  }

  // temporary hack
  private OntologyItemType inferMemberType(JSONArray jsValue) {
    OntologyItemType type = OntologyItemType.NUMBER;
    try {
      if (!jsValue.isNull(0)) jsValue.getDouble(0);
    }
    catch (JSONException e) {
      type = OntologyItemType.STRING;
    }
    return type;
  }

  /**
   * @param user  
   * @param contextParamValues 
   */
  public String getDisplayValue(User user, Map<String, String> contextParamValues) throws WdkModelException {

    initWithThrow();

    if (_filters.size() == 0) {
      String displayName = _param.getFilterDataTypeDisplayName();
      return displayName != null ? displayName : _param.getPrompt();
    }

    List<String> filterDisplays = new ArrayList<String>();
    for (Filter filter : _filters) {
      filterDisplays.add(filter.getDisplayValue() + (filter.getIncludeUnknowns() ? " (include unknowns)" : ""));
    }
    return FormatUtil.join(filterDisplays, System.lineSeparator());
  }

  //////////////////// inner classes to represent different types of filter //////////////////////////

  public abstract class Filter {

    String field = null; // the stable value did not include a value field
    Boolean includeUnknowns = null;
    boolean valueIsNull;

    public Filter(boolean valueIsNull, Boolean includeUnknowns, String field) {
      this.valueIsNull = valueIsNull;
      this.includeUnknowns = includeUnknowns;
      this.field = field;
    }

    public String getField() {
      return field;
    }

    abstract String getDisplayValue();

    abstract Boolean getIncludeUnknowns();

    protected abstract String getAndClause(String columnName, String metadataTableName) throws WdkModelException;

    abstract String getSignature();

    // include in where clause a filter by ontology_id
    public String getFilterAsAndClause(String metadataTableName, Map<String, OntologyItem> ontology) throws WdkModelException {

      OntologyItem ontologyItem = ontology.get(field);
      OntologyItemType type = ontologyItem.getType();
      String columnName = type.getMetadataQueryColumn();

      String whereClause = " WHERE " + FilterParamNew.COLUMN_ONTOLOGY_ID + " = '" + ontologyItem.getOntologyId() + "'";

      String unknownClause = includeUnknowns ? metadataTableName + "." + columnName + " is NULL OR " : "";

      String innerAndClause = valueIsNull ?
          metadataTableName + "." + columnName + " is not NULL" :
          getAndClause(columnName, metadataTableName);

      // at least one of `unknownClause` or `innerAndClause` will be non-empty, due to validation check above.
      return whereClause + " AND (" + unknownClause + innerAndClause + ")";
    }
  }

  private abstract class RangeFilter extends Filter {

    /**
     * 
     * @param jsValue
     *          the FILTERS_VALUE portion of the stable value
     * @param includeUnknowns
     * @param field
     */
    public RangeFilter(JSONObject jsValue, Boolean includeUnknowns, String field) {
      super(jsValue == null, includeUnknowns, field);

      /*
       * if (jsValue != null && jsValue.isNull(FILTERS_MAX) && jsValue.isNull(FILTERS_MIN)) throw new
       * WdkModelException("Stable value for parameter " + _param.getFullName() + " " + FILTERS_FIELD + " " +
       * field + "has no " + FILTERS_MIN + " or " + FILTERS_MAX);
       */
    }

    @Override
    public String getDisplayValue() {
      String min = getMinString();
      String max = getMaxString();
      return min == null ?
          (max == null ? "any values" : "less than " + max) :
          (max == null ? "greater than " + min : "between " + min + " and " + max);
    }

    @Override
    public Boolean getIncludeUnknowns() {
      return includeUnknowns;
    }

    @Override
    protected String getAndClause(String columnName, String metadataTableName) throws WdkModelException {

      String minStr = getMinStringSql();
      String maxStr = getMaxStringSql();
      String rowValue = metadataTableName + "." + columnName;

      return "" +
        (minStr == null ? "" : (" AND " + rowValue + " >= " + minStr)) +
        (maxStr == null ? "" : (" AND " + rowValue + " <= " + maxStr));
    }

    @Override
    String getSignature() {
      return "" + getMinString() + "," + getMaxString() + "," + includeUnknowns;
    }

    abstract String getMaxString();

    abstract String getMinString();

    abstract String getMaxStringSql();

    abstract String getMinStringSql();
  }

  private class DateRangeFilter extends RangeFilter {

    private String min = null;
    private String max = null;

    DateRangeFilter(JSONObject jsValue, Boolean includeUnknowns, String field) throws WdkModelException {
      super(jsValue, includeUnknowns, field);
      try {
        if (!jsValue.isNull(FILTERS_MIN)) min = jsValue.getString(FILTERS_MIN);
        if (!jsValue.isNull(FILTERS_MAX)) max = jsValue.getString(FILTERS_MAX);
      }
      catch (JSONException j) {
        throw new WdkModelException(j);
      }
    }

    @Override
    String getMinString() {
      return min;
    }

    @Override
    String getMaxString() {
      return max;
    }

    @Override
    String getMinStringSql() {
      return min == null ? null : "date '" + min + "'";
    }

    @Override
    String getMaxStringSql() {
      return max == null ? null : "date '" + max + "'";
    }
  }

  private class NumberRangeFilter extends RangeFilter {

    private Double min = null;
    private Double max = null;

    NumberRangeFilter(JSONObject jsValue, Boolean includeUnknowns, String field) throws WdkModelException {
      super(jsValue, includeUnknowns, field);
      try {
        if (!jsValue.isNull(FILTERS_MIN)) min = jsValue.getDouble(FILTERS_MIN);
        if (!jsValue.isNull(FILTERS_MAX)) max = jsValue.getDouble(FILTERS_MAX);

      }
      catch (JSONException j) {
        throw new WdkModelException(j);
      }
    }

    @Override
    String getMinString() {
      return min == null ? null : min.toString();
    }

    @Override
    String getMaxString() {
      return max == null ? null : max.toString();
    }

    @Override
    String getMinStringSql() {
      return min == null ? null : min.toString();
    }

    @Override
    String getMaxStringSql() {
      return max == null ? null : max.toString();
    }
  }

  private abstract class MembersFilter extends Filter {

    public MembersFilter(JSONArray jsArray, Boolean includeUnknowns, String field) throws WdkModelException {

      super(jsArray == null, includeUnknowns, field);

      if (jsArray != null) {
        try {
          setMembers(jsArray);
        }
        catch (JSONException j) {
          throw new WdkModelException(j);
        }
      }
    }

    @Override
    public Boolean getIncludeUnknowns() {
      return includeUnknowns;
    }

    /**
     * @return String with list of error values; null if no errors
     */
    public String validateValues(Map<String, Boolean> validValuesMap) {
      List<String> errList = new ArrayList<String>();
      for (String value : getMembersAsStrings()) if (!validValuesMap.containsKey(value)) errList.add(value);
      if (errList.size() != 0) return FormatUtil.join(errList, ", ");
      return null;
    }

   abstract void setMembers(JSONArray jsArray) throws JSONException;
   
   abstract List<String> getMembersAsStrings();

  }

  private class NumberMembersFilter extends MembersFilter {

    private List<Double> members;

    public NumberMembersFilter(JSONArray jsArray, Boolean includeUnknowns, String field) throws WdkModelException {
      super(jsArray, includeUnknowns, field);
    }

    @Override
    void setMembers(JSONArray jsArray) throws JSONException {
      members = new ArrayList<Double>();
      for (int i = 0; i < jsArray.length(); i++) {
        jsArray.getDouble(i);
      }
    }

    @Override
    public String getDisplayValue() {
      Collections.sort(members);
      return FormatUtil.join(members, ",");
    }

    @Override
    protected String getAndClause(String columnName, String metadataTableName) {
      if (members.size() == 0) return "1 != 1";
      return metadataTableName + "." + columnName + " IN (" + FormatUtil.join(members, ", ") + ") ";
    }

    @Override
    String getSignature() {
      List<Double> list = new ArrayList<Double>(members);
      Collections.sort(list);
      return FormatUtil.join(list, ",") + " --" + includeUnknowns;
    }
    
    List<String> getMembersAsStrings() {
      List<String> list = new ArrayList<String>();
      for (Double mem : members) list.add(mem.toString());
      return list;
    }
  }

  private class StringMembersFilter extends MembersFilter {

    private List<String> members;

    public StringMembersFilter(JSONArray jsArray, Boolean includeUnknowns, String field)
        throws WdkModelException {
      super(jsArray, includeUnknowns, field);
    }

    @Override
    void setMembers(JSONArray jsArray) throws JSONException {
      members = new ArrayList<String>();
      for (int i = 0; i < jsArray.length(); i++) {
        members.add(jsArray.getString(i));
      }
    }

    @Override
    public String getDisplayValue() {
      return FormatUtil.join(members, ",");
    }

    @Override
    protected String getAndClause(String columnName, String metadataTableName) {
      if (members.size() == 0) return "1 != 1";
      return metadataTableName + "." + columnName + " IN ('" + FormatUtil.join(members, "', '") + "') ";
    }

    @Override
    String getSignature() {
      List<String> list = new ArrayList<String>(members);
      Collections.sort(list);
      return FormatUtil.join(list, ",") + " --" + includeUnknowns;
    }
    
    List<String> getMembersAsStrings() {
      return members;
    }

  }
}
