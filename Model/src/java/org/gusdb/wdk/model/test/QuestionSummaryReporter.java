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
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wsf.util.BaseCLI;

public class QuestionSummaryReporter extends BaseCLI {

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

  private static final class QuestionInfo {
    public String questionName;
    public String recordType;
    public Map<String, Set<String>> displayNames = new LinkedHashMap<>();
    public Map<String, Set<String>> shortDisplayNames = new LinkedHashMap<>();
    public Map<String, Set<String>> summaries = new LinkedHashMap<>();
    public Map<String, Set<String>> descriptions = new LinkedHashMap<>();
    public Map<String, ParamInfo> paramInfos = new LinkedHashMap<>();

    public QuestionInfo(Question question) {
      this.questionName = question.getFullName();
      this.recordType = question.getRecordClass().getDisplayName();
    }

    public void addDisplayName(String displayName, String project) {
      addToMap(displayName, project, displayNames);
    }

    public void addShortDisplayName(String shortDisplayName, String project) {
      addToMap(shortDisplayName, project, shortDisplayNames);
    }

    public void addSummary(String summary, String project) {
      addToMap(summary, project, summaries);
    }

    public void addDescription(String description, String project) {
      addToMap(description, project, descriptions);
    }
  }

  private static final class ParamInfo {
    public String paramName;
    public Map<String, Set<String>> prompts = new LinkedHashMap<>();
    public Map<String, Set<String>> helps = new LinkedHashMap<>();

    public ParamInfo(Param param) {
      this.paramName = param.getFullName();
    }

    public void addPrompt(String prompt, String project) {
      addToMap(prompt, project, prompts);
    }

    public void addHelp(String help, String project) {
      addToMap(help, project, helps);
    }
  }

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    QuestionSummaryReporter reporter = new QuestionSummaryReporter(cmdName);
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
  protected QuestionSummaryReporter(String command) {
    super((command == null) ? "wdkReportQuestionSummary" : command,
        "Report text info in questions.");
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
    Map<String, QuestionInfo> questionInfos = new LinkedHashMap<>();
    for (String projectId : projectIds.split(",")) {
      WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
      loadQuestions(wdkModel, questionInfos);
    }

    if (!output.endsWith(".csv")) output += ".csv";
    File outFile = new File(output);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter(outFile));
      writer.println("\"Question\",\"Type\",\"Param\",\"Section\",\"Text\",\"Projects\"");

      // sort question info
      String[] questionNames = questionInfos.keySet().toArray(new String[0]);
      Arrays.sort(questionNames);
      for (String questionName : questionNames) {
        QuestionInfo questionInfo = questionInfos.get(questionName);
        writeQuestionInfo(writer, questionInfo);
        writer.flush();
      }
    } catch (IOException ex) {
      throw new WdkModelException(ex);
    } finally {
      if (writer != null) writer.close();
    }

  }

  private void loadQuestions(WdkModel wdkModel,
      Map<String, QuestionInfo> questionInfos) {
    String projectId = wdkModel.getProjectId();
    for (QuestionSet questionSet : wdkModel.getQuestionSets().values()) {
      for (Question question : questionSet.getQuestions()) {
        QuestionInfo questionInfo = questionInfos.get(question.getFullName());
        if (questionInfo == null) {
          questionInfo = new QuestionInfo(question);
          questionInfos.put(question.getFullName(), questionInfo);
        }
        questionInfo.addDisplayName(question.getDisplayName(), projectId);
        questionInfo.addShortDisplayName(question.getShortDisplayName(),
            projectId);
        questionInfo.addSummary(question.getSummary(), projectId);
        questionInfo.addDescription(question.getDescription(), projectId);
        loadParams(projectId, question.getParams(), questionInfo);
      }
    }
  }

  private void loadParams(String projectId, Param[] params,
      QuestionInfo questionInfo) {
    for (Param param : params) {
      ParamInfo paramInfo = questionInfo.paramInfos.get(param.getFullName());
      if (paramInfo == null) {
        paramInfo = new ParamInfo(param);
        questionInfo.paramInfos.put(param.getFullName(), paramInfo);
      }
      paramInfo.addPrompt(param.getPrompt(), projectId);
      paramInfo.addHelp(param.getHelp(), projectId);
    }
  }

  private void writeQuestionInfo(PrintWriter writer, QuestionInfo questionInfo) {
    writeQuestionInfo(writer, questionInfo, questionInfo.displayNames, "",
        "displayName");
    writeQuestionInfo(writer, questionInfo, questionInfo.shortDisplayNames, "",
        "shortDisplayName");
    writeQuestionInfo(writer, questionInfo, questionInfo.summaries, "",
        "summary");
    writeQuestionInfo(writer, questionInfo, questionInfo.descriptions, "",
        "description");

    // sort params
    String[] paramNames = questionInfo.paramInfos.keySet().toArray(
        new String[0]);
    Arrays.sort(paramNames);
    for (String paramName : paramNames) {
      ParamInfo paramInfo = questionInfo.paramInfos.get(paramName);
      writeQuestionInfo(writer, questionInfo, paramInfo.prompts,
          paramInfo.paramName, "prompt");
      writeQuestionInfo(writer, questionInfo, paramInfo.helps,
          paramInfo.paramName, "help");
    }
  }

  private void writeQuestionInfo(PrintWriter writer, QuestionInfo questionInfo,
      Map<String, Set<String>> sections, String paramName, String sectionName) {
    for (String text : sections.keySet()) {
      Set<String> projects = sections.get(text);
      StringBuilder buffer = new StringBuilder();
      for (String project : projects) {
        if (buffer.length() > 0) buffer.append(",");
        buffer.append(project);
      }
      String projectList = buffer.toString();

      writer.write(escape(questionInfo.questionName) + ", ");
      writer.write(escape(questionInfo.recordType) + ", ");
      writer.write(escape(paramName) + ", ");
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
