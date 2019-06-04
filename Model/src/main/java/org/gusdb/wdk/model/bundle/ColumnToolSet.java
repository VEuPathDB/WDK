package org.gusdb.wdk.model.bundle;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;

import java.util.Optional;

public interface ColumnToolSet {

  /**
   * @return the unique name of this set of tools.
   */
  String getName();

  /**
   * Checks whether or not this tool set has any filters able to handle the
   * given {@link AttributeField}.
   *
   * @param type
   *   type for which to check for filters.
   *
   * @return whether or not this tool set has a filter matching the given type.
   */
  boolean hasFilterFor(AttributeField type);

  /**
   * Build a filter for the given {@link AttributeField}.
   *
   * @param field
   *   attribute field for which a filter should be built
   * @param val
   *   answer value that the filter will be based on
   * @param config
   *   input config containing runtime options for the built filter
   *
   * @return a constructed attribute field filter
   */
  Optional<ColumnFilter> prepareFilterFor(
    AttributeField field,
    AnswerValue val,
    ColumnToolConfig config
  );

  Optional<ColumnFilter> getFilterFor(AttributeField field);

  /**
   * Checks whether or not this tool set has any reporters able to handle the
   * given {@link AttributeField}.
   *
   * @param type
   *   type for which to check for reporters.
   *
   * @return whether or not this tool set has a reporter matching the given
   * type.
   */
  boolean hasReporterFor(AttributeField type);

  /**
   * Build a reporter for the given {@link AttributeField}.
   *
   * @param field
   *   attribute field for which a reporter should be built
   * @param val
   *   answer value that the reporter will be based on
   * @param config
   *   input config containing runtime options for the built reporter
   *
   * @return a result of either an exception or a constructed attribute field
   * reporter
   */
  Optional<ColumnReporter> prepareReporter(
    AttributeField field,
    AnswerValue val,
    ColumnToolConfig config
  );

  Optional<ColumnReporter> getReporterFor(AttributeField field);
}
