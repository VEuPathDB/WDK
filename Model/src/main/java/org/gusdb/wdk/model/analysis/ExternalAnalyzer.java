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

/**
 * This analyzer dumps a pre-configured set of result attributes to a tab-
 * delimited file, where they may be accessed by an "external" (though currently
 * only on the same server) application or web app for analysis.
 * 
 * @author rdoherty
 */
public class ExternalAnalyzer extends AbstractStepAnalyzer {

  // this plugin can only be assigned to questions/recordClasses that
  //   are configured with a tabular reporter
  private static final String TABULAR_REPORTER_NAME = "tabular";

  private static final String EXTERNAL_APP_URL_PROP_KEY = "externalAppUrl";
  private static final String EXTRACTED_ATTRIBS_PROP_KEY = "columnsToExtract";
  private static final String IFRAME_WIDTH_PROP_KEY = "iframeWidthPx";
  private static final String IFRAME_LENGTH_PROP_KEY = "iframeLengthPx";
  private static final String ADD_HEADER_PROP_KEY = "addHeader";

  private static final String DATA_FILE_NAME = "data.tab";

  private static final int DEFAULT_IFRAME_WIDTH_PX = 900;
  private static final int DEFAULT_IFRAME_HEIGHT_PX = 450;
  private static final boolean ADD_HEADER_BY_DEFAULT = true;

  public static class ViewModel {

    private final String _iframeBaseUrl;
    private final int _iframeWidth;
    private final int _iframeHeight;

    public ViewModel(String iframeBaseUrl, int width, int height) {
      _iframeBaseUrl = iframeBaseUrl;
      _iframeWidth = width;
      _iframeHeight = height;
    }

    public String getIframeBaseUrl() { return _iframeBaseUrl; }
    public String getDownloadPath() { return DATA_FILE_NAME; }
    public int getIframeWidth() { return _iframeWidth; }
    public int getIframeHeight() { return _iframeHeight; }
  }

  @Override
  public void validateProperties() throws WdkModelException {
    checkPropertyExistence(EXTERNAL_APP_URL_PROP_KEY);
    checkPropertyExistence(EXTRACTED_ATTRIBS_PROP_KEY);
    checkPositiveIntegerIfPresent(IFRAME_WIDTH_PROP_KEY);
    checkPositiveIntegerIfPresent(IFRAME_LENGTH_PROP_KEY);
    checkBooleanIfPresent(ADD_HEADER_PROP_KEY);
  }

  @Override
  public void validateQuestion(Question question) throws WdkModelException {
    // make sure this question has a tabular reporter; needed to dump results
    if (question.getRecordClass().getReporterMap().get(TABULAR_REPORTER_NAME) == null) {
      throw new WdkModelException("A RecordClass using the " + getClass().getSimpleName() +
          " must also be configured with a tabular reporter under the name '" +
          TABULAR_REPORTER_NAME + "'.");
    }
  }

  @Override
  public Object getResultViewModel() throws WdkModelException {
    return new ViewModel(
        getProperty(EXTERNAL_APP_URL_PROP_KEY),
        chooseSize(IFRAME_WIDTH_PROP_KEY, DEFAULT_IFRAME_WIDTH_PX),
        chooseSize(IFRAME_LENGTH_PROP_KEY, DEFAULT_IFRAME_HEIGHT_PX));
  }

  private int chooseSize(String propName, int defaultValue) {
    String prop = getProperty(propName);
    return (prop != null && !prop.isEmpty()) ? Integer.parseInt(prop) : defaultValue;
  }

  @Override
  public ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log)
      throws WdkModelException, WdkUserException {
    String hasHeader = getProperty(ADD_HEADER_PROP_KEY);
    if (hasHeader == null || hasHeader.isEmpty())
      hasHeader = String.valueOf(ADD_HEADER_BY_DEFAULT);
    Reporter reporter = answerValue.createReport(TABULAR_REPORTER_NAME, new MapBuilder<String,String>()
        .put(StandardReporter.Configuration.ATTACHMENT_TYPE, "text")
        .put(TabularReporter.FIELD_HAS_HEADER, hasHeader.toLowerCase())
        .put(TabularReporter.FIELD_DIVIDER, FormatUtil.TAB)
        .put(TabularReporter.FIELD_SELECTED_COLUMNS, getProperty(EXTRACTED_ATTRIBS_PROP_KEY))
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
