package org.gusdb.wdk.model.toolbundle;

import java.io.OutputStream;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.report.Reporter;

public interface ColumnReporterInstance extends ColumnToolInstance, Reporter {

  /**
   * Performs the processing of the result
   */
  public interface ColumnProcessor {

    /**
     * Initialize the processor and write any preliminary data (e.g. a header)
     * @param out stream to which any output should be written
     * @throws WdkModelException if error occurs
     */
    default void initialize(OutputStream out) throws WdkModelException {}

    /**
     * Process the column value of a single row; optionally write data to output
     * @param value column value of the next row in the result
     * @param out stream to which any output should be written
     * @throws WdkModelException if error occurs
     */
    void processValue(String value, OutputStream out) throws WdkModelException;

    /**
     * Complete the processing and write any final data to the output (e.g. aggregated statistics)
     * @param out stream to which any output should be written
     * @throws WdkModelException if error occurs
     */
    default void complete(OutputStream out) throws WdkModelException {}
  }

}
