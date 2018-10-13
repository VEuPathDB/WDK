package org.gusdb.wdk.service.formatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.attribute.AttributeCategory;
import org.gusdb.wdk.model.report.ReporterRef;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK RecordClass objects into the following form:
 * {
 *   name: String,
 *   displayName: String,
 *   displayNamePlural: String,
 *   shortDisplayName: String,
 *   shortDisplayNamePlural: String,
 *   urlSegment: String,
 *   useBasket: Boolean
 *   description: String,
 *   formats: [ see getAnswerFormatsJson() ],
 *   primaryKeyColumnRefs: [ String ],
 *   recordIdAttributeName: String,
 *   attributes: [ see AttributeFieldFormatter ],
 *   tables: [ see TableFieldFormatter ],
 *   categories: [ see getAttributeCategoriesJson() ]
 * }
 *
 * @author rdoherty
 */
public class RecordClassFormatter {

  public static Collection<Object> getRecordClassesJson(RecordClassSet[] recordClassSets,
      boolean expand) {
    final Collection<Object> out = new ArrayList<>();
    for (RecordClassSet rcSet : recordClassSets) {
      for (RecordClass rc : rcSet.getRecordClasses()) {
        out.add(expand ? getRecordClassJson(rc, true, true, true) :
              rc.getFullName());
      }
    }
    return out;
  }

  public static JSONObject getRecordClassJson(RecordClass recordClass,
      boolean expandAttributes, boolean expandTables, boolean expandTableAttributes) {
    return new JSONObject()
      .put(JsonKeys.NAME, recordClass.getFullName())
      .put(JsonKeys.DISPLAY_NAME, recordClass.getDisplayName())
      .put(JsonKeys.DISPLAY_NAME_PLURAL, recordClass.getDisplayNamePlural())
      .put(JsonKeys.SHORT_DISPLAY_NAME, recordClass.getShortDisplayName())
      .put(JsonKeys.SHORT_DISPLAY_NAME_PLURAL, recordClass.getShortDisplayNamePlural())
      .put(JsonKeys.URL_SEGMENT,  recordClass.getUrlSegment())
      .put(JsonKeys.ICON_NAME, recordClass.getIconName())
      .put(JsonKeys.USE_BASKET, recordClass.isUseBasket())
      .put(JsonKeys.DESCRIPTION, recordClass.getDescription())
      .put(JsonKeys.FORMATS, getAnswerFormatsJson(recordClass.getReporterMap().values(), FieldScope.ALL))
      .put(JsonKeys.HAS_ALL_RECORDS_QUERY, recordClass.hasAllRecordsQuery())
      .put(JsonKeys.PRIMARY_KEY_REFS, JsonUtil.toJsonStringArray(recordClass.getPrimaryKeyDefinition().getColumnRefs()))
      .put(JsonKeys.RECORD_ID_ATTRIBUTE_NAME, recordClass.getIdAttributeField().getName())
      .put(JsonKeys.ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
        recordClass.getAttributeFieldMap().values(), FieldScope.ALL, expandAttributes))
      .put(JsonKeys.TABLES, TableFieldFormatter.getTablesJson(recordClass.getTableFieldMap().values(),
        FieldScope.ALL, expandTables, expandTableAttributes))
      .put(JsonKeys.CATEGORIES, getAttributeCategoriesJson(recordClass));
  }

  public static JSONArray getAnswerFormatsJson(Collection<? extends ReporterRef> reporters, FieldScope scope) {
    JSONArray array = new JSONArray();
    for (ReporterRef reporter : reporters) {
      if (scope.isFieldInScope(reporter)) {
        JSONObject obj = new JSONObject()
          .put(JsonKeys.NAME, reporter.getName())
          .put(JsonKeys.TYPE,  reporter.getReferenceName())
          .put(JsonKeys.DISPLAY_NAME, reporter.getDisplayName())
          .put(JsonKeys.DESCRIPTION, reporter.getDescription())
          .put(JsonKeys.IS_IN_REPORT, FieldScope.REPORT_MAKER.isFieldInScope(reporter))
          .put(JsonKeys.SCOPES, reporter.getScopesList());
        array.put(obj);
      }
    }
    return array;
  }

  private static JSONArray getAttributeCategoriesJson(RecordClass recordClass) {
    List<AttributeCategory> categories = recordClass.getAttributeCategoryTree(FieldScope.ALL).getTopLevelCategories();
    JSONArray attributeCategoriesJson = new JSONArray();
    for (AttributeCategory category : categories) {
      attributeCategoriesJson.put(getAttributeCategoryJson(category));
    }
    return attributeCategoriesJson;
  }

  private static JSONObject getAttributeCategoryJson(AttributeCategory category) {
    JSONObject attributeCategoryJson = new JSONObject()
      .put(JsonKeys.NAME,  category.getName())
      .put(JsonKeys.DISPLAY_NAME,  category.getDisplayName())
      .put(JsonKeys.DESCRIPTION, category.getDescription());
    JSONArray subCategoriesJson = new JSONArray();
    for (AttributeCategory subCategory : category.getSubCategories()) {
      subCategoriesJson.put(getAttributeCategoryJson(subCategory));
    }
    attributeCategoryJson.put(JsonKeys.CATEGORIES, subCategoriesJson);
    return attributeCategoryJson;
  }
}
