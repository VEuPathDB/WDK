package org.gusdb.wdk.model.record;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Wraps a buffer reader with a csv reader (from OpenCSV)
 * 
 * @author crisl-adm
 */
public class CsvResultList implements ResultList {

  /** A representation in the CSV files for a null value */
  public static final String NULL_REPRESENTATION = "__null__";

  /** A tab char that may be employed as a CSV file column separator */
  public static final char TAB = '\t';

  /** A comma char that may be employed as a CSV file column separator */
  public static final char COMMA = ',';

  /** A quote char that may be employed to bracket items */
  public static final char QUOTE = '\"';

  /** The escape char to use in escaping quotes */
  public static final char ESCAPE = '\\';

  /** The line separator char */
  public static final String NEWLINE = "\n";

  private List<String> _columnNames;
  private String[] _currentLine;
  private boolean _isClosed;
  private BufferedReader _bufferedReader;
  private CSVReader _csvReader;

  /**
   * Initializes a CSV Reader from a provided buffered reader. Column names need to be provided in exactly the
   * order they appear in the CSV file as the file itself may not indicate column order.
   * 
   * @param reader
   *          - buffered reader
   * @param columnNames
   *          - ordered list of column names
   * @throws WdkModelException
   */
  public CsvResultList(BufferedReader reader, List<String> columnNames, char columnSeparator,
      char quoteCharacter, char escapeCharacter) throws WdkModelException {
    if (reader == null || columnNames == null || columnNames.isEmpty()) {
      throw new WdkModelException("The required constructor parameters are not valid.");
    }
    _bufferedReader = reader;
    _csvReader = new CSVReaderBuilder(_bufferedReader)
        .withCSVParser(new CSVParserBuilder()
            .withSeparator(columnSeparator)
            .withQuoteChar(quoteCharacter)
            .withEscapeChar(escapeCharacter)
            .build())
        .build();
    _isClosed = false;
    _columnNames = columnNames;
  }

  /**
   * Since next() reads ahead one line, we store that line in the object
   */
  @Override
  public boolean next() throws WdkModelException {
    try {
      _currentLine = _csvReader.readNext();
    }
    catch (IOException | CsvValidationException e) {
      throw new WdkModelException(e);
    }
    // Added to be sure current line columns and column names match size-wise since
    // we are using indexing to associate the two.
    if (_currentLine != null && _currentLine.length != _columnNames.size()) {
      throw new WdkModelException("The line returned [" + Arrays.toString(_currentLine) + "]" +
          " does not match the column names [" + Arrays.toString(_columnNames.toArray()) + "]");
    }
    return _currentLine != null;
  }

  /**
   * The data item associated with the requested column name for the current line is returned. Requesting a
   * column name not in the list originally provided via the constructor will result in a WDK model exception.
   */
  @Override
  public Object get(String columnName) throws WdkModelException {
    int i = _columnNames.indexOf(columnName);
    if (i < 0) {
      throw new WdkModelException("The column name " + columnName + " does not exist.");
    }
    Object value = _currentLine[i];
    // Turn truly null values back into nulls
    value = NULL_REPRESENTATION.equals(value) ? null : value;
    return value;
  }

  /**
   * Indicates whether the columnName given is among the columns returned by the result list.
   */
  @Override
  public boolean contains(String columnName) throws WdkModelException {
    return _columnNames.contains(columnName);
  }

  /**
   * Closes this CSV reader if open. Presumably, the underlying buffered reader is closed as well as a result.
   */
  @Override
  public void close() throws WdkModelException {
    if (!_isClosed) {
      try {
        _csvReader.close();
        _isClosed = true;
      }
      catch (IOException ioe) {
        throw new WdkModelException(ioe);
      }
    }
  }

}
