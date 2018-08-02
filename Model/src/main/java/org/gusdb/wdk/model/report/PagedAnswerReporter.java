package org.gusdb.wdk.model.report;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.stream.PagedAnswerRecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStream;

/**
 * A PagedReporter outputs a result in a paged fashion in order to avoid memory problems.  To do so, it
 * iterates over a series of AnswerValues it generates, each of which represents a page of records.
 * 
 * The default page size is 200 records.
 * 
 * @author rdoherty
 */
public abstract class PagedAnswerReporter extends AbstractReporter {

  public static final String PROPERTY_PAGE_SIZE = "page_size";

  private static final int DEFAULT_PAGE_SIZE = 200;

  protected int _pageSize = DEFAULT_PAGE_SIZE;

  protected PagedAnswerReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public void setProperties(ReporterRef reporterRef) throws WdkModelException {
    super.setProperties(reporterRef);
    if (_properties.containsKey(PROPERTY_PAGE_SIZE)) {
      try {
        _pageSize = Integer.valueOf(_properties.get(PROPERTY_PAGE_SIZE));
      }
      catch (NumberFormatException e) {
        throw new WdkModelException("Reporter property '" + PROPERTY_PAGE_SIZE + "' must be a positive integer.");
      }
    }
  }

  public RecordStream getRecords() {
    return new PagedAnswerRecordStream(_baseAnswer, _pageSize);
  }

  @Override
  public String getHelp() {
    return super.getHelp() + "Optional property: '" + PROPERTY_PAGE_SIZE +
        "': specifies page size, defaults to " + DEFAULT_PAGE_SIZE;
  }

}
