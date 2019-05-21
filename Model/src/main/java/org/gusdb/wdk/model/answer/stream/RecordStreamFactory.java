package org.gusdb.wdk.model.answer.stream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.single.SingleRecordAnswerValue;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

public class RecordStreamFactory {

  private static final Logger LOG = LogManager.getLogger(RecordStreamFactory.class);

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
    final RecordStream out;
    try {
      // first, check if this is a single-record answer; if so, return iterator over one dynamic record
      if (answerValue instanceof SingleRecordAnswerValue)
        out = new SingleRecordStream((SingleRecordAnswerValue) answerValue);

      // if result is smaller than maxPageSize, load entire answer into memory and lazy load attributes/tables
      else if (answerValue.getResultSizeFactory().getResultSize() <= MAX_PAGE_SIZE)
        out = new PagedAnswerRecordStream(answerValue,
          answerValue.getResultSizeFactory().getResultSize());

      else if (hasSingleAttrQuery(answerValue, attributes, tables))
        out = new SingleAttributeRecordStream(answerValue, attributes);

      // otherwise, use file-based; most efficient method for large results where we already know attrs/tables
      else
        out = new FileBasedRecordStream(answerValue, attributes, tables);
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Unable to instantiate record stream", e);
    }

    LOG.info("Using record stream " + out.getClass());

    return out;
  }

  private static boolean hasSingleAttrQuery(
    AnswerValue answerValue,
    Collection<AttributeField> fields,
    Collection<TableField> tables
  ) {
    final Iterator<String> names = Stream.concat(
      answerValue.getSortingColumns().stream(),
      Stream.concat(
        fields.stream(),
        tables.stream()
          .flatMap(t -> Arrays.stream(t.getAttributeFields()))))
      .filter(QueryColumnAttributeField.class::isInstance)
      .map(QueryColumnAttributeField.class::cast)
      .map(QueryColumnAttributeField::getColumn)
      .map(Column::getQuery)
      .map(Query::getFullName)
      .iterator();

    return names.hasNext() && hasSingleAttrQuery(names.next(), names);
  }

  private static boolean hasSingleAttrQuery(
    final String name,
    final Iterator<String> names
  ) {
    while (names.hasNext()) {
      if (!name.equals(names.next()))
        return false;
    }
    return true;
  }
}
