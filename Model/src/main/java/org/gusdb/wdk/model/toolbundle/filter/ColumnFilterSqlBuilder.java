package org.gusdb.wdk.model.toolbundle.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.DerivedAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.toolbundle.config.ColumnConfig;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSet;

/**
 * Builder for an SQL query with {@link ColumnFilter}s applied.
 * <p>
 * Takes an input ID query and appends an SQL {@code INTERSECT} statement to it
 * for each unique {@link Query} backing the filtered {@link AttributeField}s
 * on the {@link Question} backing the input {@link AnswerValue}.
 */
public class ColumnFilterSqlBuilder {

  private final AnswerValue answer;
  private final Question question;
  private final ColumnFilterConfigSet configs;

  private ColumnFilterSqlBuilder(AnswerValue answer) {
    final var spec = answer.getAnswerSpec();

    this.answer    = answer;
    this.question  = spec.getQuestion();
    this.configs   = spec.getColumnFilterConfig();
  }

  /**
   * Constructs a new instance of  {@link ColumnFilterSqlBuilder} and calls
   * {@link #build(String)}.
   *
   * @param answer
   *   answer used for context when constructing the output SQL
   * @param idSql
   *   SQL passed to {@link #build(String)}
   *
   * @return value returned by {@link #build(String)}
   *
   * @throws WdkModelException
   *   may be thrown by {@link #build(String)}
   */
  public static String buildFilteredSql(
    final AnswerValue answer,
    final String idSql
  ) throws WdkModelException {
    return new ColumnFilterSqlBuilder(answer).build(idSql);
  }

  /**
   * Constructs a new SQL query out of the given {@code idSql} joined with
   * the intersection of each unique attribute {@link Query} backing the
   * {@link #question}'s {@link AttributeField}s.
   * <p>
   * This builder will check each {@code AttributeField} retrieved from the
   * target {@link Question} against the the {@link ColumnFilterConfigSet}
   * ({@link #configs}) to filter out {@code AttributeField}s and {@code Query}s
   * that are not being used in filters.
   * <p>
   * If an {@code AttributeField} <i>is</i> being used for a {@link
   * ColumnFilter}, then its backing query will be used to construct an SQL
   * block on which the filters for that {@code AttributeFilter}/{@code Query}
   * can be applied.  These blocks will be {@code INTERSECT}ed together, then
   * joined to the original {@code idSql} so as not to lose any dynamic columns
   * returned by the {@code idSql}.
   * <p>
   * Each attribute {@code Query} will contribute only a single
   * {@code INTERSECT} block and {@code AttributeField}s that share this
   * {@code Query} will each have their filters applied to that {@code Query}'s
   * {@code INTERSECT} block as where clauses.
   *
   * @param idSql
   *   idSql to intersect on to apply the configured {@code ColumnFilter}s
   *
   * @return a new SQL query consisting of the input SQL and a series of 0 or
   * more {@code INTERSECT} blocks used to filter that query.
   *
   * @throws WdkModelException
   *   may be thrown by {@link #retrieveBuilder(AttributeField)} or {@link
   *   ColumnFilter#build()}.
   */
  private String build(final String idSql) throws WdkModelException {

    if (configs.getColumnConfigs().isEmpty()) return idSql;

    final var allFields = question.getAttributeFieldMap();

    final var out = new StringBuilder("SELECT idsqlcf.* FROM ( " + idSql + " ) idsqlcf, (");

    boolean isFirstIntersectBlock = true;

    for (String columnName : configs.getColumnConfigs().keySet()) {
      AttributeField field = allFields.get(columnName);
      if (field == null) {
        throw new WdkModelException("Column '" + columnName + "' was configured " +
            "as a column filter but that column does not exist on question " + question.getFullName());
      }

      if (isFirstIntersectBlock)
        isFirstIntersectBlock = false;
      else
        out.append("\nINTERSECT\n");

      StringBuilder attributeQueryBlock = startQuery(getAttributeSql(getQueryName(field)));

      ColumnConfig conf = configs.getColumnConfig(columnName);

      for (final var entry : conf.entrySet()) {
        final var filter = field.makeFilterInstance(entry.getKey(), answer, entry.getValue()).get();
        attributeQueryBlock
           .append("  AND ")
           .append(filter.buildSqlWhere())
           .append(" /* ")
           .append(filter.getClass().getSimpleName())
           .append(" */\n");
      }

      // close paren added at beginning by startQuery(), then add block to out
      attributeQueryBlock.append(" )");
      out.append(attributeQueryBlock);
      
    }

    // close paren and label intersection subquery
    out.append(" ) joinedfilterblocks ")

    // join on PK cols
    .append("WHERE ")
    .append(question.getRecordClass().getPrimaryKeyDefinition().createJoinClause("idsqlcf", "joinedfilterblocks"));

    return out.toString();
  }

