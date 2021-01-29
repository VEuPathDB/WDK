package org.gusdb.wdk.model.report.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Encapsulate the configuration of a reporter, for standard reporters, that take a list of attrs and
 * tables. unfortunately existing reporters use two different ways of configuring, either storing all fields
 * in one list, or breaking lists into tables and attributes. this config object supports both, until we
 * clean that up, moving all to just the latter
 * 
 * @author steve
 *
 */
public class StandardConfig {

  public static enum StreamStrategy {
    PAGED_ANSWER, FILE_BASED, SMART;
  }

  // configuration props
  public static final String SELECT_ALL_FIELDS = "all-fields";
  public static final String SELECT_ALL_ATTRS = "allAttributes";
  public static final String SELECT_ALL_TABLES = "allTables";
  public static final String SELECTED_FIELDS = "selectedFields"; // tables or attributes
  public static final String SELECTED_ATTRS = "o-fields";
  public static final String SELECTED_TABLES = "o-tables";
  public static final String INCLUDE_EMPTY_TABLES = "hasEmptyTable";
  public static final String ATTACHMENT_TYPE = "downloadType";
  public static final String SELECTED_ATTRS_JSON = "attributes";
  public static final String SELECTED_TABLES_JSON = "tables";
  public static final String INCLUDE_EMPTY_TABLES_JSON = "includeEmptyTables";
  public static final String ATTACHMENT_TYPE_JSON = "attachmentType";
  public static final String STREAM_STRATEGY_JSON = "streamStrategy";

  private final Question _question;

  private boolean includeEmptyTables;
  private List<String> fields = new ArrayList<String>(); // table and attribute field names
  private boolean allFields = false;
  private List<String> attributes = new ArrayList<String>();
  private boolean allAttributes = false;
  private List<String> tables = new ArrayList<String>(); // table and attribute field names
  private boolean allTables = false;
  private String attachmentType = "text";
  protected StreamStrategy streamStrategy = StreamStrategy.SMART;

  public StandardConfig(Question question) {
    _question = question;
  }
  
  public boolean getIncludeEmptyTables() {
    return includeEmptyTables;
  }

  public List<String> getFields() {
    return fields == null ? null : Collections.unmodifiableList(fields);
  }

  public boolean getIsAllFields() {
    return allFields || (allTables && allAttributes);
  }

  public List<String> getAttributes() {
    return attributes == null ? null : Collections.unmodifiableList(attributes);
  }

  public boolean getIsAllAttributes() {
    return allAttributes;
  }

  public List<String> getTables() {
    return tables == null ? null : Collections.unmodifiableList(tables);
  }

  public boolean getIsAllTables() {
    return allTables;
  }

  public String getAttachmentType() {
    return attachmentType;
  }

  public StreamStrategy getStreamStrategy() {
    return streamStrategy;
  }

  /**
   * As it stands now, this class does not protect against conflicting configurations. It goes in priority
   * order. For example, if all fields are selected, it ignores other configurations.
   * 
   * Eventually this will probably lose support for the SELECT_ALL_FIELDS and SELECTED_FIELDS options as
   * they are redundant
   * 
   * @param config
   */
  public StandardConfig configure(JSONObject config) {

    if (config.has(INCLUDE_EMPTY_TABLES_JSON)) {
      includeEmptyTables = config.getBoolean(INCLUDE_EMPTY_TABLES_JSON);
    } 

    // TODO:  we are no longer using the inReportMaker flag.  we need to purge all the
    //        options that include xxxx_ALL_xxxx soon)
    Map<String, Field> fieldMap = _question.getFields(FieldScope.ALL);

    // TODO: this option should be removed, as well as all xxxx_ALL_xxxx options
    if (config.has(SELECT_ALL_FIELDS)) {
      allFields = true;
      allTables = true;
      allAttributes = true;
    }
    else if (config.has(SELECTED_FIELDS)) {
      JSONArray flds = config.getJSONArray(SELECTED_FIELDS);
      for (int i = 0; i < flds.length(); i++) {
        String fld = flds.getString(i);
        if (fieldMap.containsKey(fld)) {
          // we might get passed category names, that are not fields. skip these
          fields.add(fld);
          if (fieldMap.get(fld).getClass().getName().contains("AttributeField"))
            attributes.add(fld);
          if (fieldMap.get(fld).getClass().getName().contains("TableField"))
            tables.add(fld);
        }
      }
    }

    if (config.has(SELECT_ALL_ATTRS)) {
      allAttributes = true;
    }
    else if (!allFields && !config.has(SELECTED_FIELDS) && config.has(SELECTED_ATTRS_JSON)) {
      JSONArray flds = config.getJSONArray(SELECTED_ATTRS_JSON);
      for (int i = 0; i < flds.length(); i++) {
        String fld = flds.getString(i);
        attributes.add(fld);
      }
    }

    if (config.has(SELECT_ALL_TABLES)) {
      allTables = true;
    }
    else if (!allFields && !config.has(SELECTED_FIELDS) && config.has(SELECTED_TABLES_JSON)) {
      JSONArray flds = config.getJSONArray(SELECTED_TABLES_JSON);
      for (int i = 0; i < flds.length(); i++) {
        String fld = flds.getString(i);
        tables.add(fld);
      }
    }

    if (config.has(ATTACHMENT_TYPE_JSON))
      attachmentType = config.getString(ATTACHMENT_TYPE_JSON).toLowerCase();

    if (config.has(STREAM_STRATEGY_JSON))
      streamStrategy = StreamStrategy.valueOf(config.getString(STREAM_STRATEGY_JSON).toUpperCase());

    return this;
  }

