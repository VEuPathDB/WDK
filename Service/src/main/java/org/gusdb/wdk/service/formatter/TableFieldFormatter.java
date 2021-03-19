package org.gusdb.wdk.service.formatter;

import java.util.Collection;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.TableField;
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
        .put(JsonKeys.ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
            table.getAttributeFieldMap().values(), expandAttributes))
//        .put(JsonKeys.CATEGORY, table.getAttributeCategory())
        .put(JsonKeys.CLIENT_SORT_SPEC, getClientSortSpecJson(table))
        .put(JsonKeys.DESCRIPTION, table.getDescription())
        .put(JsonKeys.DISPLAY_NAME, table.getDisplayName())
        .put(JsonKeys.HELP, table.getHelp())
        .put(JsonKeys.IS_DISPLAYABLE, FieldScope.NON_INTERNAL.isFieldInScope(table))
        .put(JsonKeys.IS_IN_REPORT, FieldScope.REPORT_MAKER.isFieldInScope(table))
      .put(JsonKeys.NAME, table.getName())
      .put(JsonKeys.PROPERTIES, table.getPropertyLists())
      .put(JsonKeys.TYPE, table.getType());
  }

  private static JSONArray getClientSortSpecJson(TableField tableField) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonOrgModule()); // use jackson plugin to convert to org.json
    return mapper.convertValue(tableField.getClientSortingOrderList(), JSONArray.class);
  }
}
