package org.gusdb.wdk.service.formatter;

import java.util.Collection;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.service.formatter.Keys;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

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
 *   attributes: [ see AttributeFieldFormatter ]
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
      .put(Keys.IS_DISPLAYABLE, FieldScope.NON_INTERNAL.isFieldInScope(table))
      .put(Keys.IS_IN_REPORT, FieldScope.REPORT_MAKER.isFieldInScope(table))
      .put(Keys.PROPERTIES, table.getPropertyLists())
      .put(Keys.ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
          table.getAttributeFieldMap().values(), FieldScope.ALL, expandAttributes))
      .put(Keys.CLIENT_SORT_SPEC, getClientSortSpecJson(table));
  }

  private static JSONArray getClientSortSpecJson(TableField tableField) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonOrgModule()); // use jackson plugin to convert to org.json
    return mapper.convertValue(tableField.getClientSortingOrder(), JSONArray.class);
  }
}