  /**
   * support for legacy Map specification. will lose when we replace all reporter JSPs
   * 
   * @param config
   */
  public StandardConfig configure(Map<String, String> config) throws ReporterConfigException {

    if (config.containsKey(INCLUDE_EMPTY_TABLES)) {
      String value = config.get(INCLUDE_EMPTY_TABLES);
      includeEmptyTables = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) ? true : false;
    }

    Map<String, Field> fieldMap = _question.getFields(FieldScope.ALL);

    if (config.containsKey(SELECTED_FIELDS)) {
      String flds = config.get(SELECTED_FIELDS);
      if (flds.equals("all"))
        allFields = true;
      else
        fields = Arrays.asList(flds.split(","));
      for (String fld : fields) {
        if (fieldMap.containsKey(fld)) { // we might get passed category names, that are not fields. skip these
          if (fieldMap.get(fld).getClass().getName().contains("AttributeField"))
            attributes.add(fld);
          if (fieldMap.get(fld).getClass().getName().contains("TableField"))
            tables.add(fld);
        }
      }
    }
    else {
      // legacy o-fields and o-tables are used by (old) web services; track bad inputs and throw exception
      //   if any exist.  It will be caught by ProcessRESTAction and an appropriate response will be returned
      // process o-fields param
      List<String> badAttrs = new ArrayList<>();
      if (config.containsKey(SELECTED_ATTRS)) {
        String attrFlds = config.get(SELECTED_ATTRS);
        if (attrFlds.equals("all")) {
          allAttributes = true;
        }
        else {
          String[] tmpAttrs = attrFlds.split(",");
          for (String attr : tmpAttrs) {
            // only add attr if it is a real field (no categories)
            if (fieldMap.containsKey(attr) &&
                fieldMap.get(attr).getClass().getName().contains("AttributeField")) {
              attributes.add(attr);
            }
            else {
              badAttrs.add(attr);
            }
          }
        }
      }
      // process o-tables param
      List<String> badTables = new ArrayList<>();
      if (config.containsKey(SELECTED_TABLES)) {
        String tableFlds = config.get(SELECTED_TABLES);
        if (tableFlds.equals("all")) {
          allTables = true;
        }
        else {
          String[] tmpTables = tableFlds.split(",");
          for (String table : tmpTables) {
            // only add table if it is a real field (no categories)
            if (fieldMap.containsKey(table) &&
                fieldMap.get(table).getClass().getName().contains("TableField")) {
              tables.add(table);
            }
            else {
              badTables.add(table);
            }
          }
        }
      }
      // process errors
      Map<String,String> paramErrors = new HashMap<>();
      if (!badAttrs.isEmpty()) {
        paramErrors.put(SELECTED_ATTRS, "The following passed fields are invalid: " + FormatUtil.join(badAttrs.toArray(), ", "));
      }
      if (!badTables.isEmpty()) {
        paramErrors.put(SELECTED_TABLES, "The following passed tables are invalid: " + FormatUtil.join(badTables.toArray(), ", "));
      }
      if (!paramErrors.isEmpty()) {
        throw new ReporterConfigException("Invalid inputs", paramErrors);
      }
    }

    if (config.containsKey(ATTACHMENT_TYPE))
      attachmentType = config.get(ATTACHMENT_TYPE).toLowerCase();

    return this;
  }
}
