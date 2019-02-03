package org.gusdb.wdk.model.dataset;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkUserException;

public class ListDatasetParser extends AbstractDatasetParser {
  
  public static final String NAME = "list";

  public static final String PROP_ROW_DIVIDER = "row.divider";
  public static final String PROP_COLUMN_DIVIDER = "column.divider";

  public static final String DATASET_COLUMN_DIVIDER = "______";

  private static final Logger logger = Logger.getLogger(ListDatasetParser.class);

  public ListDatasetParser() {
    setName(NAME);
    setDisplay("List");
    setDescription("The input is a list of records, one record in each row, and each record can have multiple columns.");
  }

  @Override
  public List<String[]> parse(String rawValue) throws WdkUserException {
    //String rowDivider = getRowDivider(rawValue);
    // scrum Feb 2 2016: we allow all these characters as row dividers, do not expect columns
    String rowDivider = "[\\s,;]+";
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
        throw new WdkUserException("The input data for datasetParam has various columns at row #" +
            records.size() + ". " + "The number of columns has to be the same for all the rows.");
      records.add(columns);
    }
    return records;
  }

  // TODO Decide on long-term API for dataset uploads (currently only allow IDs and 
  @SuppressWarnings("unused")
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

    // determine the divider by content, starting with DATASET_COLUMN_DIVIDER
    if (row.indexOf(DATASET_COLUMN_DIVIDER) > 0)
      return DATASET_COLUMN_DIVIDER;

    // then tab
    if (row.indexOf('\t') > 0)
      return "\t";

    // then comma
    if (row.indexOf(',') >= 0)
      return ",";

    // then white space
    return "\\s+";
  }

}
