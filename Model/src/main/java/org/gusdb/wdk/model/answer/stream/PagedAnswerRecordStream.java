package org.gusdb.wdk.model.answer.stream;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.RecordInstance;

public class PagedAnswerRecordStream implements RecordStream {

  private static final Logger LOG = Logger.getLogger(PagedAnswerRecordStream.class);

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
    catch (WdkModelException | WdkUserException e) {
      throw new WdkRuntimeException("Unable to create paged answer iterator", e);
    }
  }

  @Override
  public void close() {
    // nothing to do here
  }

  private static class PagedAnswerIterator implements Iterator<AnswerValue> {

    private static final int SORTING_THRESHOLD = 100;

    private final AnswerValue _baseAnswer;
    private final int _endIndex;
    private final int _pageSize;
    private final boolean _disableSorting;

    // will be updated with each call to next()
    private int _startIndex;

    public PagedAnswerIterator(AnswerValue answerValue, int pageSize)
        throws WdkModelException, WdkUserException {
      _baseAnswer = answerValue;
      int resultSize = _baseAnswer.getResultSize();
      _startIndex = answerValue.getStartIndex();
      // determine the end index, which should be no bigger result size, since the index starts from 1
      int avEndIndex = _baseAnswer.getEndIndex();
      _endIndex = (avEndIndex == -1 /* all records */ || avEndIndex > resultSize ? resultSize : avEndIndex);
      _pageSize = pageSize;
      _disableSorting = (resultSize > SORTING_THRESHOLD);
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

      // disable sorting if the total size is bigger than threshold
      if (_disableSorting) {
        try {
          answerValue.setSortingMap(new LinkedHashMap<String, Boolean>());
        }
        catch (WdkModelException ex) {
          throw new WdkRuntimeException(ex);
        }
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
}
