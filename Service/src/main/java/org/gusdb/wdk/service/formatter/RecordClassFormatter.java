package org.gusdb.wdk.service.formatter;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.answer.ReporterRef;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeCategory;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.json.JSONArray;
import org.json.JSONObject;

public class RecordClassFormatter {

  private static FieldScope fieldScope = FieldScope.NON_INTERNAL;

  public static JSONArray getRecordClassesJson(RecordClassSet[] recordClassSets,
      boolean expandRecordClasses, boolean expandAttributes,
      boolean expandTables, boolean expandTableAttributes) {
    JSONArray json = new JSONArray();
    for (RecordClassSet rcSet : recordClassSets) {
      for (RecordClass rc : rcSet.getRecordClasses()) {
        json.put(expandRecordClasses ? getRecordClassJson(rc,
            expandAttributes, expandTables, expandTableAttributes) :
              rc.getFullName());
      }
    }
    return json;
  }

  public static JSONObject getRecordClassJson(RecordClass recordClass,
      boolean expandAttributes, boolean expandTables, boolean expandTableAttributes) {
    JSONObject json = new JSONObject();
    json.put("fullName", recordClass.getFullName());
    json.put("displayName", recordClass.getDisplayName());
    json.put("displayNamePlural", recordClass.getDisplayNamePlural());
    json.put("description", recordClass.getDescription());
    json.put("attributes", getAttributesJson(recordClass, expandAttributes));
    json.put("tables", getTablesJson(recordClass, expandTables, expandTableAttributes));
    json.put("attributeCategories", getAttributeCategoriesJson(recordClass));
    json.put("collapsedCategories",  getCollapsedAttributesJson(recordClass));
    return json;
  }

  private static JSONArray getCollapsedAttributesJson(RecordClass recordClass) {
    JSONArray json = new JSONArray();
    List<AttributeCategory> catList = recordClass.getCollapsedCategories();
    if (catList != null) {
      for (AttributeCategory cat : recordClass.getCollapsedCategories()) {
        json.put(cat.getName());
      }
    }
    return json;
  }

  public static JSONArray getAttributesJson(AttributeFieldContainer container, boolean expandAttributes) {
    JSONArray array = new JSONArray();
    for (AttributeField attrib : container.getAttributeFields()) {
      if (fieldScope.isFieldInScope(attrib)) {
        array.put(expandAttributes ? attrib.getName() : getAttributeJson(attrib));
      }
    }
    return array;
  }

  public static JSONObject getAttributeJson(AttributeField attrib) {
    JSONObject json = new JSONObject();
    json.put("name", attrib.getName());
    json.put("type", attrib.getType());
    json.put("category", attrib.getAttributeCategory());
    json.put("displayName", attrib.getDisplayName());
    json.put("help", attrib.getHelp());
    json.put("align", attrib.getAlign());
    return json;
  }

  public static JSONArray getTablesJson(RecordClass recordClass, boolean expandTables, boolean expandAttributes) {
    JSONArray array = new JSONArray();
    for (TableField table : recordClass.getTableFields()) {
      if (fieldScope.isFieldInScope(table)) {
        array.put(expandTables ? table.getName() : getTableJson(table, expandAttributes));
      }
    }
    return array;
  }

  public static JSONObject getTableJson(TableField table, boolean expandAttributes) {
    JSONObject json = new JSONObject();
    json.put("name", table.getName());
    json.put("type", table.getType());
    json.put("category", table.getAttributeCategory());
    json.put("displayName", table.getDisplayName());
    json.put("description", table.getDescription());
    json.put("help", table.getHelp());
    json.put("attributes", getAttributesJson(table, expandAttributes));
    json.put("sorting", getSortingAttributesJson(table));
    json.put("propertyLists", table.getPropertyLists());
    return json;
  }
  
  private static JSONArray getSortingAttributesJson(AttributeFieldContainer container) {
    JSONArray sortingAttributesJson = new JSONArray();
    Map<String, Boolean> sortingAttributeMap = container.getSortingAttributeMap();
    for (String attributeName : sortingAttributeMap.keySet()) {
      JSONObject sortingAttribute = new JSONObject();
      sortingAttribute.put("name", attributeName);
      sortingAttribute.put("direction", sortingAttributeMap.get(attributeName) ? "asc" : "desc");
      sortingAttributesJson.put(sortingAttribute);
    }
    return sortingAttributesJson;
  }

  public static JSONArray getAttributeCategoriesJson(RecordClass recordClass) {
    List<AttributeCategory> categories = recordClass.getAttributeCategoryTree(fieldScope).getTopLevelCategories();
    JSONArray attributeCategoriesJson = new JSONArray();
    for (AttributeCategory category : categories) {
      attributeCategoriesJson.put(getAttributeCategoryJson(category));
    }
    return attributeCategoriesJson;
  }
  
  public static JSONObject getAttributeCategoryJson(AttributeCategory category) {
    List<AttributeCategory> subCategories = category.getSubCategories();
    JSONObject attributeCategoryJson = new JSONObject()
      .put("name",  category.getName())
      .put("displayName",  category.getDisplayName())
      .put("description", category.getDescription());

    if (subCategories.size() > 0) {
      JSONArray subCategoriesJson = new JSONArray();
      for (AttributeCategory subCategory : category.getSubCategories()) {
        subCategoriesJson.put(getAttributeCategoryJson(subCategory));
      }
      attributeCategoryJson.put("subCategories",  subCategoriesJson);
    }

    return attributeCategoryJson;
  }
  
  public static JSONArray getAnswerFormatsJson(Map<String, ReporterRef>reporterMap) {
    JSONArray array = new JSONArray();
    
    for (ReporterRef reporter : reporterMap.values()) {
      JSONObject obj = new JSONObject();
      obj.put(reporter.getDisplayName(), reporter.getName());
      array.put(obj);
    }
    return array;
  }

  
}
