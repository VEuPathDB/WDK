package org.gusdb.wdk.service.request.answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.Reporter.ContentDisposition;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerDetailsFactory {

  private static final Logger LOG = Logger.getLogger(AnswerDetailsFactory.class);

  private static final String RETURN_ALL_ATTRIBUTES = "__ALL_ATTRIBUTES__";
  private static final String RETURN_DEFAULT_ATTRIBUTES = "__DEFAULT_ATTRIBUTES__";
  private static final String RETURN_ALL_TABLES = "__ALL_TABLES__";

  /**
   * Creates a default request specifics object based on the default attributes
   * and sorting defined in the question and the question's record class.
   * 
   * @param question question this specifics object is for
   * @return default request specifics
   */
  public static AnswerDetails createDefault(Question question) {
    AnswerDetails defaultSpecs = new AnswerDetails();
    // default offset and numRecords are set on declaration lines
    defaultSpecs.setAttributes(question.getSummaryAttributeFieldMap());
    defaultSpecs.setTables(Collections.EMPTY_MAP);
    defaultSpecs.setSorting(getDefaultSorting(question, defaultSpecs.getAttributes()));
    return defaultSpecs;
  }

  /**
   * Creates an instance of this class from the passed JSON, validated for the
   * passed question.  All fields are optional.  Attributes and tables passed
   * must be valid for the question.  If attributes or tables properties are
   * omitted, none are returned.  If sorting is omitted, default sorting is used.
   * Sorting attributes not in the returned attribute set are skipped.  If no
   * sorting remains after skipping, the PK is used.  Pagination offset must be
   * a non-negative integer; numRecords can be any integer, with positive values
   * being the number of records to be returned.  Zero or negative values for
   * numRecords will return all records.
   * 
   * Special values: The attributes and tables properties can be either an array
   * of names, or a special String value indicating a particular set of values.
   * The following special strings are currently supported:
   * 
   * - '__ALL_ATTRIBUTES__': represents all attributes for the passed question
   * - '__DEFAULT_ATTRIBUTES__': represents the default attributes for the passed question
   * - '__ALL_TABLES__': represents all tables for the passed question
   * - Note: there are no "default" tables
   * 
   * @param specJson JSON value used to create instance. Should have the form:
   * {
   *   pagination: { offset: Number, numRecords: Number },
   *   attributes: [ attributeName: String ], or special string,
   *   tables: [ tableName: String ], or special string,
   *   sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ],
   *   contentDisposition: 'inline' (default) OR 'attachment'
   * }
   * 
   * All values are optional.
   * 
   * @param question question used to validate incoming values
   * @throws RequestMisformatException if values are invalid or structure is malformed
   */
  public static AnswerDetails createFromJson(JSONObject specJson,
      Question question) throws RequestMisformatException {
      AnswerDetails specs = new AnswerDetails();

      // set requested paging
      if (specJson.has("pagination")) {
        JSONObject paging = specJson.getJSONObject("pagination");
        specs.setOffset(paging.getInt("offset"));
        if (specs.getOffset() < 0)
          throw new RequestMisformatException("Paging offset must be non-negative.");
        specs.setNumRecords(paging.getInt("numRecords"));
        if (specs.getNumRecords() < 0) {
          specs.setNumRecords(AnswerDetails.ALL_RECORDS);
        }
      }

      // set requested attributes
      specs.setAttributes(parseAttributeJson(specJson, question));

      // set requested tables
      specs.setTables(parseTableJson(specJson, question));

      // set requested sorting
      if (specJson.has("sorting")) {
        JSONArray sortingJson = specJson.getJSONArray("sorting");
        specs.setSorting(sortingJson.length() == 0 ?
            getDefaultSorting(question, specs.getAttributes()) :
            ensureElements(parseSorting(sortingJson, specs.getAttributes()), question));
      }
      else {
        specs.setSorting(getDefaultSorting(question, specs.getAttributes()));
      }

      // set content disposition
      if (specJson.has("contentDisposition")) {
        specs.setContentDisposition(ContentDisposition.valueOf(specJson.getString("contentDisposition").toUpperCase()));
      }

      return specs;
  }

  private static Map<String, TableField> parseTableJson(JSONObject specJson, Question question) throws RequestMisformatException {
    if (specJson.has("tables")) {
      // see if property value is a String, if so, it could be a special value
      try {
        switch(specJson.getString("tables")) {
          case RETURN_ALL_TABLES:
            return question.getRecordClass().getTableFieldMap();
          default:
            throw new RequestMisformatException("Illegal string found for " +
                "tables property.  Must be an array or '" + RETURN_ALL_TABLES + "'.");
        }
      }
      // this is the standard case; handle value as an array of table names
      catch (JSONException e) {
        JSONArray tablesJson = specJson.getJSONArray("tables");
        return (tablesJson.length() == 0 ? Collections.EMPTY_MAP :
          parseTableArray(tablesJson, question));
      }
    }
    // if unspecified, do not include any tables
    return Collections.EMPTY_MAP;
  }

  private static Map<String, AttributeField> parseAttributeJson(JSONObject specJson, Question question) throws RequestMisformatException {
    if (specJson.has("attributes")) {
      // see if property value is a String, if so, it could be a special value
      try {
        switch(specJson.getString("attributes")) {
          case RETURN_ALL_ATTRIBUTES:
            return question.getAttributeFieldMap();
          case RETURN_DEFAULT_ATTRIBUTES:
            return question.getSummaryAttributeFieldMap();
          default:
            throw new RequestMisformatException("Illegal string found for " +
                "attributes property.  Must be an array or '" + RETURN_ALL_ATTRIBUTES +
                "' or '" + RETURN_DEFAULT_ATTRIBUTES + "'.");
        }
      }
      // this is the standard case; handle value as an array of attribute names
      catch (JSONException e) {
        JSONArray attributesJson = specJson.getJSONArray("attributes");
        return (attributesJson.length() == 0 ? Collections.EMPTY_MAP :
          parseAttributeArray(attributesJson, question));
      }
    }
    else {
      // if unspecified, do not include any attributes; user could just be requesting tables
      return Collections.EMPTY_MAP;
    }
  }

  private static List<AttributeFieldSortSpec> ensureElements(List<AttributeFieldSortSpec> sorting, Question question) {
    if (!sorting.isEmpty()) return sorting;
    AttributeField pkAttribute = question.getRecordClass().getIdAttributeField();
    return AttributeFieldSortSpec.convertSorting(
        question.getRecordClass().getIdSortingAttributeMap(),
        new MapBuilder<String, AttributeField>(pkAttribute.getName(), pkAttribute).toMap());
  }

  private static List<AttributeFieldSortSpec> getDefaultSorting(Question question, Map<String, AttributeField> attributes) {
    Map<String, Boolean> defaultSorting = question.getSortingAttributeMap();
    List<AttributeFieldSortSpec> convertedSorting = AttributeFieldSortSpec.convertSorting(defaultSorting, attributes);
    return ensureElements(convertedSorting, question);
  }

  private static List<AttributeFieldSortSpec> parseSorting(JSONArray sortingJson, Map<String, AttributeField> attributes) throws RequestMisformatException {
    List<AttributeFieldSortSpec> sorting = new ArrayList<>();
    for (int i = 0; i < sortingJson.length(); i++) {
      JSONObject obj = sortingJson.getJSONObject(i);
      String attributeName = obj.getString("attributeName");
      String directionStr = obj.getString("direction").toUpperCase();
      if (!attributes.containsKey(attributeName)) {
        LOG.warn("Attribute '" + attributeName + "' was listed in sorting but is not a returned attribute.  Skipping...");
      }
      if (!SortDirection.isValidDirection(directionStr)) {
        throw new RequestMisformatException("Bad value: '" + directionStr +
            "' is not a direction. Only " + FormatUtil.join(SortDirection.values(), ", ") + " supported.");
      }
      // this entry passed; add sorting item
      sorting.add(new AttributeFieldSortSpec(attributes.get(attributeName), SortDirection.valueOf(directionStr)));
    }
    return sorting;
  }

  private static Map<String, TableField> parseTableArray(JSONArray tablesJson,
      Question question) throws RequestMisformatException {
    Map<String, TableField> availableTables = question.getRecordClass().getTableFieldMap();
    Map<String, TableField> tables = new LinkedHashMap<>();
    for (int i = 0; i < tablesJson.length(); i++) {
      String tableName = tablesJson.getString(i);
      TableField table = availableTables.get(tableName);
      if (table == null) {
        throw new RequestMisformatException("Table '" + tableName +
            "' is not available for question '" + question.getFullName() + "'");
      }
      tables.put(table.getName(), table);
    }
    return tables;
  }

  private static Map<String, AttributeField> parseAttributeArray(JSONArray attributesJson,
      Question question) throws RequestMisformatException {
    Map<String, AttributeField> availableAttribs = question.getAttributeFieldMap();
    Map<String, AttributeField> attributes = new LinkedHashMap<>();
    for (int i = 0; i < attributesJson.length(); i++) {
      String attribName = attributesJson.getString(i);
      AttributeField attrib = availableAttribs.get(attribName);
      if (attrib == null) {
        throw new RequestMisformatException("Attribute '" + attribName +
            "' is not available for record class '" + question.getFullName() + "'");
      }
      attributes.put(attrib.getName(), attrib);
    }
    return attributes;
  }
}
