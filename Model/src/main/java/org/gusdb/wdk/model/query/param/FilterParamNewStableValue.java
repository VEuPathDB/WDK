package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.log4j.Logger;


/**
 *
{
 "filters": [
  {
    "value": {
      "min": 1.82,
      "max": 2.19,
    },
    "field": "age"
    "includeUnknowns": true    (optional)

  },
  {
    "value": [
      "female"
    ],
    "field": "biological sex"
    "includeUnknowns": true    (optional)
  }
]
}
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
    } catch (JSONException e) {
      throw new WdkModelException(e);
    }
   }
  
  public FilterParamNewStableValue(JSONObject stableValueJson, FilterParamNew param) throws WdkModelException {
    _param = param;
    _stableValueJson = stableValueJson;
   }

  
  public List<Filter> getFilters() throws WdkModelException {
     initWithThrow();
     return Collections.unmodifiableList(_filters);
  }
  
  /**
   * validate the syntax and, for semantics, compare the field names and values against ontology and metadata
   * @return err message if any.  null if valid
   */
  public String validateSyntaxAndSemantics() {
    String errmsg = validateSyntax();
    if (errmsg != null) return errmsg;
    return null;
  }
  
  /**
   * validate the syntax.  does not validate semantics (ie, compare against ontology).
   * @return err message if any.  null if valid
   */
  public String validateSyntax() {
    return init();
  }
  
  public String toSignatureString() throws WdkModelException {
    initWithThrow();
    List<String> filterSigs = new ArrayList<String>();
    for (Filter filter : getFilters()) filterSigs.add(filter.getSignature());
    Collections.sort(filterSigs);
    return "[" + FormatUtil.join(filterSigs, ",") + "]";
  }
    
  private void initWithThrow() throws WdkModelException {
    String errmsg = init();
    if (errmsg != null) throw new WdkModelException(errmsg);
  }
  
  /**
   * 
   * @return error message if any; null if valid
   * @throws WdkModelException
   */
  private String init() {
    
    if (_filters == null) {
      _filters = new ArrayList<Filter>();
      try {
        
        JSONArray jsFilters = _stableValueJson.getJSONArray(FILTERS_KEY);
        if (jsFilters == null)
          return "Stable value for parameter " + _param.getFullName() + " missing the array: " + FILTERS_KEY;

        for (int i = 0; i < jsFilters.length(); i++) {
          
          JSONObject jsFilter = jsFilters.getJSONObject(i);
          
          if (!jsFilter.has(FILTERS_INCLUDE_UNKNOWN) && !jsFilter.has(FILTERS_VALUE))
            return "A value filter must have at minimum one of" +
                " the following properties: '" + FILTERS_INCLUDE_UNKNOWN + "', '" +
                FILTERS_VALUE + "'.";

          String field = jsFilter.getString(FILTERS_FIELD);
          if (field == null)
            return "Stable value for parameter " + _param.getFullName() + " does not specify an ontology term";
          
          boolean isRange = inferIsRange(jsFilter);
          OntologyItemType type;
          if (isRange) type = inferRangeType(jsFilter.getJSONObject(FILTERS_VALUE));
          else type = inferMemberType(jsFilter.getJSONArray(FILTERS_VALUE));
 
          Filter filter = null;
          Boolean includeUnknowns = jsFilter.isNull(FILTERS_INCLUDE_UNKNOWN)? false : jsFilter.getBoolean(FILTERS_INCLUDE_UNKNOWN);

          try {
            switch (type) {
              case DATE:
                filter = new DateRangeFilter(jsFilter.getJSONObject(FILTERS_VALUE), includeUnknowns, field);
                break;
              case NUMBER:
                if (isRange)
                  filter = new NumberRangeFilter(jsFilter.getJSONObject(FILTERS_VALUE), includeUnknowns,
                      field);
                else
                  filter = new NumberMembersFilter(jsFilter.getJSONArray(FILTERS_VALUE), includeUnknowns,
                      field);
                break;
              case STRING:
                filter = new StringMembersFilter(jsFilter.getJSONArray(FILTERS_VALUE), includeUnknowns,
                    field);
                break;
              default:
                break;
            }
          } catch (WdkModelException e) {
	    e.printStackTrace();
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
    } catch (JSONException e) {
      isRange = false;
    }
    return isRange;
  }
  
  // temporary hack
  private OntologyItemType inferRangeType(JSONObject jsValue) {
    OntologyItemType type = OntologyItemType.NUMBER;
    try {
      if (!jsValue.isNull(FILTERS_MIN)) jsValue.getDouble(FILTERS_MIN);
      if (!jsValue.isNull(FILTERS_MAX)) jsValue.getDouble(FILTERS_MAX);
    } catch (JSONException e) {
      type = OntologyItemType.DATE;
    }
    return type;
  }
  
  // temporary hack
  private OntologyItemType inferMemberType(JSONArray jsValue) {
    OntologyItemType type = OntologyItemType.NUMBER;
    try {
      if (!jsValue.isNull(0)) jsValue.getDouble(0);
    } catch (JSONException e) {
      type = OntologyItemType.STRING;
    }
    return type;
  }

  /**
   * @param user
   * @param stableValue
   * @param contextParamValues
   * @param param
   * @return
   * @throws WdkModelException
   */
  public String getDisplayValue(User user, Map<String, String> contextParamValues) throws WdkModelException {

    initWithThrow();

    if (_filters.size() == 0) {
      String displayName = ((FilterParamNew) _param).getFilterDataTypeDisplayName();
      return displayName != null ? displayName : _param.getPrompt();
    }

    List<String> filterDisplays = new ArrayList<String>();
    for (Filter filter : _filters) {
      filterDisplays.add(
          filter.getDisplayValue() + (filter.getIncludeUnknowns() ? " (include unknowns)" : ""));
    }
    return FormatUtil.join(filterDisplays, System.lineSeparator());
  }
  
  ////////////////////   inner classes to represent different types of filter  //////////////////////////
  
  public abstract class Filter {
    
    String field = null; // the stable value did not include a value field
    Boolean includeUnknowns = null;
    boolean valueIsNull;
    
    public Filter(boolean valueIsNull, Boolean includeUnknowns, String field) throws WdkModelException {
      this.valueIsNull = valueIsNull;
      this.includeUnknowns = includeUnknowns;
      this.field = field;
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

      String unknownClause =  includeUnknowns? metadataTableName + "." + columnName + " is NULL OR " : "";

      String innerAndClause = valueIsNull ? metadataTableName + "." + columnName + " is not NULL"
          :  getAndClause(columnName, metadataTableName);

      // at least one of `unknownClause` or `innerAndClause` will be non-empty, due to validation check above.
      return whereClause + " AND (" + unknownClause + innerAndClause + ")";
    }
  }
  
  private abstract class RangeFilter extends Filter {

    /**
     * 
     * @param jsValue  the FILTERS_VALUE portion of the stable value
     * @param includeUnknowns
     * @param field
     * @throws WdkModelException
     */
    public RangeFilter(JSONObject jsValue, Boolean includeUnknowns, String field) throws WdkModelException {
      super(jsValue == null, includeUnknowns, field);

      /*
      if (jsValue != null && jsValue.isNull(FILTERS_MAX) && jsValue.isNull(FILTERS_MIN))
        throw new WdkModelException("Stable value for parameter " + _param.getFullName() + " " + FILTERS_FIELD + " " +
            field + "has no " + FILTERS_MIN + " or " + FILTERS_MAX);
      */
    }
        
    public String getDisplayValue() {
      String min = getMinString();
      String max = getMaxString();
      return min == null ? "less than " + max
          : min == null ? "greater than " + min : "between " + min + " and " + max;
    }

    public Boolean getIncludeUnknowns() {return includeUnknowns;}
    
    protected String getAndClause(String columnName, String metadataTableName) throws WdkModelException {
      String minStr = getMinStringSql();
      String maxStr = getMaxStringSql();
 
      String clauseStart = metadataTableName + "." + columnName;
      if (minStr == null) return clauseStart + " <= " + maxStr;
      if (maxStr == null) return clauseStart + " >= " + minStr;
      return clauseStart + " >= " + minStr + " AND " + metadataTableName + "." + columnName + " <= " + maxStr;
    }
    
    String getSignature() { return "" + getMinString() + "," + getMaxString() + "," + includeUnknowns; }
    
    abstract String getMaxString();
    abstract String getMinString();
    abstract String getMaxStringSql();
    abstract String getMinStringSql();
  }

  private class DateRangeFilter extends RangeFilter {
    String min = null;
    String max = null;

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
    
    String getMinString() { return min; }
    String getMaxString() { return max; }
    String getMinStringSql() { return min == null? null : "date '" + min + "'"; }
    String getMaxStringSql() { return max == null? null : "date '" + max + "'"; }
  }
  
  private class NumberRangeFilter extends RangeFilter {
    Double min = null;
    Double max = null;
    
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
    
    String getMinString() { return min == null? null : min.toString(); }
    String getMaxString() { return max == null? null : max.toString(); }
    String getMinStringSql() { return min == null? null : min.toString(); }
    String getMaxStringSql() { return max == null? null : max.toString(); }
  }

  
  private abstract class MembersFilter extends Filter {

    public MembersFilter(JSONArray jsArray, Boolean includeUnknowns, String field)
        throws WdkModelException {

      super(jsArray == null, includeUnknowns, field);
;
      if (jsArray != null) {
        try {
	  setMembers(jsArray);
        }
        catch (JSONException j) {
          throw new WdkModelException(j);
        }
      }     
    }
    
    public Boolean getIncludeUnknowns() {return includeUnknowns;}
    
    abstract void setMembers(JSONArray jsArray) throws JSONException;    
   }
  
  private class NumberMembersFilter extends MembersFilter {
    List<Double> members;

    public NumberMembersFilter(JSONArray jsArray, Boolean includeUnknowns, String field)
        throws WdkModelException {
      super(jsArray, includeUnknowns, field);
    }

    void setMembers(JSONArray jsArray) throws JSONException {
      members = new ArrayList<Double>();
      for (int i = 0; i < jsArray.length(); i++) jsArray.getDouble(i);
    }
    
    public String getDisplayValue() {
      Collections.sort(members);
      return  FormatUtil.join(members, ",");
    }
    
    protected String getAndClause(String columnName, String metadataTableName) {
      if (members.size() == 0) return "1 != 1";
      return metadataTableName + "." + columnName + " IN (" + FormatUtil.join(members, ", ") + ") ";
    }
    
    String getSignature() {
      List<Double> list = new ArrayList<Double>(members);
      Collections.sort(list);
      return  FormatUtil.join(list, ",") + " --" + includeUnknowns;
    }
  }
  
  private class StringMembersFilter extends MembersFilter {
    List<String> members;

    public StringMembersFilter(JSONArray jsArray, Boolean includeUnknowns, String field)
        throws WdkModelException {
      super(jsArray, includeUnknowns, field);
    }

    void setMembers(JSONArray jsArray) throws JSONException {
      members = new ArrayList<String>();
      for (int i = 0; i < jsArray.length(); i++) members.add(jsArray.getString(i));
    }
    
    public String getDisplayValue() {
      return  FormatUtil.join(members, ",");
    }
    
    protected String getAndClause(String columnName, String metadataTableName) {
      if (members.size() == 0) return "1 != 1";
      return metadataTableName + "." + columnName + " IN ('" + FormatUtil.join(members, "', '") + "') ";
    }
    
    String getSignature() {
      List<String> list = new ArrayList<String>(members);
      Collections.sort(list);
      return  FormatUtil.join(list, ",") + " --" + includeUnknowns;
    }

  }

}
