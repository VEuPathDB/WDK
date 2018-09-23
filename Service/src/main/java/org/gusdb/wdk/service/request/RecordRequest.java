package org.gusdb.wdk.service.request;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecordRequest {

 /**
   * Input Format:
   * {
   *  "primaryKey": [ { name: String, value: String } ],
   *  "tables": [ String, String ],
   *  "attributes": [ String, String ]
   * }
   * 
   * @param recordClass recordClass for which this request was made
   * @param json request json in the format above
   * @return object representing request
   * @throws RequestMisformatException
 * @throws DataValidationException 
 * @throws WdkModelException 
   */
  public static RecordRequest createFromJson(RecordClass recordClass, JSONObject json)
      throws RequestMisformatException, WdkModelException, DataValidationException {
    try {
      PrimaryKeyValue primaryKey = parsePrimaryKey(json.getJSONArray(JsonKeys.PRIMARY_KEY), recordClass);
      RecordRequest request = new RecordRequest(recordClass, primaryKey);
      request.setAttributeNames(parseAttributeNames(json.getJSONArray(JsonKeys.ATTRIBUTES), recordClass));
      request.setTableNames(parseTableNames(json.getJSONArray(JsonKeys.TABLES), recordClass));
      return request;
    }
    catch (JSONException | WdkUserException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
  }

  public static PrimaryKeyValue parsePrimaryKey(JSONArray primaryKeyJson,
      RecordClass recordClass) throws DataValidationException, WdkModelException {

    String[] columnRefs = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    int providedLength = primaryKeyJson.length();
    int expectedLength = columnRefs.length;

    if (providedLength != expectedLength) {
      throw new DataValidationException("The provided primary key does not have the expected number of parts:\n" +
          "[ provided: " + providedLength + "; expected: " + expectedLength + " ]");
    }

    // validate up front and throw DataValidationException to avoid WdkModelException later
    Map<String,String> pkMap = new LinkedHashMap<>();
    for (int i = 0; i < providedLength; i++) {
      JSONObject part = primaryKeyJson.getJSONObject(i);
      // input order must match PK column order
      String keyName = part.getString("name");
      String keyValue = part.getString("value");
      if (!keyName.equals(columnRefs[i])) {
        throw new DataValidationException("Primary key value array names are incorrect or misordered. " +
            "Required columns are [ " + FormatUtil.join(columnRefs, ", ") + "].");
      }
      pkMap.put(keyName, keyValue);
    }

    return new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), pkMap);
  }

  private static List<String> parseAttributeNames(JSONArray attributeNames, RecordClass recordClass)
      throws WdkUserException {
    Map<String, AttributeField> allowedAttributes = recordClass.getAttributeFieldMap();
    List<String> namesList = new ArrayList<String>();
    for (int i = 0; i < attributeNames.length(); i++) {
      String name = attributeNames.getString(i);
      if (!allowedAttributes.containsKey(name)) throw new WdkUserException(
          "Attribute name '" + name + "' is not in record class '" + recordClass.getFullName() + "'.");
      namesList.add(name);
    }
    return namesList;
  }

  private static List<String> parseTableNames(JSONArray tableNames, RecordClass recordClass)
      throws WdkUserException {
    Map<String, TableField> allowedTables = recordClass.getTableFieldMap();
    List<String> namesList = new ArrayList<String>();
    for (int i = 0; i < tableNames.length(); i++) {
      String name = tableNames.getString(i);
      if (!allowedTables.containsKey(name)) throw new WdkUserException(
          "Table name '" + name + "' is not in record class '" + recordClass.getFullName() + "'.");
      namesList.add(name);
    }
    return namesList;
  }

  private final RecordClass _recordClass;
  private final PrimaryKeyValue _primaryKey;
  private List<String> _attrNames = new ArrayList<String>();
  private List<String> _tableNames = new ArrayList<String>();

  private RecordRequest(RecordClass recordClass, PrimaryKeyValue primaryKey) {
    _recordClass = recordClass;
    _primaryKey = primaryKey;
  }

  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public List<String> getAttributeNames() {
    return _attrNames;
  }

  private void setAttributeNames(List<String> attrNames) {
    _attrNames = attrNames;
  }

  public List<String> getTableNames() {
    return _tableNames;
  }

  private void setTableNames(List<String> tableNames) {
    _tableNames = tableNames;
  }

  public PrimaryKeyValue getPrimaryKey() {
    return _primaryKey;
  }
}
