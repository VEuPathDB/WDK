package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.wdk.model.WdkModelException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.RodsGenQueryEnum;

import static java.lang.String.format;

/**
 * iCAT query helpers and utilities.
 */
class Util {

  /**
   * Exception message formats
   */
  private static final String
    ERR_VALUE_BY_COL = "Failed to pull data for column %s from iCAT query"
      + " result set.",
    ERR_VALUE_BY_INDEX = "Failed to pull data for column %d from iCAT query"
      + " result set.";

  /**
   * Retrieves the string value from the given row at the given column, wrapping
   * any iRODS library exception as s WdkModelException.
   *
   * @param row
   *   row from which a string value should be retrieved
   * @param col
   *   column for which a string value should be retrieved
   *
   * @return string value contained at column {@code col} in row {@code row}
   *
   * @throws WdkModelException
   *   if a Jargon library exception is thrown while attempting to look up the
   *   requested value.
   */
  static String getString(
    final IRODSQueryResultRow row,
    final RodsGenQueryEnum    col
  ) throws WdkModelException {
    try {
      return row.getColumn(col.getName());
    } catch (JargonException e) {
      throw new WdkModelException(format(ERR_VALUE_BY_COL, col.getName()), e);
    }
  }

  /**
   * Retrieves the string value from the given row at the given column index,
   * wrapping any iRODS library exception as s WdkModelException.
   *
   * @param row
   *   row from which a string value should be retrieved
   * @param col
   *   column index for which a string value should be retrieved
   *
   * @return string value contained at column {@code col} in row {@code row}
   *
   * @throws WdkModelException
   *   if a Jargon library exception is thrown while attempting to look up the
   *   requested value.
   */
  static String getString(final IRODSQueryResultRow row, final int col)
  throws WdkModelException {
    try {
      return row.getColumn(col);
    } catch (JargonException e) {
      throw new WdkModelException(format(ERR_VALUE_BY_INDEX, col), e);
    }
  }

  /**
   * Retrieves the long value from the given row at the given column, wrapping
   * any iRODS library exception as s WdkModelException.
   *
   * @param row
   *   row from which a long value should be retrieved
   * @param col
   *   column for which a long value should be retrieved
   *
   * @return long value contained at column {@code col} in row {@code row}
   *
   * @throws WdkModelException
   *   if a Jargon library exception is thrown while attempting to look up the
   *   requested value.
   */
  static long getLong(
    final IRODSQueryResultRow row,
    final RodsGenQueryEnum    col
  ) throws WdkModelException {
    try {
      return row.getColumnAsLongOrZero(col.getName());
    } catch (JargonException e) {
      throw new WdkModelException(format(ERR_VALUE_BY_COL, col.getName()), e);
    }
  }
}