  /**
   * Builds the start of an attribute query SQL block for the given SQL, to
   * which conditions will be added for each filter config.
   * <p>
   * Creates a new {@link StringBuilder} containing a {@code SELECT}, {@code
   * FROM}, and the start of a {@code WHERE} clause which can be appended to to
   * complete the {@code INTERSECT} block for the given SQL.
   *
   * @param attrSql
   *   SQL for which to construct a new {@code INTERSECT} SQL block.
   *
   * @return a {@code StringBuilder} prepared for individual filter {@code
   * WHERE} predicates to be appended.
   */
  private StringBuilder startQuery(final String attrSql) {
    return new StringBuilder("(\nSELECT\n  ")
      .append(buildPkColString(question))
      .append("\nFROM\n  (")
      .append(attrSql)
      .append("\n  ) attr\nWHERE\n  1 = 1\n");
  }

  /**
   * Retrieves the backing SQL for the {@link Query} matching the given name
   * with macros resolved.
   * <p>
   * The {@code Query} instances from the {@link AttributeField}s are not used
   * due to post processing performed during the WDK startup, instead the
   * reference is re-resolved to get the unmodified SQL from the model XML.
   * <p>
   * The {@link AnswerValue} will be used to resolve any macros ({@code
   * ##MACRO##}) present in the SQL.
   *
   * @param name
   *   full name of the {@code Query} to retrieve the SQL for.
   *
   * @return The SQL string for the {@code Query} matching the name provided.
   *
   * @throws WdkModelException
   *   if the full name is invalid, or if another internal model error occurs
   *   during the resolution of the macros in the SQL.
   */
  private String getAttributeSql(final String name) throws WdkModelException {
    return answer.getUnfilteredAttributeSql((Query) answer.getWdkModel()
      .resolveReference(name));
  }

  /**
   * Builds an SQL safe list of primary key columns for the {@code SELECT}
   * clause of the query being assembled by this builder.
   *
   * @param question
   *   question used to lookup the primary key columns for the backing {@link
   *   RecordClass}
   *
   * @return SQL safe comma separated list of primary key columns.
   */
  private static String buildPkColString(final Question question) {
    return String.join(", ", question.getRecordClass()
      .getPrimaryKeyDefinition()
      .getColumnRefs());
  }

  /**
   * Retrieves the full name of the attribute {@link Query} backing the given
   * {@link AttributeField}.
   * <p>
   * The given {@code AttributeField} must be an instance of {@link
   * QueryColumnAttributeField} or a runtime {@link ClassCastException} will be
   * thrown.
   *
   * @param field
   *   field from which to get name of the backing {@code Query}
   *
   * @return full name of the given {@code AttributeField}'s backing query
   */
  private static String getQueryName(final AttributeField field) {
    return (field instanceof QueryColumnAttributeField
      ? (QueryColumnAttributeField) field
      : ((DerivedAttributeField) field).getFilterDependencyField()
        .orElseThrow(IllegalStateException::new)
    )
      .getColumn()
      .getQuery()
      .getFullName();
  }
}
