package org.gusdb.wdk.service.formatter;

import java.util.Collection;

import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.service.formatter.Keys;
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
 * }
 * 
 * @author rdoherty
 */
public class AttributeFieldFormatter {

  public static JSONArray getAttributesJson(Collection<AttributeField> attributes,
      FieldScope scope, boolean expandAttributes) {
    JSONArray attributesJson = new JSONArray();
    for (AttributeField attribute : attributes) {
      if (scope.isFieldInScope(attribute)) {
        attributesJson.put(expandAttributes ?
            getAttributeJson(attribute) : attribute.getName());
      }
    }
    return attributesJson;
  }

  public static JSONObject getAttributeJson(AttributeField attribute) {
    return new JSONObject()
      .put(Keys.NAME, attribute.getName())
      .put(Keys.DISPLAY_NAME, attribute.getDisplayName())
      .put(Keys.HELP, attribute.getHelp())
      .put(Keys.ALIGN, attribute.getAlign())
      .put(Keys.IS_SORTABLE, attribute.isSortable())
      .put(Keys.IS_REMOVABLE, attribute.isRemovable())
      .put(Keys.TYPE, attribute.getType())
      .put(Keys.CATEGORY, attribute.getAttributeCategory())
      .put(Keys.TRUNCATE_TO, attribute.getTruncateTo())
      .put(Keys.IS_DISPLAYABLE, FieldScope.NON_INTERNAL.isFieldInScope(attribute))
      .put(Keys.IS_IN_REPORT, FieldScope.REPORT_MAKER.isFieldInScope(attribute))
      .put(Keys.PROPERTIES, attribute.getPropertyLists());
  }
}
