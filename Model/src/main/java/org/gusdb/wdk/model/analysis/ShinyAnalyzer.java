package org.gusdb.wdk.model.analysis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.StandardReporter;
import org.gusdb.wdk.model.report.TabularReporter;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
import org.gusdb.wdk.model.user.analysis.StatusLogger;
import org.json.JSONException;

public class ShinyAnalyzer extends AbstractStepAnalyzer {

  // this plugin can only be assigned to questions/recordClasses that
  //   are configured with a tabular reporter
  private static final String TABULAR_REPORTER_NAME = "tabular";

  private static final String SHINY_APP_URL_PROP_KEY = "shinyAppUrl";
  private static final String REQUIRED_ATTRIBS_PROP_KEY = "requiredAttributes";

  private static final String DATA_FILE_NAME = "data.tab";

  private static final int DEFAULT_IFRAME_WIDTH_PX = 900;
  private static final int DEFAULT_IFRAME_HEIGHT_PX = 450;

  public static class ViewModel {

    private final String _iframeUrl;
    private final int _iframeWidth;
    private final int _iframeHeight;

    public ViewModel(String iframeUrl, int width, int height) {
      _iframeUrl = iframeUrl;
      _iframeWidth = width;
      _iframeHeight = height;
    }

    public String getIframeUrl() { return _iframeUrl; }
    public int getIframeWidth() { return _iframeWidth; }
    public int getIframeHeight() { return _iframeHeight; }
  }

  @Override
  public void validateProperties() throws WdkModelException {
    checkPropertyExistence(SHINY_APP_URL_PROP_KEY);
    checkPropertyExistence(REQUIRED_ATTRIBS_PROP_KEY);
  }
  
  @Override
  public void validateQuestion(Question question) throws WdkModelException {
    // make sure this question has a tabular reporter; needed to dump results to Shiny
    if (question.getRecordClass().getReporterMap().get(TABULAR_REPORTER_NAME) == null) {
      throw new WdkModelException("A RecordClass using the " + getClass().getSimpleName() +
          " must also be configured with a tabular reporter under the name '" +
          TABULAR_REPORTER_NAME + "'.");
    }
  }

  @Override
  public Object getResultViewModel() throws WdkModelException {
    return new ViewModel(new StringBuilder()
        .append(getProperty(SHINY_APP_URL_PROP_KEY))
        .append("/?contextHash=")
        .append(getStorageDirectory().getFileName().toString())
        .toString(),
        DEFAULT_IFRAME_WIDTH_PX,
        DEFAULT_IFRAME_HEIGHT_PX);
  }

  @Override
  public ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log)
      throws WdkModelException, WdkUserException {
    Reporter reporter = answerValue.createReport(TABULAR_REPORTER_NAME, new MapBuilder<String,String>()
        .put(StandardReporter.Configuration.ATTACHMENT_TYPE, "text")
        .put(TabularReporter.FIELD_HAS_HEADER, "false")
        .put(TabularReporter.FIELD_DIVIDER, FormatUtil.TAB)
        .put(TabularReporter.FIELD_SELECTED_COLUMNS, getProperty(REQUIRED_ATTRIBS_PROP_KEY))
        .toMap());
    // write query results to disk
    Path outputFile = Paths.get(getStorageDirectory().toAbsolutePath().toString(), DATA_FILE_NAME);
    try (FileOutputStream fileOut = new FileOutputStream(outputFile.toFile())) {
      reporter.report(fileOut);
      return ExecutionStatus.COMPLETE;
    }
    catch (IOException | NoSuchAlgorithmException | JSONException | SQLException e) {
      throw new WdkModelException("Unable to dump analysis files to data storage dir", e);
    }
  }
}
