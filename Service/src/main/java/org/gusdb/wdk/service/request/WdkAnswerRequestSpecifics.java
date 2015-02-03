package org.gusdb.wdk.service.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WdkAnswerRequestSpecifics {

  public static enum Direction {

    ASC(true),
    DESC(false);

    private boolean _boolValue;

    private Direction(boolean boolValue) {
      _boolValue = boolValue;
    }

    public boolean getBoolValue() {
      return _boolValue;
    }

    public static boolean isDirection(String str) {
      try {
        valueOf(str);
        return true;
      }
      catch (IllegalArgumentException | NullPointerException e) {
        return false;
      }
    }
  }

  public static class SortItem {

    private AttributeField _attributeField;
    private Direction _direction;

    public SortItem(AttributeField attributeField, Direction direction) {
      _attributeField = attributeField;
      _direction = direction;
    }

    public AttributeField getAttributeField() { return _attributeField; }
    public Direction getDirection() { return _direction; }
  }

  /**
   * {
   *   pagination: { offset: Number, numRecords: Number },
   *   attributes: [ attributeName: String ],
   *   tables: [ tableName: String ],
   *   sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]
   * }
   * @throws RequestMisformatException 
  */
  public static WdkAnswerRequestSpecifics createFromJson(JSONObject specJson,
      RecordClass recordClass) throws RequestMisformatException {
    try {
      WdkAnswerRequestSpecifics specs = new WdkAnswerRequestSpecifics();

      // set requested paging
      JSONObject paging = specJson.getJSONObject("pagination");
      specs._offset = paging.getInt("offset");
      specs._numRecords = paging.getInt("numRecords");

      // set requested attributes
      JSONArray attributesJson = specJson.getJSONArray("attributes");
      specs._attributes = getAttributes(attributesJson, recordClass);

      // set requested tables
      JSONArray tablesJson = specJson.getJSONArray("tables");
      specs._tables = getTables(tablesJson, recordClass);

      // set requested sorting
      JSONArray sortingJson = specJson.getJSONArray("sorting");
      specs._sorting = getSorting(sortingJson, specs._attributes);

      return specs;
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Could not parse answer request specs JSON", e);
    }
  }


  private static List<SortItem> getSorting(JSONArray sortingJson, Map<String, AttributeField> attributes) throws RequestMisformatException {
    List<SortItem> sorting = new ArrayList<>();
    for (int i = 0; i < sortingJson.length(); i++) {
      JSONObject obj = sortingJson.getJSONObject(i);
      String attributeName = obj.getString("attributeName");
      String directionStr = obj.getString("direction").toUpperCase();
      if (!attributes.containsKey(attributeName)) {
        throw new RequestMisformatException("Attribute '" + attributeName +
            "' was listed in sorting but was not a requested attribute");
      }
      if (!Direction.isDirection(directionStr)) {
        throw new RequestMisformatException("Bad value: '" + directionStr +
            "' is not a direction. Only " + FormatUtil.join(Direction.values(), ", ") + " supported.");
      }
      // this entry passed; add sorting item
      sorting.add(new SortItem(attributes.get(attributeName), Direction.valueOf(directionStr)));
    }
    return sorting;
  }


  private static Map<String, TableField> getTables(JSONArray tablesJson,
      RecordClass recordClass) throws RequestMisformatException {
    Map<String, TableField> availableTables = recordClass.getTableFieldMap();
    Map<String, TableField> tables = new HashMap<>();
    for (int i = 0; i < tablesJson.length(); i++) {
      String tableName = tablesJson.getString(i);
      TableField table = availableTables.get(tableName);
      if (table == null) {
        throw new RequestMisformatException("Table '" + tableName +
            "' is not available for record class '" + recordClass.getFullName() + "'");
      }
      tables.put(table.getName(), table);
    }
    return tables;
  }

  private static Map<String, AttributeField> getAttributes(JSONArray attributesJson,
      RecordClass recordClass) throws RequestMisformatException {
    Map<String, AttributeField> availableAttribs = recordClass.getAttributeFieldMap();
    Map<String, AttributeField> attributes = new HashMap<>();
    for (int i = 0; i < attributesJson.length(); i++) {
      String attribName = attributesJson.getString(i);
      AttributeField attrib = availableAttribs.get(attribName);
      if (attrib == null) {
        throw new RequestMisformatException("Attribute '" + attribName +
            "' is not available for record class '" + recordClass.getFullName() + "'");
      }
      attributes.put(attrib.getName(), attrib);
    }
    return attributes;
  }

  private int _offset;
  private int _numRecords;
  private Map<String, AttributeField> _attributes;
  private Map<String, TableField> _tables;
  private List<SortItem> _sorting;

  public int getOffset() {
    return _offset;
  }

  public int getNumRecords() {
    return _numRecords;
  }

  public Map<String, AttributeField> getAttributes() {
    return _attributes;
  }

  public Map<String, TableField> getTables() {
    return _tables;
  }

  public List<SortItem> getSorting() {
    return _sorting;
  }
}
