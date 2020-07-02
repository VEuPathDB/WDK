package org.gusdb.wdk.model.dataset;

import org.gusdb.wdk.model.WdkUserException;

public class ListDatasetParser extends AbstractDatasetParser {

  // scrum Feb 2 2016: we allow all these characters as row dividers, do not expect columns
  private static final String ROW_DIVIDER = "[\\s,;]+";

  public static final String NAME = "list";
  public static final String PROP_COLUMN_DIVIDER = "column.divider";
  public static final String DATASET_COLUMN_DIVIDER = "______";

  public ListDatasetParser() {
    setName(NAME);
    setDisplay("List");
    setDescription("The input is a list of records, one record in each row, "
      + "and each record can have multiple columns.");
  }

  @Override
  public DatasetIterator iterator(final DatasetContents contents) {
    return new Iterator(contents);
  }

  private class Iterator extends AbstractDatasetIterator {
    /**
     * Column divider.
     *
     * This value is determined dynamically on parsing the
     * first input row.
     */
    private String colDivider;

    /**
     * Number of columns expected per row.
     *
     * This value is determined by the column count of the
     * first row.  If subsequent rows have a different number
     * of columns, an error will be thrown.
     */
    private int colCount;

    /**
     * Track the current input row number for error reporting.
     */
    private int parsedRowNum;

    Iterator(final DatasetContents contents) {
      super(contents, ROW_DIVIDER);
    }

    @Override
    protected boolean rowFilter(final String row) {
      return !row.isBlank();
    }

    @Override
    protected String[] parseRow(final String row) throws WdkUserException {
      if (colDivider == null)
        colDivider = getColumnDivider(row);

      var columns = row.trim().split(colDivider);
      if (colCount == 0)
        colCount = columns.length;
      else if (colCount != columns.length)
        throw new WdkUserException(
          "The input data for datasetParam has various columns at row #" + parsedRowNum
            + ". The number of columns must be the same for all the rows."
        );

      parsedRowNum++;
      return columns;
    }

    private String getColumnDivider(String row) {
      // use the user specified one, if any;
      String columnDivider = properties.get(PROP_COLUMN_DIVIDER);
      if (columnDivider != null)
        return columnDivider;

      // determine the divider by content, starting with DATASET_COLUMN_DIVIDER
      if (row.indexOf(DATASET_COLUMN_DIVIDER) > 0)
        return DATASET_COLUMN_DIVIDER;

      // then tab
      if (row.indexOf('\t') > 0)
        return " *\t *";

      // then comma
      if (row.indexOf(',') >= 0)
        return "\\s*,\\s*";

      // then white space
      return "\\s+";
    }
  }
}
