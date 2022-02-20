package org.gusdb.wdk.service.formatter;

import java.util.Collection;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK AttributeFields.  AttributeField JSON will have the following form:
 * {
 *   name: String,
 *   displayName: String,
 *   help: String,
 *   align: String,
 *   isSortable: Boolean,
 *   isRemovable: Boolean,
 *   type: String (comes from “type” property of attribute tag),
 *   category: String,
 *   truncateTo: Integer,
 *   isDisplayable: Boolean,
 *   isInReport: Boolean,
 *   properties: Object
 *   formats: Array of reporter names (String)
 * }
 *
 * @author rdoherty
 */
public class AttributeFieldFormatter {

  public static JSONArray getAttributesJson(Collection<AttributeField> attributes, boolean expandAttributes) {
    JSONArray attributesJson = new JSONArray();
    for (AttributeField attribute : attributes) {
      attributesJson.put(expandAttributes ?
          getAttributeJson(attribute) : attribute.getName());
    }
    return attributesJson;
  }

  public static JSONObject getAttributeJson(AttributeField attribute) {
    return new JSONObject()
      .put(JsonKeys.ALIGN, attribute.getAlign())
      .put(JsonKeys.DISPLAY_NAME, attribute.getDisplayName())
      .put(JsonKeys.FORMATS, RecordClassFormatter.getAnswerFormatsJson(attribute.getReporters().values(), FieldScope.ALL))
      .put(JsonKeys.HELP, attribute.getHelp())
      .put(JsonKeys.IS_DISPLAYABLE, FieldScope.NON_INTERNAL.isFieldInScope(attribute))
      .put(JsonKeys.IS_IN_REPORT, FieldScope.REPORT_MAKER.isFieldInScope(attribute))
      .put(JsonKeys.IS_REMOVABLE, attribute.isRemovable())
      .put(JsonKeys.IS_SORTABLE, attribute.isSortable())
      .put(JsonKeys.NAME, attribute.getName())
      .put(JsonKeys.PROPERTIES, attribute.getPropertyLists())
      .put(JsonKeys.TRUNCATE_TO, attribute.getTruncateTo())
      .put(JsonKeys.TYPE, attribute.getType())
      .put(JsonKeys.TOOLS, formatToolsJson(attribute))
      .put(JsonKeys.DATA_TYPE, attribute.getDataType());

  }

  private static JSONObject formatToolsJson(AttributeField field) {
    return new JSONObject()
      // all tools must have a reporter per the RNG
      .put(JsonKeys.REPORTS, field.getColumnReporterNames())
      // but only some tools have a filter; find which ones
      .put(JsonKeys.FILTERS, field.getColumnFilterNames());
  }
}
