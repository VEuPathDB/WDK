package org.gusdb.wdk.model.answer.stream;

import java.util.Collection;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.single.SingleRecordAnswerValue;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public class RecordStreamFactory {

  private static final int MAX_PAGE_SIZE = PagedAnswerRecordStream.MAX_IN_MEMORY_PAGE_SIZE;

  /**
   * Uses arguments to make a best guess at the most efficient RecordStream implementation for this
   * AnswerValue, creates the appropriate stream, and returns it.  There are three possible return values:
   * 
   * 1. If answerValue is a SingleRecordAnswerValue, a SingleRecordStream is returned
   * 2. If result size of answerValue is <= passed maxPageSize, then the answerValue's paging is set to that
   *    a single (first) page of that size and a PageAnswerRecordStream is returned
   * 3. Otherwise, a FileBasedRecordStream is returned
   * 
   * @param answerValue answer value for which stream should be produced
   * @param attributes attributes to be collected from records returned from the stream
   * @param tables tables to be collected from records returned from the stream
   * @return most appropriate RecordStream given the passed arguments
   * @throws WdkModelException if anything goes wrong
   */
  public static RecordStream getRecordStream(AnswerValue answerValue,
      Collection<AttributeField> attributes, Collection<TableField> tables) throws WdkModelException {
    try {
      // first, check if this is a single-record answer; if so, return iterator over one dynamic record
      if (answerValue instanceof SingleRecordAnswerValue) {
        return new SingleRecordStream((SingleRecordAnswerValue)answerValue);
      }
  
      // if result is smaller than maxPageSize, load entire answer into memory and lazy load attributes/tables
      if (answerValue.getResultSizeFactory().getResultSize() <= MAX_PAGE_SIZE) {
        return new PagedAnswerRecordStream(answerValue.cloneWithNewPaging(1, MAX_PAGE_SIZE), MAX_PAGE_SIZE);
      }
  
      // otherwise, use file-based; most efficient method for large results where we already know attrs/tables
      return new FileBasedRecordStream(answerValue, attributes, tables);
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Unable to instantiate record stream", e);
    }
  }
}
