package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Reporter is used to download the content of the answerValue between the start & end indexes. It will
 * create multiple AnswerValues, one for each page.
 * 
 * The default page size is 100 records per page.
 * 
 * @author xingao
 * 
 */
public abstract class Reporter implements Iterable<AnswerValue> {

  public static enum ContentDisposition {
    INLINE, ATTACHMENT;
  }

  public static final String PROPERTY_PAGE_SIZE = "page_size";

  private static final int SORTING_THRESHOLD = 100;

  private final static Logger logger = Logger.getLogger(Reporter.class);
  
  protected int maxPageSize = 100;

  protected static class PageAnswerIterator implements Iterator<AnswerValue> {

    private AnswerValue _baseAnswer;
    private int _endIndex;
    private int _startIndex;
    private int _maxPageSize;
    private int _resultSize;

    public PageAnswerIterator(AnswerValue answerValue, int startIndex, int endIndex, int maxPageSize)
        throws WdkModelException, WdkUserException {
      this._baseAnswer = answerValue;

      // determine the end index, which should be no bigger result size,
      // since the index starts from 1
      _resultSize = _baseAnswer.getResultSize();
      this._endIndex = Math.min(endIndex, _resultSize);
      this._startIndex = startIndex;
      this._maxPageSize = maxPageSize;
    }

    @Override
    public boolean hasNext() {
      // if the current
      return (_startIndex <= _endIndex);
    }

    @Override
    public AnswerValue next() {
      // decide the new end index for the page answer
      int pageEndIndex = Math.min(_endIndex, _startIndex + _maxPageSize - 1);

      logger.debug("Getting records #" + _startIndex + " to #" + pageEndIndex);

      AnswerValue answerValue = new AnswerValue(_baseAnswer, _startIndex, pageEndIndex);

      // disable sorting if the total size is bigger than threshold
      if (_resultSize > SORTING_THRESHOLD)
        try {
          answerValue.setSortingMap(new LinkedHashMap<String, Boolean>());
        }
        catch (WdkModelException ex) {
          throw new WdkRuntimeException(ex);
        }

      // update the current index
      _startIndex = pageEndIndex + 1;
      return answerValue;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("This functionality is not implemented.");
    }

  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public abstract String getConfigInfo();

  public String getPropertyInfo() {
    StringBuilder propInfo = new StringBuilder();
    for (String propName : properties.keySet()) {
      propInfo.append(propName + ": " + properties.get(propName));
      propInfo.append(System.getProperty("line.separator"));
    }
    return propInfo.toString();
  }

  public abstract void write(OutputStream out) throws WdkModelException, NoSuchAlgorithmException,
      SQLException, JSONException, WdkUserException;

  protected Map<String, String> properties;

  protected WdkModel wdkModel;

  protected AnswerValue baseAnswer;
  private int startIndex;
  private int endIndex;

  private String description = null;

  protected Reporter(AnswerValue answerValue, int startIndex, int endIndex) {
    this.baseAnswer = answerValue;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    wdkModel = answerValue.getQuestion().getWdkModel();
  }

  /**
   * @throws WdkModelException
   *           if error while setting properties on reporter
   */
  public void setProperties(Map<String, String> properties) throws WdkModelException {
    this.properties = properties;
    if (properties.containsKey(PROPERTY_PAGE_SIZE))
      maxPageSize = Integer.valueOf(properties.get(PROPERTY_PAGE_SIZE));
  }

  public int getResultSize() throws WdkModelException, WdkUserException {
    return this.baseAnswer.getResultSize();
  }

  public AnswerValue getAnswerValue() {
    return this.baseAnswer;
  }

  public void configure(Map<String, String> config) {
    
    if (config.containsKey(PROPERTY_PAGE_SIZE)) maxPageSize = Integer.valueOf(config.get(PROPERTY_PAGE_SIZE));
  }

  public void configure(JSONObject config) {
    
    if (config.has(PROPERTY_PAGE_SIZE)) maxPageSize = config.getInt(PROPERTY_PAGE_SIZE);
  }

  /**
   * Hook used to perform any setup needed before calling the write method.
   * 
   * @throws WdkModelException
   *           if error while initializing reporter
   */
  protected abstract void initialize() throws WdkModelException;

  /**
   * Hook used to perform any teardown needed after calling the write method.
   */
  protected abstract void complete();

  public void setWdkModel(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
  }

  public String getHttpContentType() {
    // by default, generate result in plain text format
    return "text/plain";
  }

  public String getDownloadFileName() {
    // by default, display the result in the browser, by setting the file
    // name as null
    return null;
  }

  // =========================================================================
  // provide the wrapper methods to answer object, in order not to expose the
  // answer itself to avoid accidental changes on the base answer. The record
  // access to the answer should be through the page answer iterator
  // =========================================================================

  /**
   * @return get the questions of the answer
   */
  protected Question getQuestion() {
    return baseAnswer.getQuestion();
  }

  protected Map<String, AttributeField> getSummaryAttributes() throws WdkModelException {
    return baseAnswer.getSummaryAttributeFieldMap();
  }

  @Override
  public Iterator<AnswerValue> iterator() {
    try {
      return new PageAnswerIterator(baseAnswer, startIndex, endIndex, maxPageSize);
    }
    catch (WdkModelException | WdkUserException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void report(OutputStream out) throws SQLException, WdkModelException, NoSuchAlgorithmException,
      WdkUserException, JSONException {
    initialize();
    try {
      // write header
      write(out);
    }
    finally {
      complete();
    }
  }

  public ContentDisposition getContentDisposition() {
    return (getDownloadFileName() == null ?
        ContentDisposition.INLINE : ContentDisposition.ATTACHMENT);
  }
}
