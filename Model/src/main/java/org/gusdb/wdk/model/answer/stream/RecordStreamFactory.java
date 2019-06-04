package org.gusdb.wdk.model.answer.stream;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.single.SingleRecordAnswerValue;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.apache.log4j.LogManager.getLogger;

public class RecordStreamFactory {

  private final static Logger LOG = getLogger(RecordStreamFactory.class);

  private static final int MAX_PAGE_SIZE = PagedAnswerRecordStream.MAX_IN_MEMORY_PAGE_SIZE;

  /**
   * Uses arguments to make a best guess at the most efficient RecordStream
   * implementation for this AnswerValue, creates the appropriate stream, and
   * returns it.  There are three possible return values:
   *
   * <ol>
   * <li>If answerValue is a SingleRecordAnswerValue, a
   *     SingleRecordStream is returned
   * <li>If result size of answerValue is <= passed
   *     maxPageSize, then the answerValue's paging is set to that a single
   *     (first) page of that size and a PageAnswerRecordStream is returned
   * <li>Otherwise, a FileBasedRecordStream is returned
   * </ol>
   *
   * @param answerValue
   *   answer value for which stream should be produced
   * @param attributes
   *   attributes to be collected from records returned from the stream
   * @param tables
   *   tables to be collected from records returned from the stream
   *
   * @return most appropriate RecordStream given the passed arguments
   *
   * @throws WdkModelException
   *   if anything goes wrong
   */
  public static RecordStream getRecordStream(
    AnswerValue answerValue,
    Collection<AttributeField> attributes,
    Collection<TableField> tables
  ) throws WdkModelException {
    final RecordStream out;
    try {
      // first, check if this is a single-record answer; if
      // so, return iterator over one dynamic record
      if (answerValue instanceof SingleRecordAnswerValue)
        out = new SingleRecordStream((SingleRecordAnswerValue) answerValue);

      // if result is smaller than maxPageSize, load entire
      // answer into memory and lazy load attributes/tables
      else if (answerValue.getResultSizeFactory().getResultSize() <= MAX_PAGE_SIZE)
        out = new PagedAnswerRecordStream(answerValue,
          answerValue.getResultSizeFactory().getResultSize());

      else if (requiresExactlyOneAttrQuery(answerValue, attributes, tables, true) &&
               answerValue.entireResultRequested())
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

  private static boolean requiresExactlyOneAttrQuery(
    AnswerValue answerValue,
    Collection<AttributeField> fields,
    Collection<TableField> tables,
    boolean includeSortingColumnsInCalculation
  ) throws WdkModelException {
    if (!tables.isEmpty()) {
      return false;
    }
    var fieldsToConsider = new ArrayList<>(fields);
    if (includeSortingColumnsInCalculation) {
      fieldsToConsider.addAll(answerValue.getSortingColumns()
          .stream().map(SortDirectionSpec::getItem).collect(Collectors.toList()));
    }
    return FileBasedRecordStream.requiresExactlyOneAttributeQuery(fieldsToConsider);
  }
}
