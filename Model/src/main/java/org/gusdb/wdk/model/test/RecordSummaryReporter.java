package org.gusdb.wdk.model.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wsf.util.BaseCLI;

public class RecordSummaryReporter extends BaseCLI {

  private static final String ARG_OUTPUT_FILE = "out";

  private static void addToMap(String text, String project,
      Map<String, Set<String>> map) {
    if (text == null) return;
    text = text.trim();
    if (text.length() == 0) return;

    Set<String> projects = map.get(text);
    if (projects == null) {
      projects = new LinkedHashSet<>();
      map.put(text, projects);
    }
    projects.add(project);
  }

  private static final class RecordInfo {
    public String recordName;
    public Map<String, Set<String>> displayNames = new LinkedHashMap<>();
    public Map<String, Set<String>> shortDisplayNames = new LinkedHashMap<>();
    public Map<String, FieldInfo> fieldInfos = new LinkedHashMap<>();

    public RecordInfo(RecordClass recordClass) {
      this.recordName = recordClass.getFullName();
    }

    public void addDisplayName(String displayName, String project) {
      addToMap(displayName, project, displayNames);
    }

    public void addShortDisplayName(String shortDisplayName, String project) {
      addToMap(shortDisplayName, project, shortDisplayNames);
    }
  }

  private static final class FieldInfo {
    public String fieldName;
    public String fieldType;
    public Map<String, Set<String>> displayNames = new LinkedHashMap<>();
    public Map<String, Set<String>> helps = new LinkedHashMap<>();

    public FieldInfo(Field field) {
      this.fieldName = field.getName();
      this.fieldType = (field instanceof TableField) ? "Table" : "Attribute";
    }

    public void addDisplayName(String displayName, String project) {
      addToMap(displayName, project, displayNames);
    }

    public void addHelp(String help, String project) {
      addToMap(help, project, helps);
    }
  }

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    RecordSummaryReporter reporter = new RecordSummaryReporter(cmdName);
    try {
      reporter.invoke(args);
    } finally {
      System.exit(0);
    }
  }

  /**
   * @param command
   * @param description
   */
  protected RecordSummaryReporter(String command) {
    super((command == null) ? "wdkReportRecordSummary" : command,
        "Report text info in recordClasses.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
   */
  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null,
        "A comma separated list of project ids.");

    addSingleValueOption(ARG_OUTPUT_FILE, true, null,
        "The name of the output file.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.util.BaseCLI#invoke()
   */
  @Override
  protected void execute() throws WdkModelException {
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String projectIds = (String) getOptionValue(ARG_PROJECT_ID);
    String output = (String) getOptionValue(ARG_OUTPUT_FILE);

    // load all the models, then load questions from each of them
    Map<String, RecordInfo> recordInfos = new LinkedHashMap<>();
    for (String projectId : projectIds.split(",")) {
      WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
      loadRecords(wdkModel, recordInfos);
    }

    if (!output.endsWith(".csv")) output += ".csv";
    File outFile = new File(output);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter(outFile));
      writer.println("\"Record\",\"Field\",\"Type\",\"Section\",\"Text\",\"Projects\"");

      // sort record info
      String[] recordNames = recordInfos.keySet().toArray(new String[0]);
      Arrays.sort(recordNames);
      for (String recordName : recordNames) {
        RecordInfo recordInfo = recordInfos.get(recordName);
        writeRecordInfo(writer, recordInfo);
        writer.flush();
      }
    } catch (IOException ex) {
      throw new WdkModelException(ex);
    } finally {
      if (writer != null) writer.close();
    }

  }

  private void loadRecords(WdkModel wdkModel,
      Map<String, RecordInfo> recordInfos) {
    String projectId = wdkModel.getProjectId();
    for (RecordClassSet recordSet : wdkModel.getAllRecordClassSets()) {
      for (RecordClass record : recordSet.getRecordClasses()) {
        RecordInfo recordInfo = recordInfos.get(record.getFullName());
        if (recordInfo == null) {
          recordInfo = new RecordInfo(record);
          recordInfos.put(record.getFullName(), recordInfo);
        }
        recordInfo.addDisplayName(record.getDisplayNamePlural(), projectId);
        recordInfo.addShortDisplayName(record.getShortDisplayNamePlural(),
            projectId);
        loadFields(projectId, record.getFields(), recordInfo);
      }
    }
  }

  private void loadFields(String projectId, Field[] fields,
      RecordInfo recordInfo) {
    for (Field field : fields) {
      FieldInfo fieldInfo = recordInfo.fieldInfos.get(field.getName());
      if (fieldInfo == null) {
        fieldInfo = new FieldInfo(field);
        recordInfo.fieldInfos.put(field.getName(), fieldInfo);
      }
      fieldInfo.addDisplayName(field.getDisplayName(), projectId);
      fieldInfo.addHelp(field.getHelp(), projectId);
    }
  }

  private void writeRecordInfo(PrintWriter writer, RecordInfo recordInfo) {
    writeRecordInfo(writer, recordInfo, recordInfo.displayNames, "", "",
        "displayName");
    writeRecordInfo(writer, recordInfo, recordInfo.shortDisplayNames, "", "",
        "shortDisplayName");

    // sort fields
    String[] fieldNames = recordInfo.fieldInfos.keySet().toArray(new String[0]);
    Arrays.sort(fieldNames);
    for (String fieldName : fieldNames) {
      FieldInfo fieldInfo = recordInfo.fieldInfos.get(fieldName);
      writeRecordInfo(writer, recordInfo, fieldInfo.displayNames,
          fieldInfo.fieldName, fieldInfo.fieldType, "displayName");
      writeRecordInfo(writer, recordInfo, fieldInfo.helps, fieldInfo.fieldName,
          fieldInfo.fieldType, "help");
    }
  }

  private void writeRecordInfo(PrintWriter writer, RecordInfo recordInfo,
      Map<String, Set<String>> sections, String fieldName, String fieldType,
      String sectionName) {
    for (String text : sections.keySet()) {
      Set<String> projects = sections.get(text);
      StringBuilder buffer = new StringBuilder();
      for (String project : projects) {
        if (buffer.length() > 0) buffer.append(",");
        buffer.append(project);
      }
      String projectList = buffer.toString();

      writer.write(escape(recordInfo.recordName) + ", ");
      writer.write(escape(fieldName) + ", ");
      writer.write(escape(fieldType) + ", ");
      writer.write(escape(sectionName) + ", ");
      writer.print(escape(text) + ", ");
      writer.println(escape(projectList) + ", ");
    }
  }

  private String escape(String text) {
    if (text == null || text.length() == 0) return "";
    text = text.replaceAll("\\s", " ").replaceAll(",", ";");
    return "\"" + text.replaceAll("\"", "\"\"") + "\"";
  }
}
