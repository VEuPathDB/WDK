package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;

public abstract class AbstractReporter implements Reporter {

  protected AnswerValue _baseAnswer;
  protected WdkModel _wdkModel;
  protected Map<String, String> _properties;

  /**
   * Writes reporter result to the passed output stream
   */
  protected abstract void write(OutputStream out) throws WdkModelException;

  protected AbstractReporter(AnswerValue answerValue) {
    _baseAnswer = answerValue;
    _wdkModel = answerValue.getWdkModel();
  }

  @Override
  public void setProperties(ReporterRef reporterRef) throws WdkModelException {
    _properties = new HashMap<>(reporterRef.getProperties());
  }

  public String getPropertyInfo() {
    return FormatUtil.prettyPrint(_properties, Style.MULTI_LINE);
  }

  protected int getResultSize() throws WdkModelException {
    return _baseAnswer.getResultSizeFactory().getResultSize();
  }

  @Override
  public String getHttpContentType() {
    // by default, generate result in plain text format
    return "text/plain";
  }

  @Override
  public String getDownloadFileName() {
    // by default, display the result in the browser, by setting the file name as null
    return null;
  }

  @Override
  public ContentDisposition getContentDisposition() {
    return (getDownloadFileName() == null ?
        ContentDisposition.INLINE : ContentDisposition.ATTACHMENT);
  }

  @Override
  public String getHelp() {
    return "This reporter is not documented.";
  }

  @Override
  public void report(OutputStream out) throws WdkModelException {
    initialize();
    try {
      write(out);
    }
    finally {
      complete();
    }
  }

  /**
   * Performs any setup needed before calling the write method
   * 
   * @throws WdkModelException if something goes wrong during initialization
   */
  protected void initialize() throws WdkModelException {
    // by default do nothing
  }

  /**
   * Performs any teardown needed after calling the write method
   */
  protected void complete() {
    // by default do nothing
  }

  // =========================================================================
  // provide the wrapper methods to answer object, in order not to expose the
  // answer itself to avoid accidental changes on the base answer. The record
  // access to the answer should be through the page answer iterator
  // =========================================================================

  protected Question getQuestion() {
    return _baseAnswer.getAnswerSpec().getQuestion();
  }

}
