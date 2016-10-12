package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONObject;

public abstract class BaseTabularReporter extends StandardReporter {

  public static final String FIELD_HAS_HEADER = "includeHeader";
  public static final String FIELD_DIVIDER = "divider";

  protected static final long MAX_EXCEL_LENGTH = 1024 * 1024 * 10;

  protected boolean _includeHeader = true;
  protected String _divider = "\t";

  protected BaseTabularReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public void configure(Map<String, String> config) throws WdkUserException {
    super.configure(config);

    // get basic configurations
    if (config.containsKey(FIELD_HAS_HEADER)) {
      String value = config.get(FIELD_HAS_HEADER);
      _includeHeader = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"));
    }

    if (config.containsKey(FIELD_DIVIDER)) {
      _divider = config.get(FIELD_DIVIDER);
    }
  }

  @Override
  public void configure(JSONObject config) throws WdkUserException {
    super.configure(config);
    _includeHeader = (config.has(FIELD_HAS_HEADER) ? config.getBoolean(FIELD_HAS_HEADER) : true);
  }

  @Override
  public String getHttpContentType() {
    switch (getStandardConfig().getAttachmentType()) {
      case "text":
        return "text/plain";
      case "excel":
        return "application/vnd.ms-excel";
      case "pdf":
        return "application/pdf";
      default:
        return super.getHttpContentType();
    }
  }

  @Override
  public String getDownloadFileName() {
    return getDownloadFileName(getQuestion().getName());
  }

  protected String getDownloadFileName(String baseName) {
    String suffix = getFileNameSuffix();
    switch (getStandardConfig().getAttachmentType()) {
      case "text":
        return baseName + "_" + suffix + ".txt";
      case "excel":
        return baseName + "_" + suffix + ".xls";
      case "pdf":
        return baseName + "_" + suffix + ".pdf";
      default:
        return super.getDownloadFileName();
    }
  }

  protected String getFileNameSuffix() {
    return "Summary";
  }

  @Override
  public void write(OutputStream out) throws WdkModelException {
    try {
      // get the formatted result
      switch (getStandardConfig().getAttachmentType()) {
        case "excel":
          format2Excel(out);
          break;
        case "pdf":
          format2PDF(out);
          break;
        default:
          format2Text(out);
      }
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Unable to write tabular report", e);
    }
  }

  protected abstract void format2Text(OutputStream out) throws WdkModelException, WdkUserException;

  protected abstract void format2PDF(OutputStream out) throws WdkModelException, WdkUserException;

  protected abstract void format2Excel(OutputStream out) throws WdkModelException, WdkUserException;

}
