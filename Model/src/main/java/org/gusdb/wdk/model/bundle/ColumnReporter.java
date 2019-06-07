package org.gusdb.wdk.model.bundle;

import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.wdk.model.WdkModelException;

import java.io.OutputStream;

/**
 * A reporter that operates on a set of data from a single result column.
 *
 * @param <T>
 *   the type of data this column reporter operates on.
 */
public interface ColumnReporter<T> extends ColumnTool {

  /**
   * Creates and returns a copy of the current ColumnReporter instance
   *
   * @return a new ColumnReport instance containing the same configuration as
   * this instance.
   */
  @Override
  ColumnReporter<T> copy();

  /**
   * Returns a JSON Schema specification for the expected output format of this
   * reporter.
   *
   * @return JSON Schema for this reporters output.
   */
  SchemaBuilder outputSpec();

  /**
   * Constructs and returns a new, configured ColumnReport Streamer instance
   *
   * @param out
   *   output stream that the configured runner will write to.
   *
   * @return a configured report runner
   *
   * @throws WdkModelException
   *   may be thrown by implementing classes if the runner could not be
   *   constructed or configured.
   */
  Aggregator<T> build(OutputStream out) throws WdkModelException;

  ReportRunner runner() throws WdkModelException;

  interface Aggregator<T> extends AutoCloseable {

    /**
     * Casts or parses the given string value into an instance of {@link T}.
     *
     * @param raw
     *   String value of a single row of data for the column this
     *   ColumnReport.Streamer is operating on.
     *
     * @return An instance of {@link T} parsed out of the given string.
     *
     * @throws WdkModelException
     *   if the given string cannot be parsed into an instance of {@link T}
     */
    T parse(String raw) throws WdkModelException;

    void write(T field) throws WdkModelException;

    @Override
    void close() throws WdkModelException;
  }

  interface ReportRunner {
    void run(Aggregator s) throws WdkModelException;
  }
}
