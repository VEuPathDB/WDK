package org.gusdb.wdk.model.dataset;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DatasetParam;

public interface DatasetParser {

  void setParam(DatasetParam param);

  String getName();

  void setName(String name);

  String getDisplay();

  void setDisplay(String display);

  String getDescription();

  void setDescription(String description);

  void setProperties(Map<String, String> properties);

  DatasetIterator iterator(DatasetContents contents);

  default int datasetContentSize(final DatasetContents contents)
  throws WdkModelException, WdkUserException {
    var out = 0;
    var it = iterator(contents);
    while (it.hasNext()) {
      it.next();
      out++;
    }
    return out;
  }

  default int datasetContentWidth(final DatasetContents contents)
  throws WdkModelException, WdkUserException {
    var it = iterator(contents);
    return it.hasNext() ? it.next().length : 0;
  }

  /**
   * Iterator allowing access to the underlying dataset
   * contents one entry at a time.
   *
   * Iterator implementations should only return unique
   * entries.
   */
  interface DatasetIterator {
    /**
     * Parses and retrieves the next row from the input
     * {@link DatasetContents} stream.
     *
     * @return The row parsed into columns.
     *
     * @throws WdkModelException If an error is encountered
     *         while attempting to retrieve or parse the next
     *         row from the stream.
     * @throws WdkUserException If the row was found to be
     *         invalid input from the user.
     */
    String[] next() throws WdkModelException, WdkUserException;

    /**
     * Returns whether or not additional rows are available in
     * the {@link DatasetContents} stream.
     *
     * @throws WdkModelException If an error is encountered
     *         while attempting to read from the stream.
     * @throws WdkUserException If indexing the input failed
     *         due to invalid input from the user.
     */
    boolean hasNext() throws WdkModelException, WdkUserException;
  }
}
