package org.gusdb.wdk.model.dataset;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class ListDatasetParser extends AbstractDatasetParser {
  
  public static final String NAME = "list";

  public static final String PROP_ROW_DIVIDER = "row.divider";
  public static final String PROP_COLUMN_DIVIDER = "column.divider";

  private static final Logger logger = Logger.getLogger(ListDatasetParser.class);

  public ListDatasetParser() {
    setName(NAME);
    setDisplay("list");
    setDescription("The input is a list of records, one record in each row, and each record can have multiple columns.");
  }

  @Override
  public List<String[]> parse(String rawValue) throws WdkDatasetException {
    String rowDivider = getRowDivider(rawValue);
    String[] rows = rawValue.split(rowDivider);
    String columnDivider = getColumnDivider(rows[0]);

    logger.debug("row divider='" + rowDivider + "', col divider='" + columnDivider + "' for content '" + rawValue);

    List<String[]> records = new ArrayList<String[]>();
    int columnCount = 0;
    for (String row : rows) {
      row = row.trim();
      if (row.length() == 0)
        continue;

      String[] columns = row.split(columnDivider);
      if (columnCount == 0)
        columnCount = columns.length;
      else if (columnCount != columns.length)
        throw new WdkDatasetException("The input data for datasetParam has various columns at row #" +
            records.size() + ". " + "The number of columns has to be the same for all the rows.");
      records.add(columns);
    }
    return records;
  }

  private String getRowDivider(String rawValue) {
    // use the user specified one, if any
    String rowDivider = properties.get(PROP_ROW_DIVIDER);
    if (rowDivider != null)
      return rowDivider;

    // determine the divider by content, starting with newline
    if (rawValue.indexOf('\n') > 0)
      return "\n";

    // then semi-colon
    if (rawValue.indexOf(';') >= 0)
      return ";";

    // then comma
    if (rawValue.indexOf(',') >= 0)
      return ",";

    // then white space
    return "\\s+";
  }

  private String getColumnDivider(String row) {
    // use the user specified one, if any;
    String columnDivider = properties.get(PROP_COLUMN_DIVIDER);
    if (columnDivider != null)
      return columnDivider;

    // determine the divider by content, starting with tab
    if (row.indexOf('\t') > 0)
      return "\t";

    // then pipe
    if (row.indexOf('|') >= 0)
      return "\\|";

    // then comma
    if (row.indexOf(',') >= 0)
      return ",";

    // then white space
    return "\\s+";
  }

}
