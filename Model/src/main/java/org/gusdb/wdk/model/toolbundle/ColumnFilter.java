package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.WdkModelException;

/**
 * A filter that operates on a single column of data.
 */
public interface ColumnFilter extends ColumnTool {

  /**
   * Creates and returns a copy of this ColumnFilter
   * instance that will contain the same configuration.
   *
   * @return a configured copy of this ColumnFilter.
   */
  @Override
  ColumnFilter copy();

  /**
   * Constructs a configured instance of {@link SqlBuilder}
   *
   * @return constructed {@link SqlBuilder}
   *
   * @throws WdkModelException
   *   implementations may throw this in the event of an
   *   internal error.  See specific implementations for
   *   further details.
   */
  SqlBuilder build() throws WdkModelException;

  /**
   * A builder which can be used to construct one or more
   * predicates for an SQL {@code WHERE} clause.
   */
  interface SqlBuilder {

    /**
     * @return an SQL {@code WHERE} clause.
     */
    String buildSqlWhere();
  }
}
