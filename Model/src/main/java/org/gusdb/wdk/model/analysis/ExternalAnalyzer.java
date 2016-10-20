package org.gusdb.wdk.model.analysis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.report.AbstractTabularReporter;
import org.gusdb.wdk.model.report.AttributesTabularReporter;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.StandardConfig;
import org.gusdb.wdk.model.report.TableTabularReporter;
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

  private static final Logger LOG = Logger.getLogger(ExternalAnalyzer.class);

  // this plugin can only be assigned to questions/recordClasses that
  //   are configured with a tabular reporter
  protected static final String TABULAR_REPORTER_NAME = "tabular";
  protected static final String TABLE_TABULAR_REPORTER_NAME = "tableTabular";
  
  protected static final String EXTRACTED_ATTRIBS_PROP_KEY = "attributesToExtract";
  protected static final String EXTRACTED_TABLES_PROP_KEY = "tablesToExtract";
  protected static final String ADD_HEADER_PROP_KEY = "addHeader";

  protected static final String EXTERNAL_APP_URL_PROP_KEY = "externalAppUrl";
  protected static final String IFRAME_WIDTH_PROP_KEY = "iframeWidthPx";
  protected static final String IFRAME_LENGTH_PROP_KEY = "iframeLengthPx";

  protected static final String FILE_NAME_SUFFIX = ".tab";
  protected static final String ATTRIBUTES_FILE_NAME = "attributes" + FILE_NAME_SUFFIX;

  protected static final int DEFAULT_IFRAME_WIDTH_PX = 900;
  protected static final int DEFAULT_IFRAME_HEIGHT_PX = 450;
  protected static final boolean ADD_HEADER_BY_DEFAULT = true;

  public static class ViewModel {

    private String _iframeBaseUrl;
    private final int _iframeWidth;
    private final int _iframeHeight;

    public ViewModel(String iframeBaseUrl, int width, int height) {
      _iframeBaseUrl = iframeBaseUrl;
      _iframeWidth = width;
      _iframeHeight = height;
    }

    public void setIframeBaseUrl(String url) { _iframeBaseUrl = url; }

    public String getIframeBaseUrl() { return _iframeBaseUrl; }
    public String getDownloadPath() { return ATTRIBUTES_FILE_NAME; }
    public int getIframeWidth() { return _iframeWidth; }
    public int getIframeHeight() { return _iframeHeight; }
  }

  @Override
  public void validateProperties() throws WdkModelException {
    checkPropertyExistence(EXTERNAL_APP_URL_PROP_KEY);
    checkAtLeastOneExists(EXTRACTED_ATTRIBS_PROP_KEY, EXTRACTED_TABLES_PROP_KEY);
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

  protected int chooseSize(String propName, int defaultValue) {
    String prop = getProperty(propName);
    return (prop != null && !prop.isEmpty()) ? Integer.parseInt(prop) : defaultValue;
  }

  @Override
  public ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log)
      throws WdkModelException, WdkUserException {
    String hasHeader = getProperty(ADD_HEADER_PROP_KEY);
    if (hasHeader == null || hasHeader.isEmpty()) {
      hasHeader = String.valueOf(ADD_HEADER_BY_DEFAULT);
    }
    String storageDir = getStorageDirectory().toAbsolutePath().toString();

    // configure tabular reporter if attributes requested in config
    String attributes = getProperty(EXTRACTED_ATTRIBS_PROP_KEY);
    if (attributes != null && !attributes.trim().isEmpty()) {
      Reporter reporter = new AttributesTabularReporter(answerValue);
      reporter.configure(getConfig(StandardConfig.SELECTED_FIELDS, attributes.trim(), hasHeader));
      writeReport(reporter, Paths.get(storageDir, ATTRIBUTES_FILE_NAME));
    }

    // get array of requested tables
    String tablesStr = getProperty(EXTRACTED_TABLES_PROP_KEY);
    if (tablesStr != null && !tablesStr.trim().isEmpty()) {
      String[] tables = tablesStr.split(",");
      for (String table : tables) {
        table = table.trim();
        if (table.isEmpty()) {
          LOG.warn("Empty table name specified.  Skipping...");
          continue;
        }
        Reporter reporter = new TableTabularReporter(answerValue);
        reporter.configure(getConfig(StandardConfig.SELECTED_TABLES, table, hasHeader));
        writeReport(reporter, Paths.get(storageDir, table + FILE_NAME_SUFFIX));
      }
    }

    return ExecutionStatus.COMPLETE;
  }

  private Map<String, String> getConfig(String reportSpecificKey, String reportSpecificValue, String hasHeader) {
    return new MapBuilder<String,String>()
        .put(StandardConfig.ATTACHMENT_TYPE, "text")
        .put(AbstractTabularReporter.FIELD_HAS_HEADER, hasHeader)
        .put(AbstractTabularReporter.FIELD_DIVIDER, FormatUtil.TAB)
        .put(reportSpecificKey, reportSpecificValue)
        .toMap();
  }

  private static void writeReport(Reporter reporter, Path outputFile) throws WdkModelException {
    // write query results to disk
    try (FileOutputStream fileOut = new FileOutputStream(outputFile.toFile())) {
      reporter.report(fileOut);
    }
    catch (IOException | JSONException e) {
      throw new WdkModelException("Unable to dump analysis files to data storage dir", e);
    }
  }
}
