package org.gusdb.wdk.model.answer.stream;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.RecordInstance;

public class PagedAnswerRecordStream implements RecordStream {

  private static final Logger LOG = Logger.getLogger(PagedAnswerRecordStream.class);

  public static final int MAX_IN_MEMORY_PAGE_SIZE = 500;

  private final AnswerValue _answerValue;
  private final int _pageSize;

  public PagedAnswerRecordStream(AnswerValue answerValue, int pageSize) {
    _answerValue = answerValue;
    _pageSize = pageSize;
  }

  @Override
  public Iterator<RecordInstance> iterator() {
    try {
      return new PagedAnswerRecordIterator(new PagedAnswerIterator(_answerValue, _pageSize));
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to create paged answer iterator", e);
    }
  }

  @Override
  public void close() {
    // nothing to do here
  }

  private static class PagedAnswerIterator implements Iterator<AnswerValue> {

    private final AnswerValue _baseAnswer;
    private final int _endIndex;
    private final int _pageSize;

    // will be updated with each call to next()
    private int _startIndex;

    public PagedAnswerIterator(AnswerValue answerValue, int pageSize)
        throws WdkModelException {
      _baseAnswer = answerValue;
      int resultSize = _baseAnswer.getResultSizeFactory().getResultSize();
      _startIndex = answerValue.getStartIndex();
      // determine the end index, which should be no bigger result size, since the index starts from 1
      int avEndIndex = _baseAnswer.getEndIndex();
      _endIndex = (avEndIndex == -1 /* all records */ || avEndIndex > resultSize ? resultSize : avEndIndex);
      _pageSize = pageSize;
    }

    @Override
    public boolean hasNext() {
      return (_startIndex <= _endIndex);
    }

    @Override
    public AnswerValue next() {
      // decide the new end index for the page answer
      int pageEndIndex = Math.min(_endIndex, _startIndex + _pageSize - 1);

      LOG.debug("Getting records #" + _startIndex + " to #" + pageEndIndex);

      AnswerValue answerValue = _baseAnswer.cloneWithNewPaging(_startIndex, pageEndIndex);

      // update the current index
      _startIndex = pageEndIndex + 1;
      return answerValue;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("This functionality is not implemented.");
    }
  }
}
