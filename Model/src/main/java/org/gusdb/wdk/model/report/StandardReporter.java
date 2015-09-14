package org.gusdb.wdk.model.report;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A reporter whose configuration is standard, ie, a list of attributes and tables.
 * @author steve
 *
 */
public abstract class StandardReporter extends Reporter {
  
  protected Configuration reporterConfig = new Configuration();
  private static Logger logger = Logger.getLogger(XMLReporter.class);

  protected StandardReporter(AnswerValue answerValue, int startIndex, int endIndex) {
    super(answerValue, startIndex, endIndex);
  }

  /*
   * 
   */
@Override
public void configure(Map<String, String> config){
  super.configure(config);
  reporterConfig.configure(config);
}

@Override
public void configure(JSONObject config)  {
  super.configure(config);
  reporterConfig.configure(config);
}

@Override
public String getConfigInfo() {
  return "This reporter does not have config info yet.";
}

protected Set<Field> validateColumns() throws WdkModelException {
  // get a map of report maker fields
  Map<String, Field> fieldMap = getQuestion().getFields(FieldScope.REPORT_MAKER);

  // the config map contains a list of column names;
  Set<Field> columns = new LinkedHashSet<Field>();

  if (reporterConfig.getIsAllFields()) {
    columns.addAll(fieldMap.values());
  }
  else {
    if (reporterConfig.getIsAllAttributes()) {
      for (String k : fieldMap.keySet()) {
        Field f = fieldMap.get(k);
        if (f.getClass().getName().contains("AttributeField"))
          columns.add(f);
      }
    }
    else  {
      for (String column : reporterConfig.getAttributes()) {
        column = column.trim();
        if (fieldMap.containsKey(column)) {
          columns.add(fieldMap.get(column));
        }
      }
    }

    if (reporterConfig.getIsAllTables()) {
      for (String k : fieldMap.keySet()) {
        Field f = fieldMap.get(k);
        if (f.getClass().getName().contains("TableField"))
          columns.add(f);
      }
    }
    else {
      for (String column : reporterConfig.getTables()) {
        column = column.trim();
        if (!fieldMap.containsKey(column))
          throw new WdkModelException("The column '" + column + "' cannot be included in the report");
        columns.add(fieldMap.get(column));
      }
    }
  }
  return columns;
}

protected Set<AttributeField> validateAttributeColumns() throws WdkModelException {
  // get a map of report maker fields
  Map<String, AttributeField> fieldMap = getQuestion().getAttributeFieldMap();

  // the config map contains a list of column names;
  Set<AttributeField> columns = new LinkedHashSet<AttributeField>();

  if (reporterConfig.getIsAllAttributes()) {
    logger.info("FIELDSLIST ALL");
    for (String k : fieldMap.keySet()) {
      AttributeField f = fieldMap.get(k);
      if (f.getClass().getName().contains("AttributeField"))
        columns.add(f);
    }
  }
  else {
    for (String column : reporterConfig.getAttributes()) {
      column = column.trim();
      if (fieldMap.containsKey(column)) {
        columns.add(fieldMap.get(column));
      }
    }
  }
  return columns; 
}

/**
 * encapsulate the configuration of a reporter, for standard reporters, that take a list of attrs and tables.
 * unfortunately existing reporters use two different ways of configuring, either storing all fields in one list, or breaking lists into tables and attributes.
 * this config object supports both, until we clean that up, moving all to just the latter
 * @author steve
 *
 */
public class Configuration {
    // configuration props
    public static final String SELECT_ALL_FIELDS = "all-fields";
    public static final String SELECT_ALL_ATTRS = "allAttributes";
    public static final String SELECT_ALL_TABLES = "allTables";
    public static final String SELECTED_FIELDS = "selectedFields";  // tables or attributes
    public static final String SELECTED_ATTRS = "o-fields";
    public static final String SELECTED_TABLES = "o-tables";
    public static final String INCLUDE_EMPTY_TABLES = "hasEmptyTable";
    public static final String ATTACHMENT_TYPE = "downloadType";
    public static final String SELECTED_ATTRS_JSON = "attributes";
    public static final String SELECTED_TABLES_JSON = "tables";
    public static final String INCLUDE_EMPTY_TABLES_JSON = "includeEmptyTables";
    public static final String ATTACHMENT_TYPE_JSON = "attachmentType";
    
    private boolean includeEmptyTables;
    private List<String> fields = new ArrayList<String>();  // table and attribute field names
    private boolean allFields = false;
    private List<String> attributes = new ArrayList<String>();  
    private boolean allAttributes = false;
    private List<String> tables = new ArrayList<String>();  // table and attribute field names
    private boolean allTables = false;
    private String attachmentType = null;
    
