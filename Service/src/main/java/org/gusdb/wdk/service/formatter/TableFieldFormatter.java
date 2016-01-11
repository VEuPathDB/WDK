package org.gusdb.wdk.service.formatter;

import java.util.Collection;
import java.util.Map.Entry;

import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.request.answer.SortItem.Direction;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK TableFields.  TableField JSON will have the following form:
 * {
 *   name: String,
 *   displayName: String,
 *   help: String,
 *   type: String,
 *   category: String,
 *   description: String,
 *   isDisplayable: Boolean,
 *   isInReport: Boolean,
 *   properties: Object,
 *   attributes: [ see AttributeFieldFormatter ],
 *   sorting: [ {
 *     name: String,
 *     direction: ASC | DESC
 *   } ]
 * }
 * 
 * @author rdoherty
 */
public class TableFieldFormatter {

  public static JSONArray getTablesJson(Collection<TableField> tableFields, FieldScope scope, boolean expandTables, boolean expandAttributes) {
    JSONArray array = new JSONArray();
    for (TableField table : tableFields) {
      if (scope.isFieldInScope(table)) {
        array.put(expandTables ? getTableJson(table, expandAttributes) :table.getName());
      }
    }
    return array;
  }

  public static JSONObject getTableJson(TableField table, boolean expandAttributes) {
    return new JSONObject()
      .put(Keys.NAME, table.getName())
      .put(Keys.DISPLAY_NAME, table.getDisplayName())
      .put(Keys.HELP, table.getHelp())
      .put(Keys.TYPE, table.getType())
      .put(Keys.CATEGORY, table.getAttributeCategory())
      .put(Keys.DESCRIPTION, table.getDescription())
      .put(Keys.PROPERTIES, table.getPropertyLists())
      .put(Keys.ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
          table.getAttributeFieldMap().values(), FieldScope.ALL, expandAttributes))
      .put(Keys.SORTING, getSortingAttributesJson(table));
  }

  private static JSONArray getSortingAttributesJson(AttributeFieldContainer container) {
    JSONArray json = new JSONArray();
    for (Entry<String,Boolean> attribute : container.getSortingAttributeMap().entrySet()) {
      json.put(new JSONObject()
        .put(Keys.NAME, attribute.getKey())
        .put(Keys.DIRECTION, Direction.fromBoolean(attribute.getValue()).name()));
    }
    return json;
  }
}