    public boolean getIncludeEmptyTables() { return includeEmptyTables; }
    public List<String> getFields() {return fields == null? null : Collections.unmodifiableList(fields); }
    public boolean getIsAllFields() { return allFields || (allTables && allAttributes); }
    public List<String> getAttributes() {return attributes == null? null : Collections.unmodifiableList(attributes); }
    public boolean getIsAllAttributes() { return allAttributes; }
    public List<String> getTables() {return tables == null? null : Collections.unmodifiableList(tables); }
    public boolean getIsAllTables() { return allTables; }
    public String getAttachmentType() {return attachmentType; }
 
    /**
     * as it stands now, this class does not protect against conflicting configurations.  it goes in
     * priority order.   for example, if all fields are selected, it ignores other configurations.
     * 
     * eventually this will probably lose support for the SELECT_ALL_FIELDS and SELECTED_FIELDS options as they are
     * redundant
     * @param config
     */
    public void configure(JSONObject config) {
    
      if (config.has(INCLUDE_EMPTY_TABLES_JSON)) {
        String value = config.getString(INCLUDE_EMPTY_TABLES_JSON);
        includeEmptyTables = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) ? true : false;
      } 

      Map<String, Field> fieldMap = getQuestion().getFields(
                FieldScope.REPORT_MAKER);
      
      if (config.has(SELECT_ALL_FIELDS)) {
        allFields = true;
        allTables = true;
        allAttributes = true;
      } else if (config.has(SELECTED_FIELDS)){
        JSONArray flds = config.getJSONArray(SELECTED_FIELDS);
        for (int i=0; i<flds.length(); i++) {
          String fld = flds.getString(i);
          fields.add(fld);
          if (fieldMap.get(fld).getClass().getName().contains("AttributeField")) attributes.add(fld);
          if (fieldMap.get(fld).getClass().getName().contains("TableField")) tables.add(fld);
        }
      }
       
      if (config.has(SELECT_ALL_ATTRS)) {
        allAttributes = true;
      } else if (!allFields && !config.has(SELECTED_FIELDS) && config.has(SELECTED_ATTRS_JSON)){
        JSONArray flds = config.getJSONArray(SELECTED_ATTRS_JSON);
        for (int i=0; i<flds.length(); i++) {
          String fld = flds.getString(i);
          attributes.add(fld);
        }
      }

      if (config.has(SELECT_ALL_TABLES)) {
        allTables = true;
      } else if (!allFields && !config.has(SELECTED_FIELDS) && config.has(SELECTED_TABLES_JSON)){
        JSONArray flds = config.getJSONArray(SELECTED_TABLES_JSON);
        for (int i=0; i<flds.length(); i++) {
          String fld = flds.getString(i);
          tables.add(fld);
        }
      }

      if (config.has(ATTACHMENT_TYPE_JSON)) attachmentType = config.getString(ATTACHMENT_TYPE_JSON);
    }
    
    /**
     * support for legacy Map specification.  will lose when we replace all reporter JSPs
     * @param config
     */
    public void configure(Map<String,String> config) {
      
      if (config.containsKey(INCLUDE_EMPTY_TABLES)) {
        String value = config.get(INCLUDE_EMPTY_TABLES);
        includeEmptyTables = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) ? true : false;
      } 
      
      Map<String, Field> fieldMap = getQuestion().getFields(
                FieldScope.REPORT_MAKER);

      if (config.containsKey(SELECTED_FIELDS)) {
        String flds = config.get(SELECTED_FIELDS);
        if (flds.equals("all")) allFields = true;
        else fields = Arrays.asList(flds.split(","));
        for (String fld : fields) {
          if (fieldMap.get(fld).getClass().getName().contains("AttributeField")) attributes.add(fld);
          if (fieldMap.get(fld).getClass().getName().contains("TableField")) tables.add(fld);
        }
      } else {
	if (config.containsKey(SELECTED_ATTRS)) {
	  String attrFlds = config.get(SELECTED_ATTRS);
	  if (attrFlds.equals("all")) allAttributes = true;
	  else attributes = Arrays.asList(attrFlds.split(","));
	}

	if (config.containsKey(SELECTED_TABLES)) {
          String tableFlds = config.get(SELECTED_TABLES);
          if (tableFlds.equals("all")) allTables = true;
          else tables = Arrays.asList(tableFlds.split(","));
	}        
      }

      if (config.containsKey(ATTACHMENT_TYPE)) attachmentType = config.get(ATTACHMENT_TYPE);
      
    }

}

}
